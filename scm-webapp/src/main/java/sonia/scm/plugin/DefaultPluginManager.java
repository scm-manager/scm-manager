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



package sonia.scm.plugin;

//~--- non-JDK imports --------------------------------------------------------

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sonia.scm.ConfigurationException;
import sonia.scm.SCMContext;
import sonia.scm.cache.CacheManager;
import sonia.scm.cache.SimpleCache;
import sonia.scm.config.ScmConfiguration;
import sonia.scm.security.SecurityContext;
import sonia.scm.util.AssertUtil;
import sonia.scm.util.SecurityUtil;

//~--- JDK imports ------------------------------------------------------------

import java.net.URL;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

/**
 *
 * @author Sebastian Sdorra
 */
@Singleton
public class DefaultPluginManager implements PluginManager
{

  /** Field description */
  public static final String CACHE_NAME = "sonia.cache.plugins";

  /** the logger for DefaultPluginManager */
  private static final Logger logger =
    LoggerFactory.getLogger(DefaultPluginManager.class);

  /** Field description */
  public static final PluginFilter FILTER_UPDATES =
    new StatePluginFilter(PluginState.UPDATE_AVAILABLE);

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   *
   *
   *
   * @param securityContextProvicer
   * @param configuration
   * @param pluginLoader
   * @param cacheManager
   */
  @Inject
  public DefaultPluginManager(
          Provider<SecurityContext> securityContextProvicer,
          ScmConfiguration configuration, PluginLoader pluginLoader,
          CacheManager cacheManager)
  {
    this.securityContextProvicer = securityContextProvicer;
    this.configuration = configuration;
    this.cache = cacheManager.getSimpleCache(String.class, PluginCenter.class,
            CACHE_NAME);
    installedPlugins = new HashMap<String, PluginInformation>();

    for (Plugin plugin : pluginLoader.getInstalledPlugins())
    {
      PluginInformation info = plugin.getInformation();

      if ((info != null) && info.isValid())
      {
        installedPlugins.put(info.getId(), plugin.getInformation());
      }
    }

    try
    {
      unmarshaller =
        JAXBContext.newInstance(PluginCenter.class).createUnmarshaller();
    }
    catch (JAXBException ex)
    {
      throw new ConfigurationException(ex);
    }
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   */
  @Override
  public void clearCache()
  {
    cache.clear();
  }

  /**
   * Method description
   *
   *
   * @param id
   */
  @Override
  public void install(String id)
  {
    SecurityUtil.assertIsAdmin(securityContextProvicer);

    if (pluginHandler == null)
    {
      getPluginCenter();
    }

    pluginHandler.install(id);
  }

  /**
   * Method description
   *
   *
   * @param id
   */
  @Override
  public void uninstall(String id)
  {
    SecurityUtil.assertIsAdmin(securityContextProvicer);

    throw new UnsupportedOperationException("Not supported yet.");
  }

  /**
   * Method description
   *
   *
   * @param id
   */
  @Override
  public void update(String id)
  {
    SecurityUtil.assertIsAdmin(securityContextProvicer);

    throw new UnsupportedOperationException("Not supported yet.");
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param id
   *
   * @return
   */
  @Override
  public PluginInformation get(String id)
  {
    SecurityUtil.assertIsAdmin(securityContextProvicer);

    PluginInformation result = null;

    for (PluginInformation info : getPluginCenter().getPlugins())
    {
      if (id.equals(info.getId()))
      {
        result = info;

        break;
      }
    }

    return result;
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
  public Set<PluginInformation> get(PluginFilter filter)
  {
    AssertUtil.assertIsNotNull(filter);
    SecurityUtil.assertIsAdmin(securityContextProvicer);

    Set<PluginInformation> infoSet = new HashSet<PluginInformation>();

    filter(infoSet, installedPlugins.values(), filter);
    filter(infoSet, getPluginCenter().getPlugins(), filter);

    return infoSet;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  public Collection<PluginInformation> getAll()
  {
    SecurityUtil.assertIsAdmin(securityContextProvicer);

    Set<PluginInformation> infoSet = new HashSet<PluginInformation>();

    infoSet.addAll(installedPlugins.values());
    infoSet.addAll(getPluginCenter().getPlugins());

    return infoSet;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  public Collection<PluginInformation> getAvailable()
  {
    SecurityUtil.assertIsAdmin(securityContextProvicer);

    Set<PluginInformation> availablePlugins = new HashSet<PluginInformation>();
    Set<PluginInformation> centerPlugins = getPluginCenter().getPlugins();

    for (PluginInformation info : centerPlugins)
    {
      if (!installedPlugins.containsKey(info.getId()))
      {
        availablePlugins.add(info);
      }
    }

    return availablePlugins;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  public Collection<PluginInformation> getAvailableUpdates()
  {
    SecurityUtil.assertIsAdmin(securityContextProvicer);

    return get(FILTER_UPDATES);
  }

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  public Collection<PluginInformation> getInstalled()
  {
    SecurityUtil.assertIsAdmin(securityContextProvicer);

    return installedPlugins.values();
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param target
   * @param source
   * @param filter
   */
  private void filter(Set<PluginInformation> target,
                      Collection<PluginInformation> source, PluginFilter filter)
  {
    for (PluginInformation info : source)
    {
      if (filter.accept(info))
      {
        target.add(info);
      }
    }
  }

  /**
   * Method description
   *
   *
   * @param available
   */
  private void preparePlugin(PluginInformation available)
  {
    PluginState state = PluginState.AVAILABLE;

    for (PluginInformation installed : installedPlugins.values())
    {
      if (isSamePlugin(available, installed))
      {
        if (installed.getVersion().equals(available.getVersion()))
        {
          available.setState(PluginState.INSTALLED);
        }
        else
        {
          available.setState(PluginState.UPDATE_AVAILABLE);
        }
      }
    }

    available.setState(state);
  }

  /**
   * Method description
   *
   *
   * @param pc
   */
  private void preparePlugins(PluginCenter pc)
  {
    Set<PluginInformation> infoSet = pc.getPlugins();

    if (infoSet != null)
    {
      for (PluginInformation available : infoSet)
      {
        preparePlugin(available);
      }
    }
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  private PluginCenter getPluginCenter()
  {
    PluginCenter center = cache.get(PluginCenter.class.getName());

    if (center == null)
    {
      synchronized (DefaultPluginManager.class)
      {
        if (logger.isInfoEnabled())
        {
          logger.info("fetch plugin informations from {}",
                      configuration.getPluginUrl());
        }

        try
        {
          center = (PluginCenter) unmarshaller.unmarshal(
            new URL(configuration.getPluginUrl()));
          preparePlugins(center);
          cache.put(PluginCenter.class.getName(), center);

          if (pluginHandler == null)
          {
            pluginHandler = new AetherPluginHandler(SCMContext.getContext());
          }

          pluginHandler.setPluginRepositories(center.getRepositories());
        }
        catch (Exception ex)
        {
          throw new PluginLoadException(ex);
        }
      }
    }

    return center;
  }

  /**
   * Method description
   *
   *
   * @param p1
   * @param p2
   *
   * @return
   */
  private boolean isSamePlugin(PluginInformation p1, PluginInformation p2)
  {
    return p1.getGroupId().equals(p2.getGroupId())
           && p1.getArtifactId().equals(p2.getArtifactId());
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private SimpleCache<String, PluginCenter> cache;

  /** Field description */
  private ScmConfiguration configuration;

  /** Field description */
  private Map<String, PluginInformation> installedPlugins;

  /** Field description */
  private AetherPluginHandler pluginHandler;

  /** Field description */
  private Provider<SecurityContext> securityContextProvicer;

  /** Field description */
  private Unmarshaller unmarshaller;
}
