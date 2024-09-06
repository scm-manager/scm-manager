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

package sonia.scm.web.cgi;

import java.io.File;
import java.util.Collections;
import java.util.List;


public abstract class AbstractCGIExecutor implements CGIExecutor
{
  protected List<String> args = Collections.emptyList();

  protected int bufferSize;

  protected EnvList environment;

  protected CGIExceptionHandler exceptionHandler;

  protected boolean ignoreExitCode = false;

  protected String interpreter;

  protected boolean passShellEnvironment = false;

  protected CGIStatusCodeHandler statusCodeHandler;

  protected File workDirectory;

  @Override
  public int getBufferSize()
  {
    return bufferSize;
  }


  @Override
  public EnvList getEnvironment()
  {
    return environment;
  }

  /**
   * @since 1.15
   */
  @Override
  public CGIExceptionHandler getExceptionHandler()
  {
    return exceptionHandler;
  }


  @Override
  public String getInterpreter()
  {
    return interpreter;
  }

  /**
   * @since 1.15
   */
  @Override
  public CGIStatusCodeHandler getStatusCodeHandler()
  {
    return statusCodeHandler;
  }


  @Override
  public File getWorkDirectory()
  {
    return workDirectory;
  }


  @Override
  public boolean isIgnoreExitCode()
  {
    return ignoreExitCode;
  }


  @Override
  public boolean isPassShellEnvironment()
  {
    return passShellEnvironment;
  }

  @Override
  public void setBufferSize(int bufferSize)
  {
    this.bufferSize = bufferSize;
  }

  @Override
  public void setEnvironment(EnvList environment)
  {
    this.environment = environment;
  }

  /**
   * @since 1.15
   */
  @Override
  public void setExceptionHandler(CGIExceptionHandler exceptionHandler)
  {
    this.exceptionHandler = exceptionHandler;
  }

  @Override
  public void setIgnoreExitCode(boolean ignoreExitCode)
  {
    this.ignoreExitCode = ignoreExitCode;
  }

  @Override
  public void setInterpreter(String interpreter)
  {
    this.interpreter = interpreter;
  }

  @Override
  public void setPassShellEnvironment(boolean passShellEnvironment)
  {
    this.passShellEnvironment = passShellEnvironment;
  }

  /**
   * @since 1.15
   */
  @Override
  public void setStatusCodeHandler(CGIStatusCodeHandler statusCodeHandler)
  {
    this.statusCodeHandler = statusCodeHandler;
  }

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

}
