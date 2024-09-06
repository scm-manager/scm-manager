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

package sonia.scm.lifecycle;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.event.ScmEventBus;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class DefaultRestarterTest {

  @Mock
  private ScmEventBus eventBus;

  @Captor
  private ArgumentCaptor<RestartEvent> eventCaptor;

  @Test
  void shouldLoadStrategyOnCreation() {
    System.setProperty(RestartStrategyFactory.RESTART_STRATEGY, ExitRestartStrategy.NAME);
    try {
      DefaultRestarter restarter = new DefaultRestarter();
      assertThat(restarter.isSupported()).isTrue();
    } finally {
      System.clearProperty(RestartStrategyFactory.RESTART_STRATEGY);
    }
  }

  @Test
  void shouldReturnFalseIfRestartStrategyIsNotAvailable() {
    DefaultRestarter restarter = new DefaultRestarter(eventBus, null);
    assertThat(restarter.isSupported()).isFalse();
  }

  @DisabledOnOs(OS.WINDOWS)
  @Test
  void shouldReturnTrueIfRestartStrategyIsAvailable() {
    DefaultRestarter restarter = new DefaultRestarter();
    assertThat(restarter.isSupported()).isTrue();
  }

  @Test
  void shouldThrowRestartNotSupportedException() {
    DefaultRestarter restarter = new DefaultRestarter(eventBus, null);
    assertThrows(
      RestartNotSupportedException.class, () -> restarter.restart(DefaultRestarterTest.class, "test")
    );
  }

  @Test
  void shouldFireRestartEvent() {
    DefaultRestarter restarter = new DefaultRestarter(eventBus, new ExitRestartStrategy());
    restarter.restart(DefaultRestarterTest.class, "testing");

    verify(eventBus).post(eventCaptor.capture());

    RestartEvent event = eventCaptor.getValue();
    assertThat(event.getCause()).isEqualTo(DefaultRestarterTest.class);
    assertThat(event.getReason()).isEqualTo("testing");
  }
}
