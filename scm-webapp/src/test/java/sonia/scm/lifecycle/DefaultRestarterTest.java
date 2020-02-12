package sonia.scm.lifecycle;

import com.github.legman.Subscribe;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.event.ScmEventBus;

import javax.swing.*;

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
    System.setProperty(RestartStrategyFactory.PROPERTY_STRATEGY, ExitRestartStrategy.NAME);
    try {
      DefaultRestarter restarter = new DefaultRestarter();
      assertThat(restarter.isSupported()).isTrue();
    } finally {
      System.clearProperty(RestartStrategyFactory.PROPERTY_STRATEGY);
    }
  }

  @Test
  void shouldReturnFalseIfRestartStrategyIsNotAvailable() {
    DefaultRestarter restarter = new DefaultRestarter(eventBus, null);
    assertThat(restarter.isSupported()).isFalse();
  }

  @Test
  void shouldReturnTrueIfRestartStrategyIsAvailable() {
    DefaultRestarter restarter = new DefaultRestarter();
    assertThat(restarter.isSupported()).isTrue();
  }

  @Test
  void shouldThrowRestartNotSupportedException() {
    DefaultRestarter restarter = new DefaultRestarter(eventBus,null);
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
