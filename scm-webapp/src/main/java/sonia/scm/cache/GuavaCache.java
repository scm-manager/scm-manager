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
import com.google.common.cache.CacheStats;
import com.google.common.collect.Sets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Map.Entry;
import java.util.Set;

public class GuavaCache<K, V> implements Cache<K, V> {

  private static final Logger logger = LoggerFactory.getLogger(GuavaCache.class);

  private final com.google.common.cache.Cache<K, V> cache;
  private final CopyStrategy copyStrategy;
  private final String name;

  GuavaCache(com.google.common.cache.Cache<K, V> cache, CopyStrategy copyStrategy, String name) {
    this.cache = cache;
    this.name = name;

    if (copyStrategy != null) {
      this.copyStrategy = copyStrategy;
    } else {
      this.copyStrategy = CopyStrategy.NONE;
    }
  }

  @Override
  public void clear() {
    if (logger.isDebugEnabled()) {
      logger.debug("clear cache {}", name);
    }

    cache.invalidateAll();
  }

  @Override
  public boolean contains(K key) {
    return cache.getIfPresent(key) != null;
  }

  @Override
  public Set<K> keys() {
    return cache.asMap().keySet();
  }

  @Override
  public V put(K key, V value) {
    V previous = cache.getIfPresent(key);

    cache.put(key, copyStrategy.copyOnWrite(value));

    return previous;
  }

  @Override
  public V remove(K key) {
    V value = cache.getIfPresent(key);

    cache.invalidate(key);

    return value;
  }

  @Override
  @SuppressWarnings("java:S4738") // we have to use guava predicate for compatibility
  public Iterable<V> removeAll(Predicate<K> filter) {
    Set<V> removedValues = Sets.newHashSet();
    Set<K> keysToRemove = Sets.newHashSet();

    for (Entry<K, V> e : cache.asMap().entrySet()) {
      if (filter.apply(e.getKey())) {
        keysToRemove.add(e.getKey());
        removedValues.add(e.getValue());
      }
    }

    if (!keysToRemove.isEmpty()) {
      cache.invalidateAll(keysToRemove);
    }

    return removedValues;
  }

  @Override
  public int size() {
    return (int) cache.size();
  }

  @Override
  public Collection<V> values() {
    return cache.asMap().values();
  }

  @Override
  public V get(K key) {
    V value = cache.getIfPresent(key);

    if (value != null) {
      value = copyStrategy.copyOnRead(value);
    }

    return value;
  }

  @Override
  public CacheStatistics getStatistics() {
    CacheStats cacheStats = cache.stats();
    return new CacheStatistics(name, cacheStats.hitCount(), cacheStats.missCount());
  }
}
