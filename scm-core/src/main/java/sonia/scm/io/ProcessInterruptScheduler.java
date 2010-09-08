/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */



package sonia.scm.io;

//~--- JDK imports ------------------------------------------------------------

import java.util.TimerTask;

/**
 *
 * @author Sebastian Sdorra
 */
public class ProcessInterruptScheduler extends TimerTask
{

  /**
   * Constructs ...
   *
   *
   * @param process
   */
  public ProcessInterruptScheduler(Process process)
  {
    this.process = process;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   */
  @Override
  public void run()
  {
    process.destroy();
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private Process process;
}
