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
import com.google.common.collect.Maps;
import com.google.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;

import java.util.Map;

import org.apache.shiro.cache.CacheException;

/**
 * Guava based implementation of {@link CacheManager} and {@link org.apache.shiro.cache.CacheManager}.
 * 
 * @author Sebastian Sdorra
 */
@Singleton
public class GuavaCacheManager implements CacheManager, org.apache.shiro.cache.CacheManager
{

  /**
   * the logger for GuavaCacheManager
   */
  private static final Logger logger = LoggerFactory.getLogger(GuavaCacheManager.class);

  private volatile Map<String, CacheWithConfiguration> cacheMap = Maps.newHashMap();
  
  private GuavaCacheConfiguration defaultConfiguration;
  
  
  //~--- constructors ---------------------------------------------------------

  public GuavaCacheManager() {
    this(GuavaCacheConfigurationReader.read());
  }

  @VisibleForTesting
  protected GuavaCacheManager(GuavaCacheManagerConfiguration config) {
    defaultConfiguration = config.getDefaultCache();

    for (GuavaNamedCacheConfiguration namedCacheConfiguration : config.getCaches()) {
      logger.debug("create cache {} from configured configuration {}",
        namedCacheConfiguration.getName(), namedCacheConfiguration
      );
      cacheMap.put(namedCacheConfiguration.getName(), createCacheWithConfiguration(namedCacheConfiguration));
    }
  }

  private CacheWithConfiguration createCacheWithConfiguration(GuavaNamedCacheConfiguration namedCacheConfiguration) {
    return createCacheWithConfiguration(namedCacheConfiguration, namedCacheConfiguration.getName());
  }

  private CacheWithConfiguration createCacheWithConfiguration(GuavaCacheConfiguration configuration, String name) {
    return new CacheWithConfiguration(GuavaCaches.create(configuration, name), configuration);
  }

  @Override
  public void close() throws IOException {
    logger.info("close guava cache manager");

    for (CacheWithConfiguration c : cacheMap.values()) {
      c.cache.invalidateAll();
    }

    cacheMap.clear();
  }

  //~--- get methods ----------------------------------------------------------

  private synchronized <K, V> CacheWithConfiguration<K, V> getCacheWithConfiguration(String name) {
    logger.trace("try to retrieve cache {}", name);

    CacheWithConfiguration<K, V> cache = cacheMap.get(name);

    if (cache == null) {
      logger.debug(
        "cache {} does not exists, creating a new instance from default configuration: {}",
        name, defaultConfiguration
      );
      cache = createCacheWithConfiguration(defaultConfiguration, name);
      cacheMap.put(name, cache);
    }
    
    return cache;
  }
  
  @Override
  public <K, V> GuavaCache<K, V> getCache(Class<K> key, Class<V> value, String name) {
    CacheWithConfiguration<K, V> cw = getCacheWithConfiguration(name);
    return new GuavaCache<>(cw.cache, cw.configuration.getCopyStrategy(), name);
  }
  
  @Override
  public <K, V> GuavaSecurityCache<K, V> getCache(String name) throws CacheException {
    CacheWithConfiguration<K, V> cw = getCacheWithConfiguration(name);
    return new GuavaSecurityCache<>(cw.cache, cw.configuration.getCopyStrategy(), name);
  }

  private static class CacheWithConfiguration<K,V> {
  
    private final com.google.common.cache.Cache<K,V> cache;
    private final GuavaCacheConfiguration configuration;

    private CacheWithConfiguration(com.google.common.cache.Cache<K, V> cache, GuavaCacheConfiguration configuration) {
      this.cache = cache;
      this.configuration = configuration;
    }
    
  }
}
