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

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.repository.HgConfig;
import sonia.scm.repository.HgVerifier;

import java.io.File;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PosixAutoConfiguratorTest {

  @Mock
  private HgVerifier verifier;

  @Test
  void shouldConfigureMercurial(@TempDir Path directory) {
    Path hg = directory.resolve("hg");
    when(verifier.isValid(hg)).thenReturn(true);

    PosixAutoConfigurator configurator = create(directory);

    HgConfig config = new HgConfig();
    configurator.configure(config);

    assertThat(config.getHgBinary()).isEqualTo(hg.toString());
  }

  private PosixAutoConfigurator create(Path directory) {
    return new PosixAutoConfigurator(verifier, createEnv(directory), Collections.emptyList());
  }

  private Map<String, String> createEnv(Path... paths) {
    return ImmutableMap.of("PATH", Joiner.on(File.pathSeparator).join(paths));
  }


  @Test
  void shouldFindMercurialInAdditionalPath(@TempDir Path directory) {
    Path def = directory.resolve("default");
    Path additional = directory.resolve("additional");
    Path hg = def.resolve("hg");

    when(verifier.isValid(hg)).thenReturn(true);

    PosixAutoConfigurator configurator = new PosixAutoConfigurator(
      verifier, createEnv(def), ImmutableList.of(additional.toAbsolutePath().toString())
    );

    HgConfig config = new HgConfig();
    configurator.configure(config);

    assertThat(config.getHgBinary()).isEqualTo(hg.toString());
  }

  @Test
  void shouldSkipInvalidMercurialInstallations(@TempDir Path directory) {
    Path one = directory.resolve("one");
    Path two = directory.resolve("two");
    Path three = directory.resolve("three");
    Path hg = three.resolve("hg");

    when(verifier.isValid(any(Path.class))).then(ic -> {
      Path path = ic.getArgument(0, Path.class);
      return path.equals(hg);
    });

    PosixAutoConfigurator configurator = new PosixAutoConfigurator(
      verifier, createEnv(one), ImmutableList.of(
        two.toAbsolutePath().toString(),
        three.toAbsolutePath().toString()
      )
    );

    HgConfig config = new HgConfig();
    configurator.configure(config);

    assertThat(config.getHgBinary()).isEqualTo(hg.toString());
  }

  @Test
  void shouldNotConfigureMercurial(@TempDir Path directory) {
    PosixAutoConfigurator configurator = create(directory);

    HgConfig config = new HgConfig();
    configurator.configure(config);

    assertThat(config.getHgBinary()).isNull();
  }

}
