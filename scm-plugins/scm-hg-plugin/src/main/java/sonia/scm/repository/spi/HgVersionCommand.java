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

import com.google.common.io.ByteStreams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.SCMContext;
import sonia.scm.repository.HgConfig;
import sonia.scm.repository.HgPythonScript;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class HgVersionCommand {

  private static final Logger LOG = LoggerFactory.getLogger(HgVersionCommand.class);
  public static final String UNKNOWN = "python/x.y.z mercurial/x.y.z";

  private final HgConfig config;
  private final String extension;
  private final ProcessExecutor executor;

  public HgVersionCommand(HgConfig config) {
    this(config, extension(), command -> new ProcessBuilder(command).start());
  }

  HgVersionCommand(HgConfig config, String extension, ProcessExecutor executor) {
    this.config = config;
    this.extension = extension;
    this.executor = executor;
  }

  private static String extension() {
    return HgPythonScript.VERSION.getFile(SCMContext.getContext()).getAbsolutePath();
  }

  public String get() {
    List<String> command = createCommand();
    try {
      return exec(command).trim();
    } catch (IOException ex) {
      LOG.warn("failed to get mercurial version", ex);
    } catch (InterruptedException ex) {
      LOG.warn("failed to get mercurial version", ex);
      Thread.currentThread().interrupt();
    }
    return UNKNOWN;
  }

  private List<String> createCommand() {
    List<String> command = new ArrayList<>();
    command.add(config.getHgBinary());
    command.add("--config");
    command.add("extensions.scmversion=" + extension);
    command.add("scmversion");
    return command;
  }

  private String exec(List<String> command) throws IOException, InterruptedException {
    Process process = executor.execute(command);
    byte[] bytes = ByteStreams.toByteArray(process.getInputStream());
    int exitCode = process.waitFor();
    if (exitCode != 0) {
      throw new IOException("process ends with exit code " + exitCode);
    }
    return new String(bytes, StandardCharsets.UTF_8).trim();
  }

  @FunctionalInterface
  interface ProcessExecutor {
    Process execute(List<String> command) throws IOException;
  }
}
