/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */



package sonia.scm.server.jetty;

//~--- non-JDK imports --------------------------------------------------------

import org.eclipse.jetty.util.component.LifeCycle;

import sonia.scm.server.ServerListener;

/**
 *
 * @author Sebastian Sdorra
 */
public class JettyServerListenerAdapter implements LifeCycle.Listener
{

  /**
   * Constructs ...
   *
   *
   * @param listener
   */
  public JettyServerListenerAdapter(ServerListener listener)
  {
    this.listener = listener;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param lc
   * @param throwable
   */
  @Override
  public void lifeCycleFailure(LifeCycle lc, Throwable throwable)
  {
    listener.failed(throwable);
  }

  /**
   * Method description
   *
   *
   * @param lc
   */
  @Override
  public void lifeCycleStarted(LifeCycle lc)
  {
    listener.started();
  }

  /**
   * Method description
   *
   *
   * @param lc
   */
  @Override
  public void lifeCycleStarting(LifeCycle lc)
  {

    // do nothing
  }

  /**
   * Method description
   *
   *
   * @param lc
   */
  @Override
  public void lifeCycleStopped(LifeCycle lc)
  {
    listener.stopped();
  }

  /**
   * Method description
   *
   *
   * @param lc
   */
  @Override
  public void lifeCycleStopping(LifeCycle lc)
  {

    // do nothing
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private ServerListener listener;
}
