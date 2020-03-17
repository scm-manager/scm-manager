/**
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

import java.util.Map;

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
    this(null, command);
  }

  /**
   * Constructs ...
   *
   *
   * @param environment
   * @param command
   * @since 1.8
   */
  public SimpleCommand(Map<String, String> environment, String... command)
  {
    this.environment = environment;
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

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   * 
   * @since 1.23
   */
  public boolean isUseSystemEnvironment()
  {
    return useSystemEnvironment;
  }

  //~--- set methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param environment
   * @since 1.8
   */
  public void setEnvironment(Map<String, String> environment)
  {
    this.environment = environment;
  }

  /**
   * Method description
   *
   *
   * @param useSystemEnvironment
   * 
   * @since 1.23
   */
  public void setUseSystemEnvironment(boolean useSystemEnvironment)
  {
    this.useSystemEnvironment = useSystemEnvironment;
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

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private String[] command;

  /** Field description */
  private Map<String, String> environment;

  /** Field description */
  private boolean useSystemEnvironment = false;

  /** Field description */
  private File workDirectory;
}
