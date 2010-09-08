/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */



package sonia.scm.io;

//~--- non-JDK imports --------------------------------------------------------

import sonia.scm.util.Util;

//~--- JDK imports ------------------------------------------------------------

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Sebastian Sdorra
 */
public class SimpleCommand implements Command
{

  /** Field description */
  private static final Logger logger =
    Logger.getLogger(SimpleCommand.class.getName());

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
    ProcessBuilder processBuilder = new ProcessBuilder(command);

    processBuilder.redirectErrorStream(true);

    return processBuilder.start();
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
    InputStream input = null;

    try
    {
      String s = System.getProperty("line.separator");
      StringBuilder content = new StringBuilder();

      input = process.getInputStream();

      BufferedReader reader = new BufferedReader(new InputStreamReader(input));
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

      result = new SimpleCommandResult(content.toString(), returnCode);
    }
    catch (InterruptedException ex)
    {
      logger.log(Level.SEVERE, null, ex);

      throw new IOException(ex.getMessage());
    }
    finally
    {
      Util.close(input);
    }

    return result;
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private String[] command;
}
