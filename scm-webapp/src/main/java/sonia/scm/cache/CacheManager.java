/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */



package sonia.scm.cache;

/**
 *
 * @author Sebastian Sdorra
 */
public interface CacheManager
{

  /**
   * Method description
   *
   *
   * @param key
   * @param value
   * @param name
   * @param <K>
   * @param <V>
   *
   * @return
   */
  public <K, V> ExtendedCache<K, V> getExtendedCache(Class<K> key,
          Class<V> value, String name);

  /**
   * Method description
   *
   *
   * @param key
   * @param value
   * @param name
   * @param <K>
   * @param <V>
   *
   * @return
   */
  public <K, V> SimpleCache<K, V> getSimpleCache(Class<K> key, Class<V> value,
          String name);
}
