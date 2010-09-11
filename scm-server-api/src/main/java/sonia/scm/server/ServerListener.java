/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */



package sonia.scm.server;

/**
 *
 * @author Sebastian Sdorra
 */
public interface ServerListener
{

  /**
   * Method description
   *
   *
   * @param throwable
   */
  public void failed(Throwable throwable);

  /**
   * Method description
   *
   */
  public void started();

  /**
   * Method description
   *
   */
  public void stopped();
}
