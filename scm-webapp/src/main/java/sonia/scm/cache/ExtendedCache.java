/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */



package sonia.scm.cache;

//~--- JDK imports ------------------------------------------------------------

import java.util.Collection;

/**
 *
 * @author Sebastian Sdorra
 *
 * @param <K>
 * @param <V>
 */
public interface ExtendedCache<K, V> extends SimpleCache<K, V>
{

  /**
   * Method description
   *
   *
   * @param key
   * @param value
   */
  public void putCollection(K key, Collection<V> value);

  /**
   * Method description
   *
   *
   * @param key
   *
   * @return
   */
  public boolean removeCollection(K key);

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param key
   *
   * @return
   */
  public Collection<V> getCollection(K key);
}
