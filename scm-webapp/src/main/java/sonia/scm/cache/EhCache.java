/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */



package sonia.scm.cache;

//~--- non-JDK imports --------------------------------------------------------

import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;

//~--- JDK imports ------------------------------------------------------------

import java.util.Collection;

/**
 *
 * @author Sebastian Sdorra
 *
 * @param <K>
 * @param <V>
 */
public class EhCache<K, V> implements ExtendedCache<K, V>
{

  /**
   * Constructs ...
   *
   *
   * @param cache
   */
  public EhCache(Cache cache)
  {
    this.cache = cache;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   */
  @Override
  public void clear()
  {
    cache.removeAll();
  }

  /**
   * Method description
   *
   *
   * @param key
   * @param value
   */
  @Override
  public void put(K key, V value)
  {
    cache.put(new Element(key, value));
  }

  /**
   * Method description
   *
   *
   * @param key
   * @param value
   */
  @Override
  public void putCollection(K key, Collection<V> value)
  {
    cache.put(new Element(key, value));
  }

  /**
   * Method description
   *
   *
   * @param key
   *
   * @return
   */
  @Override
  public boolean remove(K key)
  {
    return cache.remove(key);
  }

  /**
   * Method description
   *
   *
   * @param key
   *
   * @return
   */
  @Override
  public boolean removeCollection(K key)
  {
    return cache.remove(key);
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param key
   *
   * @return
   */
  @Override
  public V get(K key)
  {
    V value = null;
    Element el = cache.get(key);

    if (el != null)
    {
      value = (V) el.getObjectValue();
    }

    return value;
  }

  /**
   * Method description
   *
   *
   * @param key
   *
   * @return
   */
  @Override
  public Collection<V> getCollection(K key)
  {
    Collection<V> value = null;
    Element el = cache.get(key);

    if (el != null)
    {
      value = (Collection<V>) el.getObjectValue();
    }

    return value;
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private Cache cache;
}
