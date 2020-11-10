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

package sonia.scm.web.cgi;

//~--- JDK imports ------------------------------------------------------------

import java.io.File;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author Sebastian Sdorra
 */
public abstract class AbstractCGIExecutor implements CGIExecutor
{

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  public int getBufferSize()
  {
    return bufferSize;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  public EnvList getEnvironment()
  {
    return environment;
  }

  /**
   * {@inheritDoc}
   *
   *
   * @return
   * @since 1.15
   */
  @Override
  public CGIExceptionHandler getExceptionHandler()
  {
    return exceptionHandler;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  public String getInterpreter()
  {
    return interpreter;
  }

  /**
   * {@inheritDoc}
   *
   *
   * @return
   * @since 1.15
   */
  @Override
  public CGIStatusCodeHandler getStatusCodeHandler()
  {
    return statusCodeHandler;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  public File getWorkDirectory()
  {
    return workDirectory;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  public boolean isIgnoreExitCode()
  {
    return ignoreExitCode;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  public boolean isPassShellEnvironment()
  {
    return passShellEnvironment;
  }

  //~--- set methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param bufferSize
   */
  @Override
  public void setBufferSize(int bufferSize)
  {
    this.bufferSize = bufferSize;
  }

  /**
   * Method description
   *
   *
   * @param environment
   */
  @Override
  public void setEnvironment(EnvList environment)
  {
    this.environment = environment;
  }

  /**
   * {@inheritDoc}
   *
   *
   * @param exceptionHandler
   * @since 1.15
   */
  @Override
  public void setExceptionHandler(CGIExceptionHandler exceptionHandler)
  {
    this.exceptionHandler = exceptionHandler;
  }

  /**
   * Method description
   *
   *
   * @param ignoreExitCode
   */
  @Override
  public void setIgnoreExitCode(boolean ignoreExitCode)
  {
    this.ignoreExitCode = ignoreExitCode;
  }

  /**
   * Method description
   *
   *
   * @param interpreter
   */
  @Override
  public void setInterpreter(String interpreter)
  {
    this.interpreter = interpreter;
  }

  /**
   * Method description
   *
   *
   * @param passShellEnvironment
   */
  @Override
  public void setPassShellEnvironment(boolean passShellEnvironment)
  {
    this.passShellEnvironment = passShellEnvironment;
  }

  /**
   * {@inheritDoc}
   *
   *
   * @param statusCodeHandler
   * @since 1.15
   */
  @Override
  public void setStatusCodeHandler(CGIStatusCodeHandler statusCodeHandler)
  {
    this.statusCodeHandler = statusCodeHandler;
  }

  /**
   * Method description
   *
   *
   * @param workDirectory
   */
  @Override
  public void setWorkDirectory(File workDirectory)
  {
    this.workDirectory = workDirectory;
  }

  @Override
  public void setArgs(List<String> args) {
    this.args = args;
  }

  @Override
  public List<String> getArgs() {
    return args;
  }

  //~--- fields ---------------------------------------------------------------

  protected List<String> args = Collections.emptyList();

  /** Field description */
  protected int bufferSize;

  /** Field description */
  protected EnvList environment;

  /** Field description */
  protected CGIExceptionHandler exceptionHandler;

  /** Field description */
  protected boolean ignoreExitCode = false;

  /** Field description */
  protected String interpreter;

  /** Field description */
  protected boolean passShellEnvironment = false;

  /** Field description */
  protected CGIStatusCodeHandler statusCodeHandler;

  /** Field description */
  protected File workDirectory;
}
