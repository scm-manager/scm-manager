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

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Predicate;
import com.google.common.collect.Sets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//~--- JDK imports ------------------------------------------------------------

import java.util.Set;

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
  private static final Logger logger =
    LoggerFactory.getLogger(GuavaCache.class);

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
  public GuavaCache(GuavaCacheConfiguration configuration, String name)
  {
    this(GuavaCaches.create(configuration, name),
      configuration.getCopyStrategy(), name);
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
   * @param key
   * @param value
   */
  @Override
  public void put(K key, V value)
  {
    cache.put(key, copyStrategy.copyOnWrite(value));
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
    cache.invalidate(key);

    return true;
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
  public boolean removeAll(Predicate<K> filter)
  {
    Set<K> keysToRemove = Sets.newHashSet();

    for (K key : cache.asMap().keySet())
    {
      if (filter.apply(key))
      {
        keysToRemove.add(key);
      }
    }

    boolean result = false;

    if (!keysToRemove.isEmpty())
    {
      cache.invalidateAll(keysToRemove);
      result = true;
    }

    return result;
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
    }

    return value;
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private com.google.common.cache.Cache<K, V> cache;

  /** Field description */
  private CopyStrategy copyStrategy = CopyStrategy.NONE;

  /** Field description */
  private String name;
}
