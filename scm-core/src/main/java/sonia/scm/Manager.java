/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */



package sonia.scm;

//~--- JDK imports ------------------------------------------------------------

import java.io.Closeable;
import java.io.IOException;

import java.util.Collection;

/**
 *
 * @author Sebastian Sdorra
 *
 * @param <T>
 * @param <E>
 */
public interface Manager<T, E extends Exception> extends Initable, Closeable
{

  /**
   * Method description
   *
   *
   * @param object
   *
   * @throws E
   * @throws IOException
   */
  public void create(T object) throws E, IOException;

  /**
   * Method description
   *
   *
   * @param object
   *
   * @throws E
   * @throws IOException
   */
  public void delete(T object) throws E, IOException;

  /**
   * Method description
   *
   *
   * @param object
   *
   * @throws E
   * @throws IOException
   */
  public void modify(T object) throws E, IOException;

  /**
   * Method description
   *
   *
   * @param object
   *
   * @throws E
   * @throws IOException
   */
  public void refresh(T object) throws E, IOException;

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param name
   *
   * @return
   */
  public T get(String name);

  /**
   * Method description
   *
   *
   * @return
   */
  public Collection<T> getAll();
}
