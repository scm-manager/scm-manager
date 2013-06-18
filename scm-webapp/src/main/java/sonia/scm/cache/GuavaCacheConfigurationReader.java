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
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//~--- JDK imports ------------------------------------------------------------

import java.io.File;

import java.net.MalformedURLException;
import java.net.URL;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

/**
 *
 * @author Sebastian Sdorra
 */
public class GuavaCacheConfigurationReader
{

  /** Field description */
  private static final String DEFAULT = "/config/gcache.xml";

  /** Field description */
  private static final String MANUAL_RESOURCE =
    "ext".concat(File.separator).concat("gcache.xml");

  /** Field description */
  private static final String MODULE_RESOURCES = "/META-INF/scm/gcache.xml";

  /**
   * the logger for CacheConfigurationReader
   */
  private static final Logger logger =
    LoggerFactory.getLogger(GuavaCacheConfigurationReader.class);

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   *
   * @param loader
   */
  @VisibleForTesting
  GuavaCacheConfigurationReader(CacheConfigurationLoader loader)
  {
    this.loader = loader;

    try
    {
      this.context =
        JAXBContext.newInstance(GuavaCacheManagerConfiguration.class);
    }
    catch (JAXBException ex)
    {
      throw new CacheException(
        "could not create jaxb context for cache configuration", ex);
    }
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  public static GuavaCacheManagerConfiguration read()
  {
    return new GuavaCacheConfigurationReader(
      new DefaultCacheConfigurationLoader(
        DEFAULT, MANUAL_RESOURCE, MODULE_RESOURCES)).doRead();
  }

  /**
   * Method description
   *
   *
   * @return
   */
  @VisibleForTesting
  GuavaCacheManagerConfiguration doRead()
  {
    URL defaultConfigUrl = loader.getDefaultResource();

    if (defaultConfigUrl == null)
    {
      throw new IllegalStateException(
        "could not find default cache configuration");
    }

    GuavaCacheManagerConfiguration config = readConfiguration(defaultConfigUrl,
                                              true);

    Iterator<URL> it = loader.getModuleResources();

    if (!it.hasNext())
    {
      logger.debug("no module configuration found");
    }

    while (it.hasNext())
    {
      GuavaCacheManagerConfiguration moduleConfig =
        readConfiguration(it.next(), false);

      if (moduleConfig != null)
      {
        config = merge(config, moduleConfig);
      }
    }

    File manualFile = loader.getManualFileResource();

    if (manualFile.exists())
    {
      try
      {
        GuavaCacheManagerConfiguration manualConfig =
          readConfiguration(manualFile.toURI().toURL(), false);

        config = merge(config, manualConfig);
      }
      catch (MalformedURLException ex)
      {
        logger.error("malformed url", ex);
      }
    }
    else
    {
      logger.warn("could not find manual configuration at {}", manualFile);
    }

    return config;
  }

  /**
   * Method description
   *
   *
   * @param caches
   *
   * @return
   */
  private Map<String, GuavaNamedCacheConfiguration> createNamedCacheMap(
    List<GuavaNamedCacheConfiguration> caches)
  {
    Map<String, GuavaNamedCacheConfiguration> map = Maps.newLinkedHashMap();

    for (GuavaNamedCacheConfiguration ncc : caches)
    {
      map.put(ncc.getName(), ncc);
    }

    return map;
  }

  /**
   * Method description
   *
   *
   * @param config
   * @param other
   *
   * @return
   */
  private GuavaCacheManagerConfiguration merge(
    GuavaCacheManagerConfiguration config, GuavaCacheManagerConfiguration other)
  {
    GuavaCacheConfiguration defaultCache = config.getDefaultCache();
    Map<String, GuavaNamedCacheConfiguration> namedCaches =
      createNamedCacheMap(config.getCaches());

    if (other.getDefaultCache() != null)
    {
      defaultCache = other.getDefaultCache();
    }

    List<GuavaNamedCacheConfiguration> otherNamedCaches = other.getCaches();

    for (GuavaNamedCacheConfiguration ncc : otherNamedCaches)
    {
      namedCaches.put(ncc.getName(), ncc);
    }

    return new GuavaCacheManagerConfiguration(defaultCache,
      ImmutableList.copyOf(namedCaches.values()));
  }

  /**
   * Method description
   *
   *
   * @param url
   * @param fail
   *
   * @return
   */
  private GuavaCacheManagerConfiguration readConfiguration(URL url,
    boolean fail)
  {
    logger.debug("read cache configuration from {}", url);

    GuavaCacheManagerConfiguration config = null;

    try
    {
      config =
        (GuavaCacheManagerConfiguration) context.createUnmarshaller().unmarshal(
          url);
    }
    catch (JAXBException ex)
    {
      if (fail)
      {
        throw new CacheException("could not unmarshall cache configuration",
          ex);
      }
      else
      {
        logger.warn("could not unmarshall cache configuration", ex);
      }
    }

    return config;
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private JAXBContext context;

  /** Field description */
  private CacheConfigurationLoader loader;
}
