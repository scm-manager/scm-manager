/**
 * Copyright (c) 2014, Sebastian Sdorra
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

import com.google.common.cache.Cache;
import com.google.common.collect.Sets;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.Filter;

/**
 * Implementation of basic cache methods for guava based caches.
 * 
 * @author Sebastian Sdorra
 * 
 * @since 1.52
 * 
 * @param <K> key
 * @param <V> value
 */
public abstract class GuavaBaseCache<K, V> {
  
  /**
   * the logger for GuavaBaseCache
   */
  private static final Logger logger = LoggerFactory.getLogger(GuavaCache.class);

  protected com.google.common.cache.Cache<K, V> cache;
  protected CopyStrategy copyStrategy = CopyStrategy.NONE;
  private final String name;

  GuavaBaseCache(com.google.common.cache.Cache<K, V> cache, CopyStrategy copyStrategy, String name) {
    this.cache = cache;
    this.name = name;

    if (copyStrategy != null) {
      this.copyStrategy = copyStrategy;
    }
  }

  //~--- methods --------------------------------------------------------------

  public void clear() {
    logger.debug("clear cache {}", name);
    cache.invalidateAll();
  }

  public boolean contains(K key) {
    return cache.getIfPresent(key) != null;
  }

  public boolean removeAll(Filter<K> filter) {
    Set<K> keysToRemove = Sets.newHashSet();

    for (K key : cache.asMap().keySet()) {
      if (filter.accept(key)) {
        keysToRemove.add(key);
      }
    }

    boolean result = false;

    if (!keysToRemove.isEmpty()) {
      cache.invalidateAll(keysToRemove);
      result = true;
    }

    return result;
  }
  
  public V get(K key) {
    V value = cache.getIfPresent(key);

    if (value != null) {
      value = copyStrategy.copyOnRead(value);
    }

    return value;
  }

  public Cache<K, V> getWrappedCache() {
    return cache;
  }
}
