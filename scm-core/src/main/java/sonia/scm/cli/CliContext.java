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
   * @return locale of the client terminal
   */
  Locale getLocale();

  /**
   * Returns the client.
   * @return client
   */
  Client getClient();
}
