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

import com.google.common.annotations.VisibleForTesting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.IntConsumer;

/**
 * {@link RestartStrategy} which tears down the scm-manager context and
 * then exists the java process with {@link System#exit(int)}.
 * <p>
 * This is useful if an external mechanism is able to restart the process after it has exited.
 */
class ExitRestartStrategy extends RestartStrategy {

  private static final Logger LOG = LoggerFactory.getLogger(ExitRestartStrategy.class);

  static final String NAME = "exit";

  static final String PROPERTY_EXIT_CODE = "sonia.scm.restart.exit-code";

  private IntConsumer exiter = System::exit;

  private int exitCode;

  ExitRestartStrategy() {
  }

  @VisibleForTesting
  void setExiter(IntConsumer exiter) {
    this.exiter = exiter;
  }

  @Override
  public void prepareRestart(InjectionContext context) {
    exitCode = determineExitCode();
  }

  @Override
  protected void executeRestart(InjectionContext context) {
    LOG.warn("exit scm-manager with exit code {}", exitCode);
    exiter.accept(exitCode);
  }

  private int determineExitCode() {
    String exitCodeAsString = System.getProperty(PROPERTY_EXIT_CODE, "0");
    try {
      return Integer.parseInt(exitCodeAsString);
    } catch (NumberFormatException ex) {
      throw new RestartNotSupportedException("invalid exit code " + exitCodeAsString, ex);
    }
  }
}
