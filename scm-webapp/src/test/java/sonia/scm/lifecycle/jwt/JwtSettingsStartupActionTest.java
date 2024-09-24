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
