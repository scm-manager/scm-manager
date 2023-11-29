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

import com.github.sdorra.shiro.ShiroRule;
import com.github.sdorra.shiro.SubjectAware;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

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

  @Rule
  public ShiroRule shiro = new ShiroRule();

  private KeyGenerator keyGenerator = () -> "key";
  private JwtConfig jwtConfig = mock(JwtConfig.class);

  private Clock refreshClock = mock(Clock.class);

  private JwtAccessTokenBuilder tokenBuilder;
  private PercentageJwtAccessTokenRefreshStrategy refreshStrategy;

  @Before
  public void initToken() {
    SecureKeyResolver keyResolver = mock(SecureKeyResolver.class);
    when(keyResolver.getSecureKey(any())).thenReturn(createSecureKey());

    Clock creationClock = mock(Clock.class);
    when(creationClock.instant()).thenReturn(TOKEN_CREATION);

    tokenBuilder = new JwtAccessTokenBuilderFactory(keyGenerator, keyResolver, jwtConfig, Collections.emptySet(), creationClock).create();
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
