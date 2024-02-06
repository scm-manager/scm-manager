/*
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


import com.google.common.base.Predicate;

/**
 * The main interface for the cache.
 * Provides methods to add, access, and remove entries from a cache.
 *
 *
 * @param <K> type of the keys for the cache
 * @param <V> type of cached elements
 */
@SuppressWarnings("java:S2176") // we could not rename the interface
public interface Cache<K, V> extends org.apache.shiro.cache.Cache<K, V> {

  /**
   * Remove all elements from this cache.
   */
  void clear();

  /**
   * Returns true if this cache contains an element with the specified key.
   *
   * @param key key of the cached element
   */
  boolean contains(K key);

  /**
   * Put a new element to this cache.
   *
   *
   * @param key key of the element to cache
   * @param value element that should be cached
   *
   * @return previous cached value or null
   */
  V put(K key, V value);

  /**
   * Remove the element with the specified key from this cache. Return previous
   * cached value.
   *
   * @param key key of the cached element
   *
   * @return previous cached value or null
   */
  V remove(K key);

  /**
   * Remove all elements with matching {@link Predicate} from this cache.
   * The method returns all previous cached values.
   *
   * @since 1.9
   *
   * @param predicate predicate to match cache keys
   */
  @SuppressWarnings("java:S4738") // we have to use guava predicate for compatibility
  Iterable<V> removeAll(Predicate<K> predicate);

  /**
   * Returns the number of entries in the cache.
   *
   * @since 2.0.0
   */
  int size();


  /**
   * Returns the element with the specified key.
   * Returns null if the cache contains no element with the specified key.
   *
   * @param key key of the cached element
   *
   * @return The cached element with the specified key or null
   */
  V get(K key);

  /**
   * Returns performance statistics of the cache or null if the cache does not
   * support statistics. The returned statistic is a snapshot of the current
   * performance.
   *
   * @since 2.0.0
   */
  CacheStatistics getStatistics();
}
