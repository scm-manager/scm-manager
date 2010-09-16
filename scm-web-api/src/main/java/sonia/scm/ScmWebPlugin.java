/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */



package sonia.scm;

//~--- non-JDK imports --------------------------------------------------------

import com.google.inject.Module;

//~--- JDK imports ------------------------------------------------------------

import java.io.InputStream;

/**
 *
 * @author Sebastian Sdorra
 */
public interface ScmWebPlugin
{

  /**
   * Method description
   *
   *
   * @return
   */
  public Module[] getModules();

  /**
   * Method description
   *
   *
   * @return
   */
  public InputStream getScript();
}
