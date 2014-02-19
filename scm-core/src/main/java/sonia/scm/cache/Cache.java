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

/**
 * The main interface for the cache.
 * Provides methods to add, access, and remove entries from a cache.
 *
 * @author Sebastian Sdorra
 *
 * @param <K> type of the keys for the cache
 * @param <V> type of cached elements
 */
public interface Cache<K, V>
{

  /**
   * Remove all elements from this cache.
   *
   */
  public void clear();

  /**
   * Returns true if this cache contains an element with the specified key.
   *
   *
   * @param key key of the cached element
   *
   * @return true if this cache contains an element with the specified key
   */
  public boolean contains(K key);

  /**
   * Put a new element to this cache.
   *
   *
   * @param key key of the element to cache
   * @param value element that should be cached
   *
   * @return previous cached value or null
   */
  public V put(K key, V value);

  /**
   * Remove the element with the specified key from this cache. Return previous
   * cached value.
   *
   * @param key key of the cached element
   *
   * @return previous cached value or null
   */
  public V remove(K key);

  /**
   * Remove all elements with matching {@link Predicate} from this cache.
   * The method returns all previous cached values.
   *
   * @since 1.9
   *
   * @param predicate predicate to match cache keys
   *
   * @return all previous cached values
   */
  public Iterable<V> removeAll(Predicate<K> predicate);

  /**
   * Returns the number of entries in the cache.
   *
   * @return number of entries in the cache
   *
   * @since 2.0.0
   */
  public int size();

  //~--- get methods ----------------------------------------------------------

  /**
   * Returns the element with the specified key.
   * Returns null if the cache contains no element with the specified key.
   *
   * @param key key of the cached element
   *
   * @return The cached element with the specified key or null
   */
  public V get(K key);

  /**
   * Returns performance statistics of the cache or null if the cache does not
   * support statistics. The returned statistic is a snapshot of the current
   * performance.
   *
   * @return performance statistics or null
   *
   * @since 2.0.0
   */
  public CacheStatistics getStatistics();
}
