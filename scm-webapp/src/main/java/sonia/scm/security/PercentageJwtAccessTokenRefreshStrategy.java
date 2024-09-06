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
