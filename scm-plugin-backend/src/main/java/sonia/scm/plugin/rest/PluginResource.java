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



package sonia.scm.plugin.rest;

//~--- non-JDK imports --------------------------------------------------------

import com.google.inject.Inject;
import com.google.inject.Singleton;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sonia.scm.plugin.BackendConfiguration;
import sonia.scm.plugin.PluginBackend;
import sonia.scm.plugin.PluginBackendListener;
import sonia.scm.plugin.PluginCenter;
import sonia.scm.plugin.PluginInformation;
import sonia.scm.plugin.PluginVersion;

//~--- JDK imports ------------------------------------------------------------

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 *
 * @author Sebastian Sdorra
 */
@Singleton
@Path("{version}/plugins")
public class PluginResource implements PluginBackendListener
{

  /** Field description */
  public static final String CACHE = "sonia.cache.plugin-backend";

  /** the logger for PluginResource */
  private static final Logger logger =
    LoggerFactory.getLogger(PluginResource.class);

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   *
   * @param backend
   * @param configuration
   * @param cacheManager
   */
  @Inject
  public PluginResource(PluginBackend backend,
                        BackendConfiguration configuration,
                        CacheManager cacheManager)
  {
    this.backend = backend;
    this.configuration = configuration;
    cache = cacheManager.getCache(CACHE);

    // added listener to clear the cache on a event
    this.backend.addListener(this);
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param plugins
   */
  @Override
  public void addedNewPlugins(Collection<PluginInformation> plugins)
  {
    if (logger.isDebugEnabled())
    {
      logger.debug("clear cache {}", CACHE);
    }

    cache.removeAll();
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param version
   * @param os
   * @param arch
   * @param snapshot
   *
   * @return
   */
  @GET
  @Produces(MediaType.APPLICATION_XML)
  public Response getPlugins(@PathParam("version") String version,
                             @QueryParam("os") String os,
                             @QueryParam("arch") String arch,
                             @DefaultValue("false")
  @QueryParam("snapshot") boolean snapshot)
  {
    if (logger.isDebugEnabled())
    {
      logger.debug("load plugins for version {}, include snapshots: {}",
                   version, Boolean.toString(snapshot));
    }

    PluginCenter pc = null;
    String key = createCacheKey(version, os, arch, snapshot);
    Element el = cache.get(key);

    if (el != null)
    {
      if (logger.isDebugEnabled())
      {
        logger.debug("load plugin center from cache");
      }

      pc = (PluginCenter) el.getObjectValue();
    }
    else
    {
      if (logger.isDebugEnabled())
      {
        logger.debug("load plugin center from backend");
      }

      List<PluginInformation> plugins =
        backend.getPlugins(new DefaultPluginFilter(version, os, arch,
          snapshot));

      pc = new PluginCenter();
      pc.setPlugins(getNewestPlugins(plugins));
      pc.setRepositories(configuration.getRepositories());
      cache.put(new Element(key, pc));
    }

    return Response.ok(pc).build();
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param version
   * @param os
   * @param arch
   * @param snapshot
   *
   * @return
   */
  private String createCacheKey(String version, String os, String arch,
                                boolean snapshot)
  {
    StringBuilder key = new StringBuilder(version);

    key.append(":").append(os).append(":").append(arch).append(":");
    key.append(Boolean.toString(snapshot));

    return key.toString();
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param plugins
   *
   * @return
   */
  private Set<PluginInformation> getNewestPlugins(
          List<PluginInformation> plugins)
  {
    Collections.sort(plugins, PluginInformationComparator.INSTANCE);

    Set<PluginInformation> pluginSet = new HashSet<PluginInformation>();
    PluginInformation newest = null;

    for (PluginInformation plugin : plugins)
    {
      if (newest == null)
      {
        newest = plugin;
      }
      else if (isSamePlugin(plugin, newest))
      {
        if (isNewer(plugin, newest))
        {
          newest = plugin;
        }
      }
      else
      {
        pluginSet.add(newest);
        newest = plugin;
      }
    }

    pluginSet.add(newest);

    return pluginSet;
  }

  /**
   * Method description
   *
   *
   * @param plugin
   * @param newest
   *
   * @return
   */
  private boolean isNewer(PluginInformation plugin, PluginInformation newest)
  {
    return new PluginVersion(plugin.getVersion()).isNewer(newest.getVersion());
  }

  /**
   * Method description
   *
   *
   * @param plugin
   * @param otherPlugin
   *
   * @return
   */
  private boolean isSamePlugin(PluginInformation plugin,
                               PluginInformation otherPlugin)
  {
    return plugin.getGroupId().equals(otherPlugin.getGroupId())
           && plugin.getArtifactId().equals(otherPlugin.getArtifactId());
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private PluginBackend backend;

  /** Field description */
  private Cache cache;

  /** Field description */
  private BackendConfiguration configuration;
}
