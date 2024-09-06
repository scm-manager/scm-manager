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

import jakarta.inject.Inject;
import org.apache.shiro.SecurityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Clock;
import java.util.Date;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

public class JwtAccessTokenRefresher {

  private static final Logger log = LoggerFactory.getLogger(JwtAccessTokenRefresher.class);

  private final JwtAccessTokenBuilderFactory builderFactory;
  private final JwtAccessTokenRefreshStrategy refreshStrategy;
  private final Clock clock;

  @Inject
  public JwtAccessTokenRefresher(JwtAccessTokenBuilderFactory builderFactory, JwtAccessTokenRefreshStrategy refreshStrategy) {
    this(builderFactory, refreshStrategy, Clock.systemDefaultZone());
  }

  JwtAccessTokenRefresher(JwtAccessTokenBuilderFactory builderFactory, JwtAccessTokenRefreshStrategy refreshStrategy, Clock clock) {
    this.builderFactory = builderFactory;
    this.refreshStrategy = refreshStrategy;
    this.clock = clock;
  }

  @SuppressWarnings("squid:S3655") // the refresh expiration cannot be null at the time building the new token, because
                                   // we checked this before in tokenCanBeRefreshed
  public Optional<JwtAccessToken> refresh(JwtAccessToken oldToken) {
    JwtAccessTokenBuilder builder = builderFactory.create();
    Map<String, Object> claims = oldToken.getClaims();
    claims.forEach(builder::custom);

    if (canBeRefreshed(oldToken) && shouldBeRefreshed(oldToken)) {
      Optional<Object> parentTokenId = oldToken.getCustom("scm-manager.parentTokenId");
      if (!parentTokenId.isPresent()) {
        log.warn("no parent token id found in token; could not refresh");
        return Optional.empty();
      }
      builder.expiresIn(computeOldExpirationInMillis(oldToken), TimeUnit.MILLISECONDS);
      builder.parentKey(parentTokenId.get().toString());
      builder.refreshExpiration(oldToken.getRefreshExpiration().get().toInstant());
      return Optional.of(builder.build());
    } else {
      return Optional.empty();
    }
  }

  private long computeOldExpirationInMillis(JwtAccessToken oldToken) {
    return oldToken.getExpiration().getTime() - oldToken.getIssuedAt().getTime();
  }

  private boolean canBeRefreshed(JwtAccessToken oldToken) {
    return tokenIsValid(oldToken) && tokenCanBeRefreshed(oldToken)
      && SecurityUtils.getSubject().getPrincipals() != null;
  }

  private boolean shouldBeRefreshed(JwtAccessToken oldToken) {
    return refreshStrategy.shouldBeRefreshed(oldToken);
  }

  private boolean tokenCanBeRefreshed(JwtAccessToken oldToken) {
    return oldToken.getRefreshExpiration().map(this::isAfterNow).orElse(false);
  }

  private boolean tokenIsValid(JwtAccessToken oldToken) {
    return isAfterNow(oldToken.getExpiration());
  }

  private boolean isAfterNow(Date expiration) {
    return expiration.toInstant().isAfter(clock.instant());
  }
}
