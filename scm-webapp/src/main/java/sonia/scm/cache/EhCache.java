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

import net.sf.ehcache.Element;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sonia.scm.Filter;

//~--- JDK imports ------------------------------------------------------------

import java.util.Iterator;

/**
 *
 * @author Sebastian Sdorra
 *
 * @param <K>
 * @param <V>
 */
public class EhCache<K, V> implements Cache<K, V>
{

  /** the logger for EhCache */
  private static final Logger logger = LoggerFactory.getLogger(EhCache.class);

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   *
   * @param cache
   * @param name
   */
  public EhCache(net.sf.ehcache.Cache cache, String name)
  {
    this.cache = cache;
    this.name = name;
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

    cache.removeAll();
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
    return cache.get(key) != null;
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
   * @param filter
   *
   * @return
   */
  @Override
  public boolean removeAll(Filter<K> filter)
  {
    boolean result = true;
    Iterator<K> it = cache.getKeys().iterator();

    while (it.hasNext())
    {
      K key = it.next();

      if (filter.accept(key))
      {
        if (!cache.remove(key))
        {
          result = false;
        }
      }
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
    V value = null;
    Element el = cache.get(key);

    if (el != null)
    {
      value = (V) el.getObjectValue();
    }

    return value;
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private net.sf.ehcache.Cache cache;

  /** Field description */
  private String name;
}
