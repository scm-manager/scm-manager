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

package sonia.scm.repository.spi;

import com.google.common.base.Joiner;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.repository.HgConfig;
import sonia.scm.repository.HgVersion;

import javax.annotation.Nonnull;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HgVersionCommandTest {

  private static final String PYTHON_OUTPUT = String.join("\n",
    "3.9.0 (default, Oct 27 2020, 14:15:17)",
    "[Clang 12.0.0 (clang-1200.0.32.21)]"
  );

  private Map<String, Process> outputs;

  @BeforeEach
  void setUpOutputs() {
    outputs = new HashMap<>();
  }

  @Test
  void shouldReturnHgVersion() throws InterruptedException {
    command("/usr/local/bin/hg", HgVersionCommand.HG_ARGS, "5.5.2", 0);
    command("/opt/python/bin/python", HgVersionCommand.PYTHON_ARGS, PYTHON_OUTPUT, 0);

    HgVersion hgVersion = getVersion("/usr/local/bin/hg", "/opt/python/bin/python");
    assertThat(hgVersion.getMercurial()).isEqualTo("5.5.2");
    assertThat(hgVersion.getPython()).isEqualTo("3.9.0");
  }

  @Test
  void shouldReturnUnknownMercurialVersionOnNonZeroExitCode() throws InterruptedException {
    command("hg", HgVersionCommand.HG_ARGS, "", 1);
    command("python", HgVersionCommand.PYTHON_ARGS, PYTHON_OUTPUT, 0);

    HgVersion hgVersion = getVersion("hg", "python");
    assertThat(hgVersion.getMercurial()).isEqualTo(HgVersion.UNKNOWN);
    assertThat(hgVersion.getPython()).isEqualTo("3.9.0");
  }

  @Test
  void shouldReturnUnknownPythonVersionOnNonZeroExitCode() throws InterruptedException {
    command("hg", HgVersionCommand.HG_ARGS, "4.4.2", 0);
    command("python", HgVersionCommand.PYTHON_ARGS, "", 1);

    HgVersion hgVersion = getVersion("hg", "python");
    assertThat(hgVersion.getMercurial()).isEqualTo("4.4.2");
    assertThat(hgVersion.getPython()).isEqualTo(HgVersion.UNKNOWN);
  }

  @Test
  void shouldReturnUnknownForInvalidPythonOutput() throws InterruptedException {
    command("hg", HgVersionCommand.HG_ARGS, "1.0.0", 0);
    command("python", HgVersionCommand.PYTHON_ARGS, "abcdef", 0);

    HgVersion hgVersion = getVersion("hg", "python");
    assertThat(hgVersion.getMercurial()).isEqualTo("1.0.0");
    assertThat(hgVersion.getPython()).isEqualTo(HgVersion.UNKNOWN);
  }

  @Test
  void shouldReturnUnknownForIOException() {
    HgVersionCommand command = new HgVersionCommand(new HgConfig(), cmd -> {
      throw new IOException("failed");
    });

    HgVersion hgVersion = command.get();
    assertThat(hgVersion.getMercurial()).isEqualTo(HgVersion.UNKNOWN);
    assertThat(hgVersion.getPython()).isEqualTo(HgVersion.UNKNOWN);
  }

  private Process command(String command, String[] args, String content, int exitValue) throws InterruptedException {
    Process process = mock(Process.class);
    when(process.getInputStream()).thenReturn(new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8)));
    when(process.waitFor()).thenReturn(exitValue);

    List<String> cmdLine = new ArrayList<>();
    cmdLine.add(command);
    cmdLine.addAll(Arrays.asList(args));

    outputs.put(Joiner.on(' ').join(cmdLine), process);

    return process;
  }

  @Nonnull
  private HgVersion getVersion(String hg, String python) {
    HgConfig config = new HgConfig();
    config.setHgBinary(hg);
    config.setPythonBinary(python);
    return new HgVersionCommand(config, command -> outputs.get(Joiner.on(' ').join(command))).get();
  }

}
