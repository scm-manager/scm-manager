/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */



package sonia.scm.io;

//~--- JDK imports ------------------------------------------------------------

import java.io.File;
import java.io.IOException;

/**
 *
 * @author Sebastian Sdorra
 */
public interface Command
{

  /**
   * Method description
   *
   *
   * @return
   *
   * @throws IOException
   */
  public CommandResult execute() throws IOException;

  //~--- set methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param file
   */
  public void setWorkDirectory(File file);
}
