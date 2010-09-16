/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */



package sonia.scm;

//~--- non-JDK imports --------------------------------------------------------

import sonia.scm.util.ServiceUtil;

//~--- JDK imports ------------------------------------------------------------

import java.util.List;

/**
 *
 * @author Sebastian Sdorra
 */
public class ScmWebPluginContext
{

  /**
   * Constructs ...
   *
   */
  public ScmWebPluginContext()
  {
    plugins = ServiceUtil.getServices(ScmWebPlugin.class);
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  public List<ScmWebPlugin> getPlugins()
  {
    return plugins;
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private List<ScmWebPlugin> plugins;
}
