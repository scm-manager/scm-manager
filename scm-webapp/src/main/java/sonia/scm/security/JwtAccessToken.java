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

import com.google.common.collect.ImmutableSet;
import io.jsonwebtoken.Claims;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static java.util.Optional.ofNullable;

/**
 * Jwt implementation of {@link AccessToken}.
 * 
 * @author Sebastian Sdorra
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
