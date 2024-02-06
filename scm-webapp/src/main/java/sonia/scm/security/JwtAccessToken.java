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

import com.google.common.collect.ImmutableSet;
import io.jsonwebtoken.Claims;

import java.util.Collections;
import java.util.Date;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static java.util.Optional.ofNullable;

/**
 * Jwt implementation of {@link AccessToken}.
 * 
 * @since 2.0.0
 */
public final class JwtAccessToken implements AccessToken {

  public static final String REFRESHABLE_UNTIL_CLAIM_KEY = "scm-manager.refreshExpiration";
  public static final String PARENT_TOKEN_ID_CLAIM_KEY = "scm-manager.parentTokenId";
  public static final String GROUPS_CLAIM_KEY = "scm-manager.groups";

  private final Claims claims;
  private final String compact;

  JwtAccessToken(Claims claims, String compact) {
    this.claims = claims;
    this.compact = compact;
  }

  @Override
  public String getId() {
    return claims.getId();
  }

  @Override
  public String getSubject() {
    return claims.getSubject();
  }

  @Override
  public Optional<String> getIssuer() {
    return Optional.ofNullable(claims.getIssuer());
  }

  @Override
  public Date getIssuedAt() {
    return claims.getIssuedAt();
  }

  @Override
  public Date getExpiration() {
    return claims.getExpiration();
  }

  @Override
  public Optional<Date> getRefreshExpiration() {
    return ofNullable(claims.get(REFRESHABLE_UNTIL_CLAIM_KEY, Date.class));
  }

  @Override
  public Optional<String> getParentKey() {
    return ofNullable(claims.get(PARENT_TOKEN_ID_CLAIM_KEY).toString());
  }

  @Override
  public Scope getScope() {
    return Scopes.fromClaims(claims);
  }
  
  @Override
  @SuppressWarnings("unchecked")
  public Optional<Object> getCustom(String key) {
    return Optional.ofNullable(claims.get(key));
  }

  @Override
  @SuppressWarnings("unchecked")
  public Set<String> getGroups() {
    Iterable<String> groups = claims.get(GROUPS_CLAIM_KEY, Iterable.class);
    if (groups != null) {
      return ImmutableSet.copyOf(groups);
    }
    return ImmutableSet.of();
  }

  @Override
  public String compact() {
    return compact;
  }

  @Override
  public Map<String, Object> getClaims() {
    return Collections.unmodifiableMap(claims);
  }
}
