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

import java.io.Closeable;

/**
 * The {@link CacheManager} holds references to {@link Cache}
 * and manages their creation.
 * This class is a singleton which can be retrieved via injection.
 *
 */
public interface CacheManager extends Closeable {

  /**
   * Returns the cache with the specified types and name.
   * If the cache does not exist, a new cache is created.
   *
   * @param name - The name of the cache
   * @param <K> - The type of the keys for the cache
   * @param <V>  - The type of cache elements
   *
   * @return the cache with the specified types and name
   */
  <K, V> Cache<K, V> getCache(String name);

  /**
   * Clears (aka invalidates) all caches.
   * @since 2.48.0
   */
  void clearAllCaches();
}
