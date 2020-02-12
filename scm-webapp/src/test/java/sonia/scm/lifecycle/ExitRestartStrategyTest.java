package sonia.scm.lifecycle;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.function.IntConsumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ExitRestartStrategyTest {

  @Mock
  private RestartStrategy.InjectionContext context;

  private ExitRestartStrategy strategy;
  private CapturingExiter exiter;

  @BeforeEach
  void setUpStrategy() {
    strategy = new ExitRestartStrategy();
    exiter = new CapturingExiter();
    strategy.setExiter(exiter);
  }

  @Test
  void shouldTearDownContextAndThenExit() {
    strategy.restart(context);

    verify(context).destroy();
    assertThat(exiter.getExitCode()).isEqualTo(0);
  }

  @Test
  void shouldUseExitCodeFromSystemProperty() {
    System.setProperty(ExitRestartStrategy.PROPERTY_EXIT_CODE, "42");
    try {
      strategy.restart(context);

      verify(context).destroy();
      assertThat(exiter.getExitCode()).isEqualTo(42);
    } finally {
      System.clearProperty(ExitRestartStrategy.PROPERTY_EXIT_CODE);
    }
  }

  @Test
  void shouldThrowExceptionForNonNumericExitCode() {
    System.setProperty(ExitRestartStrategy.PROPERTY_EXIT_CODE, "xyz");
    try {
      assertThrows(RestartNotSupportedException.class, () -> strategy.restart(context));
    } finally {
      System.clearProperty(ExitRestartStrategy.PROPERTY_EXIT_CODE);
    }
  }

  private static class CapturingExiter implements IntConsumer {

    private int exitCode = -1;

    public int getExitCode() {
      return exitCode;
    }

    @Override
    public void accept(int exitCode) {
      this.exitCode = exitCode;
    }
  }
}
