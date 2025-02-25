/*
 * Copyright (c) 2020 - present Cloudogu GmbH
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

package sonia.scm.security;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.AuthorizationException;
import org.apache.shiro.subject.Subject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.config.ScmConfiguration;

import java.time.Clock;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Jwt implementation of {@link AccessTokenBuilder}.
 *
 * @since 2.0.0
 */
public final class JwtAccessTokenBuilder implements AccessTokenBuilder {

 
  private static final Logger LOG = LoggerFactory.getLogger(JwtAccessTokenBuilder.class);

  @VisibleForTesting
  static final long DEFAULT_REFRESHABLE = 12L;

  @VisibleForTesting
  static final TimeUnit DEFAULT_REFRESHABLE_UNIT = TimeUnit.HOURS;

  private final KeyGenerator keyGenerator;
  private final SecureKeyResolver keyResolver;
  private final JwtConfig jwtConfig;
  private final Clock clock;
  private final ScmConfiguration scmConfiguration;

  private String subject;
  private String issuer;
  private long expiresIn;
  private TimeUnit expiresInUnit = TimeUnit.HOURS;
  private long refreshableFor = DEFAULT_REFRESHABLE;
  private TimeUnit refreshableForUnit = DEFAULT_REFRESHABLE_UNIT;
  private Instant refreshExpiration;
  private String parentKeyId;
  private Scope scope = Scope.empty();

  private final Map<String,Object> custom = Maps.newHashMap();

  JwtAccessTokenBuilder(KeyGenerator keyGenerator, SecureKeyResolver keyResolver, JwtConfig jwtConfig, Clock clock, ScmConfiguration scmConfiguration)  {
    this.keyGenerator = keyGenerator;
    this.keyResolver = keyResolver;
    this.jwtConfig = jwtConfig;
    this.clock = clock;
    this.scmConfiguration = scmConfiguration;
    this.expiresIn = scmConfiguration.getJwtExpirationInH();
  }

  @Override
  public JwtAccessTokenBuilder subject(String subject) {
    Preconditions.checkArgument(!Strings.isNullOrEmpty(subject), "null or empty value not allowed");
    this.subject = subject;
    return this;
  }

  @Override
  public JwtAccessTokenBuilder custom(String key, Object value) {
    this.custom.put(key, value);
    return this;
  }

  @Override
  public JwtAccessTokenBuilder scope(Scope scope) {
    Preconditions.checkArgument(scope != null, "scope cannot be null");
    this.scope = scope;
    return this;
  }

  @Override
  public JwtAccessTokenBuilder issuer(String issuer) {
    Preconditions.checkArgument(!Strings.isNullOrEmpty(issuer), "null or empty value not allowed");
    this.issuer = issuer;
    return this;
  }

  @Override
  public JwtAccessTokenBuilder expiresIn(long count, TimeUnit unit) {
    Preconditions.checkArgument(count > 0, "count must be greater than 0");
    Preconditions.checkArgument(unit != null, "unit cannot be null");

    this.expiresIn = count;
    this.expiresInUnit = unit;

    return this;
  }

  @Override
  public JwtAccessTokenBuilder refreshableFor(long count, TimeUnit unit) {
    Preconditions.checkArgument(count >= 0, "count must be greater or equal to 0");
    Preconditions.checkArgument(unit != null, "unit cannot be null");

    this.refreshableFor = count;
    this.refreshableForUnit = unit;

    return this;
  }

  JwtAccessTokenBuilder refreshExpiration(Instant refreshExpiration) {
    this.refreshExpiration = refreshExpiration;
    this.refreshableFor = 0;
    return this;
  }

  public JwtAccessTokenBuilder parentKey(String parentKeyId) {
    this.parentKeyId = parentKeyId;
    return this;
  }

  private String getSubject(){
    if (subject == null) {
      Subject currentSubject = SecurityUtils.getSubject();
      // TODO find a better way
      currentSubject.checkRole(Role.USER);
      return currentSubject.getPrincipal().toString();
    }
    return subject;
  }

  @Override
  public JwtAccessToken build() {
    final Scope principalScope = SecurityUtils.getSubject().getPrincipals().oneByType(Scope.class);
    if (principalScope != null && !principalScope.isEmpty()) {
      if (scope != null && !supportsAll()) {
        throw new AuthorizationException(String.format("cannot merge builder scope (%s) with principal scope (%s)", scope, principalScope));
      }
      LOG.debug("using existing scope for new access token: {}", principalScope);
      scope = principalScope;
    }
    String id = keyGenerator.createKey();

    String sub = getSubject();

    LOG.trace("create new token {} for user {}", id, subject);
    SecureKey key = keyResolver.getSecureKey(sub);

    Map<String,Object> customClaims = new HashMap<>(custom);

    // add scope to custom claims
    Scopes.toClaims(customClaims, scope);

    Instant now = clock.instant();
    long expiration = expiresInUnit.toMillis(expiresIn);

    Claims claims = Jwts.claims(customClaims)
      .setSubject(sub)
      .setId(id)
      .setIssuedAt(Date.from(now));

    if(!jwtConfig.isEndlessJwtEnabled()) {
      claims.setExpiration(new Date(now.toEpochMilli() + expiration));
    } else {
      scmConfiguration.setEnabledJwtExpiration(true);
    }

    if (refreshableFor > 0) {
      long re = refreshableForUnit.toMillis(refreshableFor);
      claims.put(JwtAccessToken.REFRESHABLE_UNTIL_CLAIM_KEY, Date.from(now.plusMillis(re)));
    } else if (refreshExpiration != null) {
      claims.put(JwtAccessToken.REFRESHABLE_UNTIL_CLAIM_KEY, Date.from(refreshExpiration));
    }
    if (parentKeyId == null) {
      claims.put(JwtAccessToken.PARENT_TOKEN_ID_CLAIM_KEY, id);
    } else {
      claims.put(JwtAccessToken.PARENT_TOKEN_ID_CLAIM_KEY, parentKeyId);
    }

    if ( issuer != null ) {
      claims.setIssuer(issuer);
    }
    
    // sign token and create compact version
    String compact = Jwts.builder()
        .setClaims(claims)
        .signWith(Keys.hmacShaKeyFor(key.getBytes()), SignatureAlgorithm.HS256)
        .compact();

    return new JwtAccessToken(claims, compact);
  }

  private boolean supportsAll() {
    for (String permission : scope) {
      if (!SecurityUtils.getSubject().isPermitted(permission)) {
        return false;
      }
    }
    return true;
  }
}
