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
