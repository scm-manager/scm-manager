/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */



package sonia.scm;

//~--- JDK imports ------------------------------------------------------------

import java.io.Closeable;
import java.io.File;

/**
 *
 * @author Sebastian Sdorra
 */
public interface SCMContextProvider extends Closeable
{

  /**
   * Method description
   *
   */
  public void init();

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  public File getBaseDirectory();
}
