/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */



package sonia.scm;

/**
 *
 * @author Sebastian Sdorra
 */
public interface SCMPlugin
{

  /**
   * Method description
   *
   *
   * @param context
   */
  public void contextDestroyed(SCMContextProvider context);

  /**
   * Method description
   *
   *
   * @param context
   */
  public void contextInitialized(SCMContextProvider context);
}
