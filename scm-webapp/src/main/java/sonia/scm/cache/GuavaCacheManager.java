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

import com.google.common.annotations.VisibleForTesting;
import com.google.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Guava based implementation of {@link CacheManager} and {@link org.apache.shiro.cache.CacheManager}.
 *
 * @author Sebastian Sdorra
 */
@Singleton
public class GuavaCacheManager implements CacheManager, org.apache.shiro.cache.CacheManager {

  private static final Logger LOG = LoggerFactory.getLogger(GuavaCacheManager.class);

  @SuppressWarnings({"java:S3740", "rawtypes"})
  private final ConcurrentHashMap<String, GuavaCache> caches = new ConcurrentHashMap<>();
  private final GuavaCacheConfiguration defaultConfiguration;
  private final GuavaCacheFactory cacheFactory;


  @Inject
  public GuavaCacheManager(GuavaCacheConfigurationReader configurationReader, GuavaCacheFactory cacheFactory) {
    this(configurationReader.read(), cacheFactory);
  }

  @VisibleForTesting
  protected GuavaCacheManager(GuavaCacheManagerConfiguration config, GuavaCacheFactory cacheFactory) {
    defaultConfiguration = config.getDefaultCache();
    this.cacheFactory = cacheFactory;

    for (GuavaNamedCacheConfiguration ncc : config.getCaches()) {
      LOG.debug("create cache {} from configured configuration {}", ncc.getName(), ncc);
      caches.put(ncc.getName(), cacheFactory.create(ncc, ncc.getName()));
    }
  }

  @Override
  @SuppressWarnings("unchecked")
  public <K, V> GuavaCache<K, V> getCache(String name) {
    LOG.trace("try to retrieve cache {}", name);

    return caches.computeIfAbsent(name, cacheName -> {
      LOG.debug(
        "cache {} does not exists, creating a new instance from default configuration: {}",
        cacheName, defaultConfiguration
      );
      return cacheFactory.create(defaultConfiguration, cacheName);
    });
  }

  @Override
  public void clearAllCaches() {
    for(GuavaCache<?, ?> cache : caches.values()) {
      cache.clear();
    }
  }

  @Override
  public void close() throws IOException {
    LOG.info("close guava cache manager");

    for (Cache<?, ?> c : caches.values()) {
      c.clear();
    }

    caches.clear();
  }
}
