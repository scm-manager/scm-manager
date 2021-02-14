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
import com.google.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

//~--- JDK imports ------------------------------------------------------------

/**
 * Guava based implementation of {@link CacheManager} and {@link org.apache.shiro.cache.CacheManager}.
 *
 * @author Sebastian Sdorra
 */
@Singleton
public class GuavaCacheManager
  implements CacheManager, org.apache.shiro.cache.CacheManager {

  /**
   * the logger for GuavaCacheManager
   */
  private static final Logger LOG = LoggerFactory.getLogger(GuavaCacheManager.class);

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   */
  @Inject
  public GuavaCacheManager(GuavaCacheConfigurationReader configurationReader) {
    this(configurationReader.read());
  }

  /**
   * Constructs ...
   *
   * @param config
   */
  @VisibleForTesting
  protected GuavaCacheManager(GuavaCacheManagerConfiguration config) {
    defaultConfiguration = config.getDefaultCache();

    for (GuavaNamedCacheConfiguration ncc : config.getCaches()) {
      LOG.debug("create cache {} from configured configuration {}", ncc.getName(), ncc);
      caches.put(ncc.getName(), new GuavaCache<>(ncc));
    }
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   * @throws IOException
   */
  @Override
  public void close() throws IOException {
    LOG.info("close guava cache manager");

    for (Cache c : caches.values()) {
      c.clear();
    }

    caches.clear();
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   * @param name
   * @param <K>
   * @param <V>
   * @return
   */
  @Override
  @SuppressWarnings("unchecked")
  public <K, V> GuavaCache<K, V> getCache(String name) {
    LOG.trace("try to retrieve cache {}", name);

    return caches.computeIfAbsent(name, cacheName -> {
      LOG.debug(
        "cache {} does not exists, creating a new instance from default configuration: {}",
        cacheName, defaultConfiguration
      );
      return new GuavaCache<>(defaultConfiguration, cacheName);
    });
  }

  //~--- fields ---------------------------------------------------------------

  /**
   * Field description
   */
  private final ConcurrentHashMap<String, GuavaCache> caches = new ConcurrentHashMap<>();

  /**
   * Field description
   */
  private GuavaCacheConfiguration defaultConfiguration;
}
