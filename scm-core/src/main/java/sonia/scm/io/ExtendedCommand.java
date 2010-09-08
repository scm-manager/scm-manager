/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */



package sonia.scm.io;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;

import java.util.Timer;

/**
 *
 * @author Sebastian Sdorra
 */
public class ExtendedCommand extends SimpleCommand
{

  /**
   * Constructs ...
   *
   *
   * @param command
   */
  public ExtendedCommand(String... command)
  {
    super(command);
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
    SimpleCommandResult result = null;
    Process process = createProcess();
    Timer timer = new Timer();
    ProcessInterruptScheduler pis = null;

    try
    {
      pis = new ProcessInterruptScheduler(process);
      timer.schedule(pis, timeout);
      result = getResult(process);
    }
    finally
    {
      timer.cancel();
    }

    return result;
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  public long getTimeout()
  {
    return timeout;
  }

  //~--- set methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param timeout
   */
  public void setTimeout(long timeout)
  {
    this.timeout = timeout;
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private long timeout = 30000;
}
