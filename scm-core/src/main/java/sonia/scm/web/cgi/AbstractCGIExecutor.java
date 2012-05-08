/**
 * Copyright (c) 2010, Sebastian Sdorra
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 3. Neither the name of SCM-Manager; nor the names of its
 *    contributors may be used to endorse or promote products derived from this
 *    software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * http://bitbucket.org/sdorra/scm-manager
 *
 */



package sonia.scm.web.cgi;

//~--- JDK imports ------------------------------------------------------------

import java.io.File;

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

  //~--- fields ---------------------------------------------------------------

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
