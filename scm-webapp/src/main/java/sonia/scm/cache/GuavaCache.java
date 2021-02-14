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

//~--- non-JDK imports --------------------------------------------------------

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Predicate;
import com.google.common.collect.Sets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//~--- JDK imports ------------------------------------------------------------

import java.util.Collection;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

/**
 *
 * @author Sebastian Sdorra
 *
 * @param <K>
 * @param <V>
 */
public class GuavaCache<K, V> implements Cache<K, V>
{

  /**
   * the logger for GuavaCache
   */
  private static final Logger logger = LoggerFactory.getLogger(GuavaCache.class);

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   *
   * @param configuration
   */
  public GuavaCache(GuavaNamedCacheConfiguration configuration)
  {
    this(configuration, configuration.getName());
  }

  /**
   * Constructs ...
   *
   *
   * @param configuration
   * @param name
   */
  @SuppressWarnings("unchecked")
  public GuavaCache(GuavaCacheConfiguration configuration, String name)
  {
    this(GuavaCaches.create(configuration, name), configuration.getCopyStrategy(), name);
  }

  /**
   * Constructs ...
   *
   *
   * @param cache
   * @param copyStrategy
   * @param name
   */
  @VisibleForTesting
  protected GuavaCache(com.google.common.cache.Cache<K, V> cache,
    CopyStrategy copyStrategy, String name)
  {
    this.cache = cache;
    this.name = name;

    if (copyStrategy != null)
    {
      this.copyStrategy = copyStrategy;
    }
    else
    {
      this.copyStrategy = CopyStrategy.NONE;
    }
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   */
  @Override
  public void clear()
  {
    if (logger.isDebugEnabled())
    {
      logger.debug("clear cache {}", name);
    }

    cache.invalidateAll();
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
    return cache.getIfPresent(key) != null;
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
    return cache.asMap().keySet();
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
    V previous = cache.getIfPresent(key);

    cache.put(key, copyStrategy.copyOnWrite(value));

    return previous;
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
    V value = cache.getIfPresent(key);

    cache.invalidate(key);

    return value;
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
    Set<V> removedValues = Sets.newHashSet();
    Set<K> keysToRemove = Sets.newHashSet();

    for (Entry<K, V> e : cache.asMap().entrySet())
    {
      if (filter.apply(e.getKey()))
      {
        keysToRemove.add(e.getKey());
        removedValues.add(e.getValue());
      }
    }

    if (!keysToRemove.isEmpty())
    {
      cache.invalidateAll(keysToRemove);
    }

    return removedValues;
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
    return (int) cache.size();
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
    return cache.asMap().values();
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
    V value = cache.getIfPresent(key);

    if (value != null)
    {
      value = copyStrategy.copyOnRead(value);
      hitCount.incrementAndGet();
    }
    else
    {
      missCount.incrementAndGet();
    }

    return value;
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
    return new CacheStatistics(name, hitCount.get(), missCount.get());
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private final com.google.common.cache.Cache<K, V> cache;

  /** Field description */
  private final CopyStrategy copyStrategy;

  /** Field description */
  private final AtomicLong hitCount = new AtomicLong();

  /** Field description */
  private final AtomicLong missCount = new AtomicLong();

  /** Field description */
  private final String name;
}
