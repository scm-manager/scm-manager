/**
 * MIT License
 *
 * Copyright (c) 2020-present Cloudogu GmbH and Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package sonia.scm.cache;

//~--- non-JDK imports --------------------------------------------------------

import com.google.common.base.Predicate;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

//~--- JDK imports ------------------------------------------------------------

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Sebastian Sdorra
 * @since 1.17
 *
 * @param <K>
 * @param <V>
 */
public class MapCache<K, V>
  implements Cache<K, V>, org.apache.shiro.cache.Cache<K, V>
{

  /**
   * Method description
   *
   */
  @Override
  public void clear()
  {
    map.clear();
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
  public boolean contains(K key)
  {
    return map.containsKey(key);
  }

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  public Set<K> keys()
  {
    return Collections.unmodifiableSet(map.keySet());
  }

  /**
   * Method description
   *
   *
   * @param key
   * @param value
   *
   * @return
   */
  @Override
  public V put(K key, V value)
  {
    return map.put(key, value);
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
  public V remove(K key)
  {
    return map.remove(key);
  }

  /**
   * Method description
   *
   *
   * @param filter
   *
   * @return
   */
  @Override
  public Iterable<V> removeAll(Predicate<K> filter)
  {
    Set<V> values = Sets.newHashSet();

    for (K key : map.keySet())
    {
      if (filter.apply(key))
      {
        values.add(remove(key));
      }
    }

    return values;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  public int size()
  {
    return map.size();
  }

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  public Collection<V> values()
  {
    return Collections.unmodifiableCollection(map.values());
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
    return map.get(key);
  }

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  public CacheStatistics getStatistics()
  {
    return null;
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private final Map<K, V> map = Maps.newHashMap();
}
