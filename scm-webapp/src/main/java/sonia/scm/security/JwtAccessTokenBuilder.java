/**
 * Copyright (c) 2014, Sebastian Sdorra
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 3. Neither the name of SCM-Manager; nor the names of its
 *    contributors may be used to endorse or promote products derived from this
 *    software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * http://bitbucket.org/sdorra/scm-manager
 *
 */
package sonia.scm.security;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.group.ExternalGroupNames;

import java.time.Clock;
import java.time.Instant;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Jwt implementation of {@link AccessTokenBuilder}.
 *
 * @author Sebastian Sdorra
 * @since 2.0.0
 */
public final class JwtAccessTokenBuilder implements AccessTokenBuilder {

  /**
   * the logger for JwtAccessTokenBuilder
   */
  private static final Logger LOG = LoggerFactory.getLogger(JwtAccessTokenBuilder.class);

  private final KeyGenerator keyGenerator;
  private final SecureKeyResolver keyResolver;
  private final Clock clock;

  private String subject;
  private String issuer;
  private long expiresIn = 1;
  private TimeUnit expiresInUnit = TimeUnit.HOURS;
  private long refreshableFor = 12;
  private TimeUnit refreshableForUnit = TimeUnit.HOURS;
  private Instant refreshExpiration;
  private String parentKeyId;
  private Scope scope = Scope.empty();
  private Set<String> groups = new HashSet<>();

  private final Map<String,Object> custom = Maps.newHashMap();

  JwtAccessTokenBuilder(KeyGenerator keyGenerator, SecureKeyResolver keyResolver, Clock clock)  {
    this.keyGenerator = keyGenerator;
    this.keyResolver = keyResolver;
    this.clock = clock;
  }

  @Override
  public JwtAccessTokenBuilder subject(String subject) {
    Preconditions.checkArgument(!Strings.isNullOrEmpty(subject), "null or empty value not allowed");
    this.subject = subject;
    return this;
  }

  @Override
  public JwtAccessTokenBuilder custom(String key, Object value) {
    Preconditions.checkArgument(!Strings.isNullOrEmpty(key), "null or empty value not allowed");
    Preconditions.checkArgument(value != null, "null or empty value not allowed");
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

  @Override
  public JwtAccessTokenBuilder groups(String... groups) {
    Collections.addAll(this.groups, groups);
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
      .setIssuedAt(Date.from(now))
      .setExpiration(new Date(now.toEpochMilli() + expiration));


    if (refreshableFor > 0) {
      long refreshExpiration = refreshableForUnit.toMillis(refreshableFor);
      claims.put(JwtAccessToken.REFRESHABLE_UNTIL_CLAIM_KEY, new Date(now.toEpochMilli() + refreshExpiration).getTime());
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

    if (!groups.isEmpty()) {
      claims.put(JwtAccessToken.GROUPS_CLAIM_KEY, groups);
    } else {
      Subject currentSubject = SecurityUtils.getSubject();
      ExternalGroupNames externalGroupNames = currentSubject.getPrincipals().oneByType(ExternalGroupNames.class);
      if (externalGroupNames != null) {
        claims.put(JwtAccessToken.GROUPS_CLAIM_KEY, externalGroupNames.getCollection().toArray(new String[]{}));
      }
    }

    // sign token and create compact version
    String compact = Jwts.builder()
        .setClaims(claims)
        .signWith(SignatureAlgorithm.HS256, key.getBytes())
        .compact();

    return new JwtAccessToken(claims, compact);
  }

}
