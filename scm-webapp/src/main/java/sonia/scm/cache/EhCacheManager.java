/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */



package sonia.scm.cache;

//~--- non-JDK imports --------------------------------------------------------

import com.google.inject.Singleton;

/**
 *
 * @author Sebastian Sdorra
 */
@Singleton
public class EhCacheManager implements CacheManager
{

  /** Field description */
  public static final String CONFIG = "/config/ehcache.xml";

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   */
  public EhCacheManager()
  {
    cacheManager =
      new net.sf.ehcache.CacheManager(EhCacheManager.class.getResource(CONFIG));
  }

  //~--- get methods ----------------------------------------------------------

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
  @Override
  public <K, V> ExtendedCache<K, V> getExtendedCache(Class<K> key,
          Class<V> value, String name)
  {
    return getCache(name);
  }

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
  @Override
  public <K, V> SimpleCache<K, V> getSimpleCache(Class<K> key, Class<V> value,
          String name)
  {
    return getCache(name);
  }

  /**
   * Method description
   *
   *
   * @param name
   * @param <K>
   * @param <V>
   *
   * @return
   */
  private <K, V> EhCache<K, V> getCache(String name)
  {
    return new EhCache<K, V>(cacheManager.getCache(name));
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private net.sf.ehcache.CacheManager cacheManager;
}
