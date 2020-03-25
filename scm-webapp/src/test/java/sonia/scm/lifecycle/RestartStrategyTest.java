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
  void shouldReturnRestartStrategyFromSystemPropertyWithClassLoaderConstructor() {
    withStrategy(ComplexRestartStrategy.class.getName(), (rs) -> {
      assertThat(rs).containsInstanceOf(ComplexRestartStrategy.class)
        .get()
        .extracting("classLoader")
        .isSameAs(classLoader);
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

  public static class ComplexRestartStrategy implements RestartStrategy {

    private final ClassLoader classLoader;

    public ComplexRestartStrategy(ClassLoader classLoader) {
      this.classLoader = classLoader;
    }

    @Override
    public void restart(InjectionContext context) {

    }
  }

}
