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



package sonia.scm;

//~--- non-JDK imports --------------------------------------------------------

import com.google.inject.servlet.ServletModule;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sonia.scm.api.rest.UriExtensionsConfig;
import sonia.scm.cache.CacheManager;
import sonia.scm.cache.EhCacheManager;
import sonia.scm.config.ScmConfiguration;
import sonia.scm.filter.GZipFilter;
import sonia.scm.filter.SSLFilter;
import sonia.scm.filter.SecurityFilter;
import sonia.scm.group.GroupManager;
import sonia.scm.group.xml.XmlGroupManager;
import sonia.scm.plugin.DefaultPluginManager;
import sonia.scm.plugin.Plugin;
import sonia.scm.plugin.PluginLoader;
import sonia.scm.plugin.PluginManager;
import sonia.scm.plugin.ScriptResourceServlet;
import sonia.scm.repository.RepositoryManager;
import sonia.scm.repository.xml.XmlRepositoryManager;
import sonia.scm.security.EncryptionHandler;
import sonia.scm.security.MessageDigestEncryptionHandler;
import sonia.scm.security.SecurityContext;
import sonia.scm.store.JAXBStoreFactory;
import sonia.scm.store.StoreFactory;
import sonia.scm.user.UserManager;
import sonia.scm.user.xml.XmlUserManager;
import sonia.scm.util.DebugServlet;
import sonia.scm.web.security.AuthenticationManager;
import sonia.scm.web.security.BasicSecurityContext;
import sonia.scm.web.security.ChainAuthenticatonManager;
import sonia.scm.web.security.WebSecurityContext;

//~--- JDK imports ------------------------------------------------------------

import com.sun.jersey.api.core.PackagesResourceConfig;
import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.api.json.JSONConfiguration;
import com.sun.jersey.guice.spi.container.servlet.GuiceContainer;
import com.sun.jersey.spi.container.servlet.ServletContainer;

import java.io.File;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.JAXB;

/**
 *
 * @author Sebastian Sdorra
 */
public class ScmServletModule extends ServletModule
{

  /** Field description */
  public static final String PATTERN_ALL = "/*";

  /** Field description */
  public static final String PATTERN_DEBUG = "/debug.html";

  /** Field description */
  public static final String PATTERN_PAGE = "*.html";

  /** Field description */
  public static final String PATTERN_PLUGIN_SCRIPT = "/plugins/sonia.plugin.js";

  /** Field description */
  public static final String PATTERN_RESTAPI = "/api/rest/*";

  /** Field description */
  public static final String PATTERN_SCRIPT = "*.js";

  /** Field description */
  public static final String PATTERN_STYLESHEET = "*.css";

  /** Field description */
  public static final String RESOURCE_REGEX =
    "^/(?:resources|api|plugins|index)[\\./].*(?:html|\\.css|\\.js|\\.xml|\\.json|\\.txt)";

  /** Field description */
  public static final String REST_PACKAGE = "sonia.scm.api.rest";

  /** Field description */
  public static final String[] PATTERN_STATIC_RESOURCES = new String[] {
                                                            PATTERN_SCRIPT,
          PATTERN_STYLESHEET, "*.jpg", "*.gif", "*.png" };

  /** Field description */
  public static final String[] PATTERN_COMPRESSABLE = new String[] {
                                                        PATTERN_SCRIPT,
          PATTERN_STYLESHEET, "*.json", "*.xml", "*.txt" };

  /** Field description */
  private static Logger logger =
    LoggerFactory.getLogger(ScmServletModule.class);

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   *
   * @param pluginLoader
   * @param bindExtProcessor
   */
  ScmServletModule(PluginLoader pluginLoader,
                   BindingExtensionProcessor bindExtProcessor)
  {
    this.pluginLoader = pluginLoader;
    this.bindExtProcessor = bindExtProcessor;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   */
  @Override
  protected void configureServlets()
  {
    SCMContextProvider context = SCMContext.getContext();

    bind(SCMContextProvider.class).toInstance(context);

    ScmConfiguration config = getScmConfiguration(context);

    bind(StoreFactory.class).to(JAXBStoreFactory.class);
    bind(ScmConfiguration.class).toInstance(config);
    bind(PluginLoader.class).toInstance(pluginLoader);
    bind(PluginManager.class).to(DefaultPluginManager.class);
    bind(EncryptionHandler.class).to(MessageDigestEncryptionHandler.class);
    bindExtProcessor.bindExtensions(binder());

    // bind security stuff
    bind(AuthenticationManager.class).to(ChainAuthenticatonManager.class);
    bind(SecurityContext.class).to(BasicSecurityContext.class);
    bind(WebSecurityContext.class).to(BasicSecurityContext.class);

    // bind security cache
    bind(CacheManager.class).to(EhCacheManager.class);

    // bind(RepositoryManager.class).annotatedWith(Undecorated.class).to(
    // BasicRepositoryManager.class);
    bind(RepositoryManager.class).to(XmlRepositoryManager.class);
    bind(UserManager.class).to(XmlUserManager.class);
    bind(GroupManager.class).to(XmlGroupManager.class);

    // filter(PATTERN_RESTAPI).through(LoggingFilter.class);

    /*
     * filter(PATTERN_PAGE,
     *      PATTERN_STATIC_RESOURCES).through(StaticResourceFilter.class);
     */
    filter(PATTERN_ALL).through(SSLFilter.class);
    filterRegex(RESOURCE_REGEX).through(GZipFilter.class);
    filter(PATTERN_RESTAPI, PATTERN_DEBUG).through(SecurityFilter.class);

    // debug servlet
    serve(PATTERN_DEBUG).with(DebugServlet.class);

    // plugin resources
    serve(PATTERN_PLUGIN_SCRIPT).with(ScriptResourceServlet.class);

    // jersey
    Map<String, String> params = new HashMap<String, String>();

    /*
     * params.put("com.sun.jersey.spi.container.ContainerRequestFilters",
     *          "com.sun.jersey.api.container.filter.LoggingFilter");
     * params.put("com.sun.jersey.spi.container.ContainerResponseFilters",
     *          "com.sun.jersey.api.container.filter.LoggingFilter");
     * params.put("com.sun.jersey.config.feature.Trace", "true");
     * params.put("com.sun.jersey.config.feature.TracePerRequest", "true");
     */
    params.put(JSONConfiguration.FEATURE_POJO_MAPPING, Boolean.TRUE.toString());
    params.put(ResourceConfig.FEATURE_REDIRECT, Boolean.TRUE.toString());
    params.put(ServletContainer.RESOURCE_CONFIG_CLASS,
               UriExtensionsConfig.class.getName());

    String restPath = getRestPackages();

    if (logger.isInfoEnabled())
    {
      logger.info("configure jersey with package path: {}", restPath);
    }

    params.put(PackagesResourceConfig.PROPERTY_PACKAGES, restPath);
    serve(PATTERN_RESTAPI).with(GuiceContainer.class, params);
  }

  /**
   * Method description
   *
   *
   * @param packageSet
   * @param plugin
   */
  private void appendPluginPackages(Set<String> packageSet, Plugin plugin)
  {
    Set<String> pluginPackageSet = plugin.getPackageSet();

    if (pluginPackageSet != null)
    {
      for (String pluginPkg : pluginPackageSet)
      {
        boolean append = true;

        for (String pkg : packageSet)
        {
          if (pluginPkg.startsWith(pkg))
          {
            append = false;

            break;
          }
        }

        if (append)
        {
          if (logger.isDebugEnabled())
          {
            String name = "unknown";

            if (plugin.getInformation() != null)
            {
              name = plugin.getInformation().getName();
            }

            logger.debug("plugin {} added rest path {}", name, pluginPkg);
          }

          packageSet.add(pluginPkg);
        }
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
  private String getRestPackages()
  {
    Set<String> packageSet = new HashSet<String>();

    packageSet.add(SCMContext.DEFAULT_PACKAGE);

    Collection<Plugin> plugins = pluginLoader.getInstalledPlugins();

    if (plugins != null)
    {
      for (Plugin plugin : plugins)
      {
        appendPluginPackages(packageSet, plugin);
      }
    }

    StringBuilder buffer = new StringBuilder();
    Iterator<String> pkgIterator = packageSet.iterator();

    while (pkgIterator.hasNext())
    {
      buffer.append(pkgIterator.next());

      if (pkgIterator.hasNext())
      {
        buffer.append(";");
      }
    }

    return buffer.toString();
  }

  /**
   * Method description
   *
   *
   * @param context
   *
   * @return
   */
  private ScmConfiguration getScmConfiguration(SCMContextProvider context)
  {
    ScmConfiguration config = null;
    File file = new File(context.getBaseDirectory(), ScmConfiguration.PATH);

    if (file.exists())
    {
      try
      {
        config = JAXB.unmarshal(file, ScmConfiguration.class);
      }
      catch (Exception ex)
      {
        logger.error(ex.getMessage(), ex);
      }
    }

    if (config == null)
    {
      config = new ScmConfiguration();
    }

    return config;
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private BindingExtensionProcessor bindExtProcessor;

  /** Field description */
  private PluginLoader pluginLoader;
}
