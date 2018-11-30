package sonia.scm.security;

import java.time.Clock;

public class PercentageJwtAccessTokenRefreshStrategy implements JwtAccessTokenRefreshStrategy {

  private final Clock clock;
  private final float refreshPercentage;

  public PercentageJwtAccessTokenRefreshStrategy(float refreshPercentage) {
    this(Clock.systemDefaultZone(), refreshPercentage);
  }

  PercentageJwtAccessTokenRefreshStrategy(Clock clock, float refreshPercentage) {
    this.clock = clock;
    this.refreshPercentage = refreshPercentage;
  }

  @Override
  public boolean shouldBeRefreshed(JwtAccessToken oldToken) {
    long liveSpan = oldToken.getExpiration().getTime() - oldToken.getIssuedAt().getTime();
    long age = clock.instant().toEpochMilli() - oldToken.getIssuedAt().getTime();
    return (float)age/liveSpan > refreshPercentage;
  }
}
