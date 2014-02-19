/**
 * Copyright (c) 2010, Sebastian Sdorra
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 3. Neither the name of SCM-Manager; nor the names of its
 *    contributors may be used to endorse or promote products derived from this
 *    software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * http://bitbucket.org/sdorra/scm-manager
 *
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

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private final Map<K, V> map = Maps.newHashMap();
}
