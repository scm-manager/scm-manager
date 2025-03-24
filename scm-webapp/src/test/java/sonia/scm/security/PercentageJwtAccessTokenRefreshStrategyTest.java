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

import com.github.sdorra.shiro.ShiroRule;
import com.github.sdorra.shiro.SubjectAware;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import sonia.scm.config.ScmConfiguration;

import java.time.Clock;
import java.time.Instant;
import java.util.Collections;

import static java.time.temporal.ChronoUnit.MINUTES;
import static java.time.temporal.ChronoUnit.SECONDS;
import static java.util.concurrent.TimeUnit.HOURS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static sonia.scm.security.SecureKeyTestUtil.createSecureKey;

@SubjectAware(
  username = "user",
  password = "secret",
  configuration = "classpath:sonia/scm/repository/shiro.ini"
)
public class PercentageJwtAccessTokenRefreshStrategyTest {

  private static final Instant TOKEN_CREATION = Instant.now().truncatedTo(SECONDS);
  private final KeyGenerator keyGenerator = () -> "key";
  private final JwtConfig jwtConfig = mock(JwtConfig.class);
  private final Clock refreshClock = mock(Clock.class);
  @Rule
  public ShiroRule shiro = new ShiroRule();
  private JwtAccessTokenBuilder tokenBuilder;
  private PercentageJwtAccessTokenRefreshStrategy refreshStrategy;

  @Before
  public void initToken() {
    SecureKeyResolver keyResolver = mock(SecureKeyResolver.class);
    when(keyResolver.getSecureKey(any())).thenReturn(createSecureKey());
    ScmConfiguration scmConfiguration = new ScmConfiguration();
    Clock creationClock = mock(Clock.class);
    when(creationClock.instant()).thenReturn(TOKEN_CREATION);

    tokenBuilder = new JwtAccessTokenBuilderFactory(
      keyGenerator,
      keyResolver,
      jwtConfig,
      Collections.emptySet(),
      creationClock,
      scmConfiguration
    ).create();
    tokenBuilder.expiresIn(1, HOURS);
    tokenBuilder.refreshableFor(1, HOURS);

    refreshStrategy = new PercentageJwtAccessTokenRefreshStrategy(refreshClock, 0.5F);
  }

  @Test
  public void shouldNotRefreshWhenTokenIsYoung() {
    when(refreshClock.instant()).thenReturn(TOKEN_CREATION.plus(29, MINUTES));
    assertThat(refreshStrategy.shouldBeRefreshed(tokenBuilder.build())).isFalse();
  }

  @Test
  public void shouldRefreshWhenTokenIsOld() {
    when(refreshClock.instant()).thenReturn(TOKEN_CREATION.plus(31, MINUTES));
    assertThat(refreshStrategy.shouldBeRefreshed(tokenBuilder.build())).isTrue();
  }
}
