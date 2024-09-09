/*
 * Copyright (c) 2020 - present Cloudogu GmbH
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

package sonia.scm.repository.spi;

import com.google.common.io.ByteStreams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.SCMContext;
import sonia.scm.repository.HgGlobalConfig;
import sonia.scm.repository.HgExtensions;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class HgVersionCommand {

  private static final Logger LOG = LoggerFactory.getLogger(HgVersionCommand.class);
  public static final String UNKNOWN = "python/x.y.z mercurial/x.y.z";

  private final HgGlobalConfig config;
  private final String extension;
  private final ProcessExecutor executor;

  public HgVersionCommand(HgGlobalConfig config) {
    this(config, extension(), command -> new ProcessBuilder(command).start());
  }

  HgVersionCommand(HgGlobalConfig config, String extension, ProcessExecutor executor) {
    this.config = config;
    this.extension = extension;
    this.executor = executor;
  }

  private static String extension() {
    return HgExtensions.VERSION.getFile(SCMContext.getContext()).getAbsolutePath();
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
