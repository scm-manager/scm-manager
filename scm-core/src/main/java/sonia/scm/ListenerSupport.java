/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */



package sonia.scm;

/**
 *
 * @author Sebastian Sdorra
 *
 * @param <T>
 */
public interface ListenerSupport<T>
{

  /**
   * Method description
   *
   *
   * @param listener
   */
  public void addListener(T listener);

  /**
   * Method description
   *
   *
   * @param listener
   */
  public void removeListener(T listener);
}
