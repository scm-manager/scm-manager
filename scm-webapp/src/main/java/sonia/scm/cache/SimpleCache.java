/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */



package sonia.scm.cache;

/**
 *
 * @author Sebastian Sdorra
 *
 * @param <K>
 * @param <V>
 */
public interface SimpleCache<K, V>
{

  /**
   * Method description
   *
   */
  public void clear();

  /**
   * Method description
   *
   *
   * @param key
   * @param value
   */
  public void put(K key, V value);

  /**
   * Method description
   *
   *
   * @param key
   *
   * @return
   */
  public boolean remove(K key);

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param key
   *
   * @return
   */
  public V get(K key);
}
