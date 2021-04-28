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

package sonia.scm.autoconfig;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.repository.HgGlobalConfig;
import sonia.scm.repository.HgVerifier;

import java.io.File;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static sonia.scm.autoconfig.WindowsAutoConfigurator.*;

@ExtendWith(MockitoExtension.class)
class WindowsAutoConfiguratorTest {

  @Mock
  private HgVerifier verifier;

  @Mock
  private WindowsRegistry registry;

  @ParameterizedTest
  @ValueSource(strings = { BINARY_HG_EXE, BINARY_HG_BAT })
  void shouldConfigureHgFromPath(String binary, @TempDir Path directory) {
    Path hgPath = directory.resolve(binary);
    mockIsValid(hgPath);

    String hg = configure(directory);
    assertThat(hg).isEqualTo(hgPath.toString());
  }

  @ParameterizedTest
  @ValueSource(strings = { BINARY_HG_EXE, BINARY_HG_BAT })
  void shouldConfigureHgFromSecondPath(String binary, @TempDir Path directory) {
    Path one = directory.resolve("one");
    Path two = directory.resolve("two");
    Path hgPath = two.resolve(binary);
    mockIsValid(hgPath);

    String hg = configure(one, two);
    assertThat(hg).isEqualTo(hgPath.toString());
  }

  @ParameterizedTest
  @ValueSource(strings = { REGISTRY_KEY_TORTOISE_HG, REGISTRY_KEY_MERCURIAL })
  void shouldConfigureHgFromHgInstallation(String registryKey, @TempDir Path directory) {
    Path one = directory.resolve("one");
    Path two = directory.resolve("two");
    Path hgPath = two.resolve(BINARY_HG_EXE);

    mockIsValid(hgPath);
    mockRegistryKey(registryKey, two.toString());

    String hg = configure(one);
    assertThat(hg).isEqualTo(hgPath.toString());
  }

  @Test
  void shouldNotConfigureMercurial(@TempDir Path directory) {
    String hg = configure(directory);
    assertThat(hg).isNull();
  }

  private void mockRegistryKey(String key, String value) {
    when(registry.get(anyString())).then(ic -> {
      String k = ic.getArgument(0, String.class);
      if (key.equals(k)) {
        return Optional.of(value);
      }
      return Optional.empty();
    });
  }

  private void mockIsValid(Path path) {
    when(verifier.verify(any(Path.class))).then(ic -> {
      Path p = ic.getArgument(0, Path.class);
      if (path.equals(p)) {
        return HgVerifier.HgVerifyStatus.VALID;
      }
      return HgVerifier.HgVerifyStatus.INVALID_VERSION;
    });
  }

  private String path(Path... paths) {
    return Arrays.stream(paths)
      .map(Path::toString)
      .collect(
        Collectors.joining(File.pathSeparator)
      );
  }

  private String configure(Path... paths) {
    return configure(path(paths));
  }

  private String configure(String path) {
    HgGlobalConfig config = new HgGlobalConfig();
    configurator(path).configure(config);
    return config.getHgBinary();
  }

  private WindowsAutoConfigurator configurator(String path) {
    Map<String, String> env = new HashMap<>();
    env.put(ENV_PATH, path);
    return new WindowsAutoConfigurator(verifier, registry, env);
  }

}
