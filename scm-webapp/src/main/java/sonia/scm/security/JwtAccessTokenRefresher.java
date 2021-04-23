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

import org.apache.shiro.SecurityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
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
