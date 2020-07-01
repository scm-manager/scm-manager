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
import org.assertj.core.util.Strings;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import sonia.scm.repository.HgConfig;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class UnixAutoConfiguratorTest {

  @Test
  void shouldConfigureWithShebangPath(@TempDir Path directory) throws IOException {
    Path hg = directory.resolve("hg");
    Path python = directory.resolve("python");

    Files.write(hg, ("#!" + python.toAbsolutePath().toString()).getBytes(StandardCharsets.UTF_8));
    Files.createFile(python);

    UnixAutoConfigurator configurator = create(directory);
    HgConfig config = configurator.configure();

    assertThat(config.getHgBinary()).isEqualTo(hg.toString());
    assertThat(config.getPythonBinary()).isEqualTo(python.toString());
  }

  private UnixAutoConfigurator create(@TempDir Path directory) {
    return new UnixAutoConfigurator(createEnv(directory), Collections.emptyList());
  }

  private Map<String, String> createEnv(Path... paths) {
    return ImmutableMap.of("PATH", Joiner.on(File.pathSeparator).join(paths));
  }

  @Test
  void shouldConfigureWithShebangEnv(@TempDir Path directory) throws IOException {
    Path hg = directory.resolve("hg");
    Path python = directory.resolve("python3.8");

    Files.write(hg, "#!/usr/bin/env python3.8".getBytes(StandardCharsets.UTF_8));
    Files.createFile(python);

    UnixAutoConfigurator configurator = create(directory);
    HgConfig config = configurator.configure();

    assertThat(config.getHgBinary()).isEqualTo(hg.toString());
    assertThat(config.getPythonBinary()).isEqualTo(python.toString());
  }

  @Test
  void shouldConfigureWithoutShebang(@TempDir Path directory) throws IOException {
    Path hg = directory.resolve("hg");
    Path python = directory.resolve("python");

    Files.createFile(hg);
    Files.createFile(python);

    UnixAutoConfigurator configurator = create(directory);
    HgConfig config = configurator.configure();

    assertThat(config.getHgBinary()).isEqualTo(hg.toString());
    assertThat(config.getPythonBinary()).isEqualTo(python.toString());
  }

  @Test
  void shouldConfigureWithoutShebangButWithPython3(@TempDir Path directory) throws IOException {
    Path hg = directory.resolve("hg");
    Path python = directory.resolve("python3");

    Files.createFile(hg);
    Files.createFile(python);

    UnixAutoConfigurator configurator = create(directory);
    HgConfig config = configurator.configure();

    assertThat(config.getHgBinary()).isEqualTo(hg.toString());
    assertThat(config.getPythonBinary()).isEqualTo(python.toString());
  }

  @Test
  void shouldConfigureFindPythonInAdditionalPath(@TempDir Path directory) throws IOException {
    Path def = directory.resolve("default");
    Files.createDirectory(def);
    Path additional = directory.resolve("additional");
    Files.createDirectory(additional);

    Path hg = def.resolve("hg");
    Path python = additional.resolve("python");

    Files.createFile(hg);
    Files.createFile(python);

    UnixAutoConfigurator configurator = new UnixAutoConfigurator(
      createEnv(def), ImmutableList.of(additional.toAbsolutePath().toString())
    );

    HgConfig config = configurator.configure();
    assertThat(config.getHgBinary()).isEqualTo(hg.toString());
    assertThat(config.getPythonBinary()).isEqualTo(python.toString());
  }

  @Test
  void shouldFindModulePathFromDebuginstallOutput(@TempDir Path directory) throws IOException {
    Path hg = directory.resolve("hg");
    Files.createFile(hg);
    hg.toFile().setExecutable(true);

    Path modules = directory.resolve("modules");
    Files.createDirectories(modules);

    Path mercurialModule = modules.resolve("mercurial");
    Files.createDirectories(mercurialModule);

    UnixAutoConfigurator configurator = create(directory);
    configurator.setExecutor((Path binary, String... args) -> {
      String content = String.join("\n",
        "checking Python executable (/python3.8)",
        "checking Python lib (/python3.8)...",
        "checking installed modules (" + mercurialModule.toString() + ")...",
        "checking templates (/mercurial/templates)...",
        "checking default template (/mercurial/templates/map-cmdline.default))"
      );
      return new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
    });
    HgConfig config = configurator.configure();

    assertThat(config.getHgBinary()).isEqualTo(hg.toString());
    assertThat(config.getPythonPath()).isEqualTo(modules.toString());
  }

}
