/**
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
import com.google.common.collect.Maps;
import com.google.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;

import java.util.Map;

/**
 * Guava based implementation of {@link CacheManager} and {@link org.apache.shiro.cache.CacheManager}.
 * 
 * @author Sebastian Sdorra
 */
@Singleton
public class GuavaCacheManager
  implements CacheManager, org.apache.shiro.cache.CacheManager
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
   * @param name
   * @param <K>
   * @param <V>
   *
   * @return
   */
  @Override
  @SuppressWarnings("unchecked")
  public synchronized <K, V> GuavaCache<K, V> getCache(String name)
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
