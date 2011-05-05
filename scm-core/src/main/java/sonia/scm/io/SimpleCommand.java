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



package sonia.scm.io;

//~--- non-JDK imports --------------------------------------------------------

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sonia.scm.util.IOUtil;

//~--- JDK imports ------------------------------------------------------------

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 *
 * @author Sebastian Sdorra
 */
public class SimpleCommand implements Command
{

  /** Field description */
  private static final Logger logger =
    LoggerFactory.getLogger(SimpleCommand.class);

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   *
   * @param command
   */
  public SimpleCommand(String... command)
  {
    this.command = command;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   *
   * @throws IOException
   */
  @Override
  public SimpleCommandResult execute() throws IOException
  {
    Process process = createProcess();

    return getResult(process);
  }

  //~--- set methods ----------------------------------------------------------

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

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   *
   * @throws IOException
   */
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

    return processBuilder.redirectErrorStream(true).start();
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param process
   *
   * @return
   *
   * @throws IOException
   */
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
      
      if ( logger.isDebugEnabled() )
      {
        logger.debug( "command returned with exitcode {}", returnCode );
        if ( logger.isTraceEnabled() )
        {
          logger.trace("command content: {}{}", s, content.toString() );
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

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private String[] command;

  /** Field description */
  private File workDirectory;
}
