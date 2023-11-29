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

/**
 * @author Sebastian Sdorra
 */
public class GuavaCacheConfigurationReader {

  /**
   * the logger for CacheConfigurationReader
   */
  private static final Logger logger =
    LoggerFactory.getLogger(GuavaCacheConfigurationReader.class);

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   * @param loader
   */
  @Inject
  public GuavaCacheConfigurationReader(CacheConfigurationLoader loader) {
    this.loader = loader;

    try {
      this.context = JAXBContext.newInstance(GuavaCacheManagerConfiguration.class);
    } catch (JAXBException ex) {
      throw new CacheException("could not create jaxb context for cache configuration", ex);
    }
  }

  //~--- methods --------------------------------------------------------------

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

  /**
   * Method description
   *
   * @param config
   * @param other
   * @return
   */
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

  /**
   * Method description
   *
   * @param url
   * @param fail
   * @return
   */
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

  //~--- fields ---------------------------------------------------------------

  /**
   * Field description
   */
  private JAXBContext context;

  /**
   * Field description
   */
  private CacheConfigurationLoader loader;
}
