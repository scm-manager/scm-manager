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

package sonia.scm.io;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sonia.scm.util.IOUtil;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

import java.util.Map;


public class SimpleCommand implements Command
{
  private String[] command;

  private Map<String, String> environment;

  private boolean useSystemEnvironment = false;

  private File workDirectory;

  private static final Logger logger =
    LoggerFactory.getLogger(SimpleCommand.class);

  public SimpleCommand(String... command)
  {
    this(null, command);
  }

  /**
   * @since 1.8
   */
  public SimpleCommand(Map<String, String> environment, String... command)
  {
    this.environment = environment;
    this.command = command;
  }

  @Override
  public SimpleCommandResult execute() throws IOException
  {
    Process process = createProcess();

    return getResult(process);
  }


  /**
   * @since 1.23
   */
  public boolean isUseSystemEnvironment()
  {
    return useSystemEnvironment;
  }


  /**
   * @since 1.8
   */
  public void setEnvironment(Map<String, String> environment)
  {
    this.environment = environment;
  }

  /**
   * @since 1.23
   */
  public void setUseSystemEnvironment(boolean useSystemEnvironment)
  {
    this.useSystemEnvironment = useSystemEnvironment;
  }

  @Override
  public void setWorkDirectory(File workDirectory)
  {
    this.workDirectory = workDirectory;
  }

  protected Process createProcess() throws IOException
  {
    if (logger.isDebugEnabled())
    {
      StringBuilder cmd = new StringBuilder();

      for (String c : command)
      {
        cmd.append(c).append(" ");
      }

      logger.debug("start external process '{}'", cmd.toString());
    }

    ProcessBuilder processBuilder = new ProcessBuilder(command);

    if (workDirectory != null)
    {
      processBuilder = processBuilder.directory(workDirectory);
    }

    Map<String,String> env = processBuilder.environment();
    if ( useSystemEnvironment )
    {
      env.putAll(System.getenv());
    }
    
    if (environment != null)
    {
      env.putAll(environment);
    }

    return processBuilder.redirectErrorStream(true).start();
  }

  protected SimpleCommandResult getResult(Process process) throws IOException
  {
    SimpleCommandResult result = null;
    BufferedReader reader = null;

    try
    {
      String s = System.getProperty("line.separator");
      StringBuilder content = new StringBuilder();

      reader =
        new BufferedReader(new InputStreamReader(process.getInputStream()));

      String line = reader.readLine();

      while (line != null)
      {
        content.append(line);
        line = reader.readLine();

        if (line != null)
        {
          content.append(s);
        }
      }

      int returnCode = process.waitFor();

      if (logger.isDebugEnabled())
      {
        logger.debug("command returned with exitcode {}", returnCode);

        if (logger.isTraceEnabled())
        {
          logger.trace("command content: {}{}", s, content.toString());
        }
      }

      result = new SimpleCommandResult(content.toString(), returnCode);
    }
    catch (InterruptedException ex)
    {
      logger.error(ex.getMessage(), ex);

      throw new IOException(ex.getMessage());
    }
    finally
    {
      IOUtil.close(reader);
    }

    return result;
  }

}
