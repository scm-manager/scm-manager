package sonia.scm.security;

import java.time.Instant;
import java.util.Date;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

public class JwtAccessTokenRefresher {

  private final JwtAccessTokenBuilderFactory builderFactory;
  private final JwtAccessTokenRefreshStrategy refreshStrategy;

  public JwtAccessTokenRefresher(JwtAccessTokenBuilderFactory builderFactory, JwtAccessTokenRefreshStrategy refreshStrategy) {
    this.builderFactory = builderFactory;
    this.refreshStrategy = refreshStrategy;
  }

  public Optional<JwtAccessToken> refresh(JwtAccessToken oldToken) {
    JwtAccessTokenBuilder builder = builderFactory.create();
    Map<String, Object> claims = oldToken.getClaims();
    claims.forEach(builder::custom);

    if (canBeRefreshed(oldToken) && shouldBeRefreshed(oldToken)) {
      builder.expiresIn(1, TimeUnit.HOURS);
//    builder.custom("scm-manager.parentTokenId")
      return Optional.of(builder.build());
    } else {
      return Optional.empty();
    }
  }

  private boolean canBeRefreshed(JwtAccessToken oldToken) {
    return tokenIsValid(oldToken) || tokenCanBeRefreshed(oldToken);
  }

  private boolean shouldBeRefreshed(JwtAccessToken oldToken) {
    return refreshStrategy.shouldBeRefreshed(oldToken);
  }

  private boolean tokenCanBeRefreshed(JwtAccessToken oldToken) {
    Date refreshExpiration = oldToken.getRefreshExpiration();
    return refreshExpiration != null && isBeforeNow(refreshExpiration);
  }

  private boolean tokenIsValid(JwtAccessToken oldToken) {
    return isBeforeNow(oldToken.getExpiration());
  }

  private boolean isBeforeNow(Date expiration) {
    return expiration.toInstant().isBefore(Instant.now());
  }
}
