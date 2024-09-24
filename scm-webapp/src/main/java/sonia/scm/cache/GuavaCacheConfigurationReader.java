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


import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import jakarta.inject.Inject;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


public class GuavaCacheConfigurationReader {

 
  private static final Logger logger =
    LoggerFactory.getLogger(GuavaCacheConfigurationReader.class);

  private JAXBContext context;

  private CacheConfigurationLoader loader;

  @Inject
  public GuavaCacheConfigurationReader(CacheConfigurationLoader loader) {
    this.loader = loader;

    try {
      this.context = JAXBContext.newInstance(GuavaCacheManagerConfiguration.class);
    } catch (JAXBException ex) {
      throw new CacheException("could not create jaxb context for cache configuration", ex);
    }
  }


  public GuavaCacheManagerConfiguration read() {
    URL defaultConfigUrl = loader.getDefaultResource();

    if (defaultConfigUrl == null) {
      throw new IllegalStateException(
        "could not find default cache configuration");
    }

    GuavaCacheManagerConfiguration config = readConfiguration(defaultConfigUrl, true);

    Iterator<URL> it = loader.getModuleResources();

    if (!it.hasNext()) {
      logger.debug("no module configuration found");
    }

    while (it.hasNext()) {
      GuavaCacheManagerConfiguration moduleConfig =
        readConfiguration(it.next(), false);

      if (moduleConfig != null) {
        config = merge(config, moduleConfig);
      }
    }

    File manualFile = loader.getManualFileResource();

    if (manualFile.exists()) {
      try {
        GuavaCacheManagerConfiguration manualConfig = readConfiguration(manualFile.toURI().toURL(), false);
        if (manualConfig != null) {
          config = merge(config, manualConfig);
        }
      } catch (MalformedURLException ex) {
        logger.error("malformed url", ex);
      }
    } else {
      logger.warn("could not find manual configuration at {}", manualFile);
    }

    return config;
  }

  private Map<String, GuavaNamedCacheConfiguration> createNamedCacheMap(
    List<GuavaNamedCacheConfiguration> caches) {
    Map<String, GuavaNamedCacheConfiguration> map = Maps.newLinkedHashMap();

    for (GuavaNamedCacheConfiguration ncc : caches) {
      map.put(ncc.getName(), ncc);
    }

    return map;
  }


  private GuavaCacheManagerConfiguration merge(
    GuavaCacheManagerConfiguration config, GuavaCacheManagerConfiguration other) {
    GuavaCacheConfiguration defaultCache = config.getDefaultCache();
    Map<String, GuavaNamedCacheConfiguration> namedCaches =
      createNamedCacheMap(config.getCaches());

    if (other.getDefaultCache() != null) {
      defaultCache = other.getDefaultCache();
    }

    List<GuavaNamedCacheConfiguration> otherNamedCaches = other.getCaches();

    for (GuavaNamedCacheConfiguration ncc : otherNamedCaches) {
      namedCaches.put(ncc.getName(), ncc);
    }

    return new GuavaCacheManagerConfiguration(defaultCache,
      ImmutableList.copyOf(namedCaches.values()));
  }


  private GuavaCacheManagerConfiguration readConfiguration(URL url, boolean fail) {
    logger.debug("read cache configuration from {}", url);

    GuavaCacheManagerConfiguration config = null;

    try {
      Unmarshaller unmarshaller = context.createUnmarshaller();
      config = (GuavaCacheManagerConfiguration) unmarshaller.unmarshal(url);
    } catch (JAXBException ex) {
      if (fail) {
        throw new CacheException("could not unmarshall cache configuration", ex);
      } else {
        logger.warn("could not unmarshall cache configuration", ex);
      }
    }

    return config;
  }

}
