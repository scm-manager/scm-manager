/*
 * MIT License
 *
 * Copyright (c) 2020-present Cloudogu GmbH and Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
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

import java.time.Clock;
import java.time.Instant;
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

    // sign token and create compact version
    String compact = Jwts.builder()
        .setClaims(claims)
        .signWith(SignatureAlgorithm.HS256, key.getBytes())
        .compact();

    return new JwtAccessToken(claims, compact);
  }

}
