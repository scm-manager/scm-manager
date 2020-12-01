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

import com.google.common.annotations.VisibleForTesting;
import com.google.common.io.ByteStreams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.repository.HgConfig;
import sonia.scm.repository.HgVersion;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class HgVersionCommand {

  private static final Logger LOG = LoggerFactory.getLogger(HgVersionCommand.class);

  @VisibleForTesting
  static final String[] HG_ARGS = {
    "version", "--template", "{ver}"
  };

  @VisibleForTesting
  static final String[] PYTHON_ARGS = {
    "-c", "import sys; print(sys.version)"
  };

  private final HgConfig config;
  private final ProcessExecutor executor;

  public HgVersionCommand(HgConfig config) {
    this(config, command -> new ProcessBuilder(command).start());
  }

  HgVersionCommand(HgConfig config, ProcessExecutor executor) {
    this.config = config;
    this.executor = executor;
  }

  public HgVersion get() {
    return new HgVersion(getHgVersion(), getPythonVersion());
  }

  @Nonnull
  private String getPythonVersion() {
    try {
      String content = exec(config.getPythonBinary(), PYTHON_ARGS);
      int index = content.indexOf(' ');
      if (index > 0) {
        return content.substring(0, index);
      }
    } catch (IOException ex) {
      LOG.warn("failed to get python version", ex);
    } catch (InterruptedException ex) {
      LOG.warn("failed to get python version", ex);
      Thread.currentThread().interrupt();
    }
    return HgVersion.UNKNOWN;
  }

  @Nonnull
  private String getHgVersion() {
    try {
      return exec(config.getHgBinary(), HG_ARGS).trim();
    } catch (IOException ex) {
      LOG.warn("failed to get mercurial version", ex);
    } catch (InterruptedException ex) {
      LOG.warn("failed to get mercurial version", ex);
      Thread.currentThread().interrupt();
    }
    return HgVersion.UNKNOWN;
  }

  @SuppressWarnings("UnstableApiUsage")
  private String exec(String command, String[] args) throws IOException, InterruptedException {
    List<String> cmd = new ArrayList<>();
    cmd.add(command);
    cmd.addAll(Arrays.asList(args));

    Process process = executor.execute(cmd);
    byte[] bytes = ByteStreams.toByteArray(process.getInputStream());
    int exitCode = process.waitFor();
    if (exitCode != 0) {
      throw new IOException("process ends with exit code " + exitCode);
    }
    return new String(bytes, StandardCharsets.UTF_8);
  }

  @FunctionalInterface
  interface ProcessExecutor {
    Process execute(List<String> command) throws IOException;
  }
}
