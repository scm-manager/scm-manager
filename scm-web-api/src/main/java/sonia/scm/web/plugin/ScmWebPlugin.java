/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */



package sonia.scm.web.plugin;

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
   * @param context
   */
  public void contextDestroyed(ScmWebPluginContext context);

  /**
   * Method description
   *
   *
   * @param context
   */
  public void contextInitialized(ScmWebPluginContext context);
}
