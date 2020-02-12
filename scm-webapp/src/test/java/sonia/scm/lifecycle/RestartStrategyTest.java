package sonia.scm.lifecycle;

import com.google.common.base.Strings;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import sonia.scm.util.SystemUtil;

import java.util.Optional;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RestartStrategyTest {
  private final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

  @Test
  void shouldReturnRestartStrategyFromSystemProperty() {
    withStrategy(TestingRestartStrategy.class.getName(), (rs) -> {
      assertThat(rs).containsInstanceOf(TestingRestartStrategy.class);
    });
  }

  @Test
  void shouldThrowExceptionForNonStrategyClass() {
    withStrategy(RestartStrategyTest.class.getName(), () -> {
      assertThrows(RestartNotSupportedException.class, () -> RestartStrategy.get(classLoader));
    });
  }

  @Test
  void shouldReturnEmpty() {
    withStrategy(RestartStrategyFactory.STRATEGY_NONE, (rs) -> {
      assertThat(rs).isEmpty();
    });
  }

  @Test
  void shouldReturnEmptyForUnknownOs() {
    withSystemProperty(SystemUtil.PROPERTY_OSNAME, "hitchhiker-os", () -> {
      Optional<RestartStrategy> restartStrategy = RestartStrategy.get(classLoader);
      assertThat(restartStrategy).isEmpty();
    });
  }

  @Test
  void shouldReturnExitRestartStrategy() {
    withStrategy(ExitRestartStrategy.NAME, (rs) -> {
      assertThat(rs).containsInstanceOf(ExitRestartStrategy.class);
    });
  }

  @Test
  void shouldReturnInjectionContextRestartStrategy() {
    withStrategy(InjectionContextRestartStrategy.NAME, (rs) -> {
      assertThat(rs).containsInstanceOf(InjectionContextRestartStrategy.class);
    });
  }

  @ParameterizedTest
  @ValueSource(strings = { "linux", "darwin", "solaris", "freebsd", "openbsd" })
  void shouldReturnPosixRestartStrategyForPosixBased(String os) {
    withSystemProperty(SystemUtil.PROPERTY_OSNAME, os, () -> {
      Optional<RestartStrategy> restartStrategy = RestartStrategy.get(classLoader);
      assertThat(restartStrategy).containsInstanceOf(PosixRestartStrategy.class);
    });
  }

  private void withStrategy(String strategy, Consumer<Optional<RestartStrategy>> consumer) {
      withStrategy(strategy, () -> {
        consumer.accept(RestartStrategy.get(classLoader));
      });
  }

  private void withStrategy(String strategy, Runnable runnable) {
    withSystemProperty(RestartStrategyFactory.PROPERTY_STRATEGY, strategy, runnable);
  }

  private void withSystemProperty(String key, String value, Runnable runnable) {
    String oldValue = System.getProperty(key);
    System.setProperty(key, value);
    try {
      runnable.run();
    } finally {
      if (Strings.isNullOrEmpty(oldValue)) {
        System.clearProperty(key);
      } else {
        System.setProperty(key, oldValue);
      }
    }
  }

  public static class TestingRestartStrategy implements RestartStrategy {
    @Override
    public void restart(InjectionContext context) {

    }
  }

}
