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
