/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */



package sonia.scm;

//~--- JDK imports ------------------------------------------------------------

import java.io.InputStream;

/**
 *
 * @author Sebastian Sdorra
 */
public class HgPlugin extends ScmWebPluginAdapter
{

  /** Field description */
  public static final String SCRIPT = "/sonia/scm/hg.config.js";

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  public InputStream getScript()
  {
    return HgPlugin.class.getResourceAsStream(SCRIPT);
  }
}
