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

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sonia.scm.plugin.BackendConfiguration;
import sonia.scm.plugin.PluginBackend;
import sonia.scm.plugin.PluginBackendListener;
import sonia.scm.plugin.PluginInformation;

//~--- JDK imports ------------------------------------------------------------

import com.sun.jersey.api.view.Viewable;

import java.util.Collection;

import javax.servlet.ServletContext;

/**
 *
 * @author Sebastian Sdorra
 */
public class CachedViewableResource extends ViewableResource
        implements PluginBackendListener
{

  /** the logger for CachedViewableResource */
  private static final Logger logger =
    LoggerFactory.getLogger(CachedViewableResource.class);

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   *
   * @param context
   * @param configuration
   * @param backend
   * @param cacheManager
   * @param cacheName
   */
  public CachedViewableResource(ServletContext context, PluginBackend backend,
                                BackendConfiguration configuration,
                                CacheManager cacheManager, String cacheName)
  {
    super(context, configuration);
    this.cacheName = cacheName;
    this.cache = cacheManager.getCache(cacheName);
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
      logger.debug("clear cache {}", cacheName);
    }

    cache.removeAll();
  }

  /**
   * Method description
   *
   *
   * @param key
   * @param viewable
   */
  protected void putToCache(String key, Viewable viewable)
  {
    if (logger.isTraceEnabled())
    {
      logger.trace("put viewable to cache with key {}", key);
    }

    cache.put(new Element(key, viewable));
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param key
   *
   * @return
   */
  protected Viewable getFromCache(String key)
  {
    if (logger.isTraceEnabled())
    {
      logger.trace("retrive viewable from cache with key {}", key);
    }

    Viewable viewable = null;
    Element el = cache.get(key);

    if (el != null)
    {
      viewable = (Viewable) el.getObjectValue();
    }

    return viewable;
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private Cache cache;

  /** Field description */
  private String cacheName;
}
