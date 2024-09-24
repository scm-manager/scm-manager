/*
 * Copyright (c) 2020 - present Cloudogu GmbH
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

package sonia.scm.cache;

import com.google.common.annotations.VisibleForTesting;
import com.google.inject.Singleton;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Guava based implementation of {@link CacheManager} and {@link org.apache.shiro.cache.CacheManager}.
 *
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
