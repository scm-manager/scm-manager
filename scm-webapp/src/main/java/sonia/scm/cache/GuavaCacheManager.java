/**
 * Copyright (c) 2010, Sebastian Sdorra All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer. 2. Redistributions in
 * binary form must reproduce the above copyright notice, this list of
 * conditions and the following disclaimer in the documentation and/or other
 * materials provided with the distribution. 3. Neither the name of SCM-Manager;
 * nor the names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * http://bitbucket.org/sdorra/scm-manager
 *
 */



package sonia.scm.cache;

//~--- non-JDK imports --------------------------------------------------------

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Maps;
import com.google.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;

import java.util.Map;

/**
 *
 * @author Sebastian Sdorra
 */
@Singleton
public class GuavaCacheManager implements CacheManager
{

  /**
   * the logger for GuavaCacheManager
   */
  private static final Logger logger =
    LoggerFactory.getLogger(GuavaCacheManager.class);

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   */
  public GuavaCacheManager()
  {
    this(GuavaCacheConfigurationReader.read());
  }

  /**
   * Constructs ...
   *
   *
   * @param config
   */
  @VisibleForTesting
  protected GuavaCacheManager(GuavaCacheManagerConfiguration config)
  {
    defaultConfiguration = config.getDefaultCache();

    for (GuavaNamedCacheConfiguration ncc : config.getCaches())
    {
      logger.debug("create cache {} from configured configuration {}",
        ncc.getName(), ncc);
      cacheMap.put(ncc.getName(), new GuavaCache(ncc));
    }
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @throws IOException
   */
  @Override
  public void close() throws IOException
  {
    logger.info("close guava cache manager");

    for (Cache c : cacheMap.values())
    {
      c.clear();
    }

    cacheMap.clear();
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param key
   * @param value
   * @param name
   * @param <K>
   * @param <V>
   *
   * @return
   */
  @Override
  public synchronized <K, V> GuavaCache<K, V> getCache(Class<K> key,
    Class<V> value, String name)
  {
    logger.trace("try to retrieve cache {}", name);

    GuavaCache<K, V> cache = cacheMap.get(name);

    if (cache == null)
    {
      logger.debug(
        "cache {} does not exists, creating a new instance from default configuration: {}",
        name, defaultConfiguration);
      cache = new GuavaCache<K, V>(defaultConfiguration, name);
      cacheMap.put(name, cache);
    }

    return cache;
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private volatile Map<String, GuavaCache> cacheMap = Maps.newHashMap();

  /** Field description */
  private GuavaCacheConfiguration defaultConfiguration;
}
