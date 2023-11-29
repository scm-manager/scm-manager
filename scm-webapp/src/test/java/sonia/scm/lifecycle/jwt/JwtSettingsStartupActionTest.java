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

package sonia.scm.lifecycle.jwt;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.security.JwtConfig;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtSettingsStartupActionTest {

  private JwtSettingsStartupAction jwtSettingsAction;

  @Mock
  private JwtSettingsStore jwtSettingsStore;

  @Mock
  private JwtConfig jwtConfig;

  private final Clock clock = Clock.fixed(Instant.now(), ZoneId.systemDefault());

  @BeforeEach
  void setupAction() {
    jwtSettingsAction = new JwtSettingsStartupAction(jwtSettingsStore, jwtConfig, clock);
  }

  @Test
  void shouldNotChangeSettings_Enabled() {
    when(jwtConfig.isEndlessJwtEnabled()).thenReturn(true);

    JwtSettings settings = new JwtSettings(true, 0);
    when(jwtSettingsStore.get()).thenReturn(settings);

    jwtSettingsAction.run();

    assertThat(settings.isEndlessJwtEnabledLastStartUp()).isTrue();
    assertThat(settings.getKeysValidAfterTimestampInMs()).isZero();

    verify(jwtSettingsStore).get();
    verifyNoMoreInteractions(jwtSettingsStore);
  }

  @Test
  void shouldNotChangeSettings_Disabled() {
    when(jwtConfig.isEndlessJwtEnabled()).thenReturn(false);

    JwtSettings settings = new JwtSettings(false, 0);
    when(jwtSettingsStore.get()).thenReturn(settings);

    jwtSettingsAction.run();

    assertThat(settings.isEndlessJwtEnabledLastStartUp()).isFalse();
    assertThat(settings.getKeysValidAfterTimestampInMs()).isZero();

    verify(jwtSettingsStore).get();
    verifyNoMoreInteractions(jwtSettingsStore);
  }


  @Test
  void shouldOnlyUpdateEndlessJwtEnabledLastStartup() {
    when(jwtConfig.isEndlessJwtEnabled()).thenReturn(true);
    JwtSettings settings = new JwtSettings(false, 0);
    when(jwtSettingsStore.get()).thenReturn(settings);

    jwtSettingsAction.run();


    verify(jwtSettingsStore).get();
    verify(jwtSettingsStore).set(argThat(actualSettings -> {
      assertThat(actualSettings.isEndlessJwtEnabledLastStartUp()).isTrue();
      assertThat(actualSettings.getKeysValidAfterTimestampInMs()).isZero();
      return true;
    }));
  }

  @Test
  void shouldInvalidateKeys() {
    JwtSettings settings = new JwtSettings(true, 0);
    when(jwtSettingsStore.get()).thenReturn(settings);

    jwtSettingsAction.run();

    verify(jwtSettingsStore).get();
    verify(jwtSettingsStore).set(argThat(actualSettings -> {
      assertThat(actualSettings.isEndlessJwtEnabledLastStartUp()).isEqualTo(false);
      assertThat(actualSettings.getKeysValidAfterTimestampInMs()).isEqualTo(Instant.now(clock).toEpochMilli());
      return true;
    }));
  }
}
