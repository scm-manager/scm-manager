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

package sonia.scm.cli;

import java.io.InputStream;
import java.io.PrintWriter;
import java.util.Locale;

/**
 * Context for the CLI client which is used by the CLI commands
 * @since 2.33.0
 */
public interface CliContext {
  /**
   * This is the {@link PrintWriter} which writes to the stdout channel of the client terminal.
   * Use this channel for "normal" messages, for errors use {@link CliContext#getStderr()}.
   * @return writer for stdout
   */
  PrintWriter getStdout();

  /**
   * This is the {@link PrintWriter} which writes to the stderr channel of the client terminal.
   * Use this channel for error messages, for "normal" messages use {@link CliContext#getStdout()}.
   * @return writer for stderr
   */
  PrintWriter getStderr();

  /**
   * Returns an {@link InputStream} which represents the stdin of the client terminal.
   * @return the stdin channel of the client terminal
   */
  InputStream getStdin();

  /**
   * Sets the exit code for the current command execution and stops the execution.
   * @param exitcode exit code which will be return to the client terminal
   */
  void exit(int exitcode);

  /**
   * Returns the {@link Locale} of the client terminal.
   */
  Locale getLocale();

  /**
   * Returns the client.
   */
  Client getClient();
}
