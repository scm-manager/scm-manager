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
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import sonia.scm.Platform;
import sonia.scm.config.WebappConfigProvider;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RestartStrategyFactoryTest {

  private final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

  @Test
  void shouldReturnRestartStrategyFromSystemProperty() {
    RestartStrategyFactory factory = builder().withStrategy(TestingRestartStrategy.class).create();
    RestartStrategy restartStrategy = factory.fromClassLoader(classLoader);
    assertThat(restartStrategy).isInstanceOf(TestingRestartStrategy.class);
  }

  @Test
  void shouldReturnRestartStrategyFromSystemPropertyWithClassLoaderConstructor() {
    RestartStrategyFactory factory = builder().withStrategy(ComplexRestartStrategy.class).create();
    RestartStrategy restartStrategy = factory.fromClassLoader(classLoader);
    assertThat(restartStrategy).isInstanceOf(ComplexRestartStrategy.class)
      .extracting("classLoader")
      .isSameAs(classLoader);
  }

  @Test
  void shouldThrowExceptionForNonStrategyClass() {
    RestartStrategyFactory factory = builder().withStrategy(RestartStrategyFactoryTest.class).create();
    assertThrows(RestartNotSupportedException.class, () -> factory.fromClassLoader(classLoader));
  }

  @Test
  void shouldReturnEmpty() {
    RestartStrategyFactory factory = builder().withStrategy(RestartStrategyFactory.STRATEGY_NONE).create();
    assertThat(factory.fromClassLoader(classLoader)).isNull();
  }

  @Test
  void shouldReturnEmptyForUnknownOs() {
    RestartStrategyFactory factory = builder().withOs("hitchhiker-os").create();
    assertThat(factory.fromClassLoader(classLoader)).isNull();
  }

  @Test
  void shouldReturnExitRestartStrategy() {
    RestartStrategyFactory factory = builder().withStrategy(ExitRestartStrategy.NAME).create();
    assertThat(factory.fromClassLoader(classLoader)).isInstanceOf(ExitRestartStrategy.class);
  }

  @ParameterizedTest
  @ValueSource(strings = { "linux", "darwin", "solaris", "freebsd", "openbsd" })
  void shouldReturnPosixRestartStrategyForPosixBased(String os) {
    RestartStrategyFactory factory = builder().withOs(os).create();
    assertThat(factory.fromClassLoader(classLoader)).isInstanceOf(PosixRestartStrategy.class);
  }

  @Test
  void shouldReturnWinSWRestartStrategy(@TempDir Path tempDir) throws IOException {
    File exe = tempDir.resolve("winsw.exe").toFile();
    exe.createNewFile();

    RestartStrategyFactory factory = builder()
      .withOs("windows")
      .withEnvironment(WinSWRestartStrategy.ENV_EXECUTABLE, exe.getAbsolutePath())
      .create();
    assertThat(factory.fromClassLoader(classLoader)).isInstanceOf(WinSWRestartStrategy.class);
  }

  public static class TestingRestartStrategy extends RestartStrategy {
    @Override
    protected void executeRestart(InjectionContext context) {
    }
  }

  public static class ComplexRestartStrategy extends RestartStrategy {

    private final ClassLoader classLoader;

    public ComplexRestartStrategy(ClassLoader classLoader) {
      this.classLoader = classLoader;
    }

    @Override
    protected void executeRestart(InjectionContext context) {
    }
  }

  private static Builder builder() {
    WebappConfigProvider.setConfigBindings(Collections.emptyMap());
    return new Builder();
  }

  private static class Builder {

    private final Map<String, String> environment = new HashMap<>();
    private Platform platform = new Platform("Linux", "64Bit", "x64");

    public Builder withStrategy(Class<?> strategy) {
      return withStrategy(strategy.getName());
    }

    public Builder withStrategy(String strategy) {
      return withConfig(strategy);
    }

    private Builder withConfig(String strategy) {
      WebappConfigProvider.setConfigBindings(Map.of(RestartStrategyFactory.RESTART_STRATEGY, strategy));
      return this;
    }

    public Builder withEnvironment(String key, String value) {
      environment.put(key, value);
      return this;
    }

    public Builder withOs(String os) {
      platform = new Platform(os, "64Bit", "x64");
      return this;
    }

    public RestartStrategyFactory create() {
      return new RestartStrategyFactory(platform, environment);
    }
  }
}
