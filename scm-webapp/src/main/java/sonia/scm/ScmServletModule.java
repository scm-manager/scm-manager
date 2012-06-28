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

import com.google.inject.name.Names;
import com.google.inject.servlet.RequestScoped;
import com.google.inject.servlet.ServletModule;
import com.google.inject.throwingproviders.ThrowingProviderBinder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sonia.scm.api.rest.UriExtensionsConfig;
import sonia.scm.cache.CacheManager;
import sonia.scm.cache.EhCacheManager;
import sonia.scm.config.ScmConfiguration;
import sonia.scm.filter.AdminSecurityFilter;
import sonia.scm.filter.BaseUrlFilter;
import sonia.scm.filter.GZipFilter;
import sonia.scm.filter.SecurityFilter;
import sonia.scm.group.DefaultGroupManager;
import sonia.scm.group.GroupDAO;
import sonia.scm.group.GroupManager;
import sonia.scm.group.xml.XmlGroupDAO;
import sonia.scm.io.DefaultFileSystem;
import sonia.scm.io.FileSystem;
import sonia.scm.net.HttpClient;
import sonia.scm.net.URLHttpClient;
import sonia.scm.plugin.DefaultPluginManager;
import sonia.scm.plugin.Plugin;
import sonia.scm.plugin.PluginLoader;
import sonia.scm.plugin.PluginManager;
import sonia.scm.repository.ChangesetViewerUtil;
import sonia.scm.repository.DefaultRepositoryManager;
import sonia.scm.repository.DefaultRepositoryProvider;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryBrowserUtil;
import sonia.scm.repository.RepositoryDAO;
import sonia.scm.repository.RepositoryManager;
import sonia.scm.repository.RepositoryProvider;
import sonia.scm.repository.xml.XmlRepositoryDAO;
import sonia.scm.resources.DefaultResourceManager;
import sonia.scm.resources.DevelopmentResourceManager;
import sonia.scm.resources.ResourceManager;
import sonia.scm.resources.ScriptResourceServlet;
import sonia.scm.security.CipherHandler;
import sonia.scm.security.CipherUtil;
import sonia.scm.security.EncryptionHandler;
import sonia.scm.security.KeyGenerator;
import sonia.scm.security.MessageDigestEncryptionHandler;
import sonia.scm.security.SecurityContext;
import sonia.scm.store.JAXBStoreFactory;
import sonia.scm.store.ListenableStoreFactory;
import sonia.scm.store.StoreFactory;
import sonia.scm.template.FreemarkerTemplateHandler;
import sonia.scm.template.TemplateHandler;
import sonia.scm.template.TemplateServlet;
import sonia.scm.url.RestJsonUrlProvider;
import sonia.scm.url.RestXmlUrlProvider;
import sonia.scm.url.UrlProvider;
import sonia.scm.url.UrlProviderFactory;
import sonia.scm.url.WebUIUrlProvider;
import sonia.scm.user.DefaultUserManager;
import sonia.scm.user.UserDAO;
import sonia.scm.user.UserManager;
import sonia.scm.user.xml.XmlUserDAO;
import sonia.scm.util.DebugServlet;
import sonia.scm.util.ScmConfigurationUtil;
import sonia.scm.web.cgi.CGIExecutorFactory;
import sonia.scm.web.cgi.DefaultCGIExecutorFactory;
import sonia.scm.web.filter.LoggingFilter;
import sonia.scm.web.security.AdministrationContext;
import sonia.scm.web.security.ApiBasicAuthenticationFilter;
import sonia.scm.web.security.AuthenticationManager;
import sonia.scm.web.security.BasicSecurityContext;
import sonia.scm.web.security.ChainAuthenticatonManager;
import sonia.scm.web.security.DefaultAdministrationContext;
import sonia.scm.web.security.LocalSecurityContextHolder;
import sonia.scm.web.security.SecurityContextProvider;
import sonia.scm.web.security.WebSecurityContext;

//~--- JDK imports ------------------------------------------------------------

import com.sun.jersey.api.core.PackagesResourceConfig;
import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.api.json.JSONConfiguration;
import com.sun.jersey.guice.spi.container.servlet.GuiceContainer;
import com.sun.jersey.spi.container.servlet.ServletContainer;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Sebastian Sdorra
 */
public class ScmServletModule extends ServletModule
{

  /** Field description */
  public static final String[] PATTERN_ADMIN = new String[] {
                                                 "/api/rest/groups*",
          "/api/rest/users*", "/api/rest/plguins*" };

  /** Field description */
  public static final String PATTERN_ALL = "/*";

  /** Field description */
  public static final String PATTERN_CONFIG = "/api/rest/config*";

  /** Field description */
  public static final String PATTERN_DEBUG = "/debug.html";

  /** Field description */
  public static final String PATTERN_INDEX = "/index.html";

  /** Field description */
  public static final String PATTERN_PAGE = "*.html";

  /** Field description */
  public static final String PATTERN_PLUGIN_SCRIPT = "/plugins/resources/js/*";

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
  public static final String SYSTEM_PROPERTY_DEBUG_HTTP = "scm.debug.http";

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
   * @param overrides
   */
  ScmServletModule(PluginLoader pluginLoader,
                   BindingExtensionProcessor bindExtProcessor,
                   ClassOverrides overrides)
  {
    this.pluginLoader = pluginLoader;
    this.bindExtProcessor = bindExtProcessor;
    this.overrides = overrides;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   */
  @Override
  protected void configureServlets()
  {
    install(ThrowingProviderBinder.forModule(this));

    SCMContextProvider context = SCMContext.getContext();

    bind(SCMContextProvider.class).toInstance(context);

    ScmConfiguration config = getScmConfiguration(context);
    CipherUtil cu = CipherUtil.getInstance();

    // bind repository provider
    ThrowingProviderBinder.create(binder()).bind(
        RepositoryProvider.class, Repository.class).to(
        DefaultRepositoryProvider.class).in(RequestScoped.class);

    // bind core
    bind(StoreFactory.class, JAXBStoreFactory.class);
    bind(ListenableStoreFactory.class, JAXBStoreFactory.class);
    bind(ScmConfiguration.class).toInstance(config);
    bind(PluginLoader.class).toInstance(pluginLoader);
    bind(PluginManager.class, DefaultPluginManager.class);
    bind(KeyGenerator.class).toInstance(cu.getKeyGenerator());
    bind(CipherHandler.class).toInstance(cu.getCipherHandler());
    bind(EncryptionHandler.class, MessageDigestEncryptionHandler.class);
    bindExtProcessor.bindExtensions(binder());

    Class<? extends FileSystem> fileSystem =
      bindExtProcessor.getFileSystemClass();

    if (fileSystem == null)
    {
      fileSystem = DefaultFileSystem.class;
    }

    bind(FileSystem.class).to(fileSystem);

    // bind security stuff
    bind(AuthenticationManager.class, ChainAuthenticatonManager.class);
    bind(LocalSecurityContextHolder.class);
    bind(WebSecurityContext.class).annotatedWith(Names.named("userSession")).to(
        BasicSecurityContext.class);
    bind(SecurityContext.class).toProvider(SecurityContextProvider.class);
    bind(WebSecurityContext.class).toProvider(SecurityContextProvider.class);
    bind(AdministrationContext.class, DefaultAdministrationContext.class);

    // bind security cache
    bind(CacheManager.class, EhCacheManager.class);

    // bind dao
    bind(GroupDAO.class, XmlGroupDAO.class);
    bind(UserDAO.class, XmlUserDAO.class);
    bind(RepositoryDAO.class, XmlRepositoryDAO.class);

    // bind(RepositoryManager.class).annotatedWith(Undecorated.class).to(
    // BasicRepositoryManager.class);
    bind(RepositoryManager.class, DefaultRepositoryManager.class);
    bind(UserManager.class, DefaultUserManager.class);
    bind(GroupManager.class, DefaultGroupManager.class);
    bind(CGIExecutorFactory.class, DefaultCGIExecutorFactory.class);
    bind(ChangesetViewerUtil.class);
    bind(RepositoryBrowserUtil.class);

    // bind httpclient
    bind(HttpClient.class, URLHttpClient.class);

    // bind resourcemanager
    if (context.getStage() == Stage.DEVELOPMENT)
    {
      bind(ResourceManager.class, DevelopmentResourceManager.class);
    }
    else
    {
      bind(ResourceManager.class, DefaultResourceManager.class);
    }

    // bind url provider staff
    bind(UrlProvider.class).annotatedWith(
        Names.named(UrlProviderFactory.TYPE_RESTAPI_JSON)).toProvider(
        RestJsonUrlProvider.class);
    bind(UrlProvider.class).annotatedWith(
        Names.named(UrlProviderFactory.TYPE_RESTAPI_XML)).toProvider(
        RestXmlUrlProvider.class);
    bind(UrlProvider.class).annotatedWith(
        Names.named(UrlProviderFactory.TYPE_WUI)).toProvider(
        WebUIUrlProvider.class);

    if ("true".equalsIgnoreCase(System.getProperty(SYSTEM_PROPERTY_DEBUG_HTTP)))
    {
      filter(PATTERN_ALL).through(LoggingFilter.class);
    }

    /*
     * filter(PATTERN_PAGE,
     *      PATTERN_STATIC_RESOURCES).through(StaticResourceFilter.class);
     */
    filter(PATTERN_ALL).through(BaseUrlFilter.class);
    filterRegex(RESOURCE_REGEX).through(GZipFilter.class);
    filter(PATTERN_RESTAPI,
           PATTERN_DEBUG).through(ApiBasicAuthenticationFilter.class);
    filter(PATTERN_RESTAPI, PATTERN_DEBUG).through(SecurityFilter.class);
    filter(PATTERN_CONFIG, PATTERN_ADMIN).through(AdminSecurityFilter.class);

    // debug servlet
    serve(PATTERN_DEBUG).with(DebugServlet.class);

    // plugin resources
    serve(PATTERN_PLUGIN_SCRIPT).with(ScriptResourceServlet.class);

    // template
    bind(TemplateHandler.class).to(FreemarkerTemplateHandler.class);
    serve(PATTERN_INDEX, "/").with(TemplateServlet.class);

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

  /**
   * Method description
   *
   *
   * @param clazz
   * @param defaultImplementation
   * @param <T>
   */
  private <T> void bind(Class<T> clazz,
                        Class<? extends T> defaultImplementation)
  {
    Class<? extends T> implementation = overrides.getOverride(clazz);

    if (implementation != null)
    {
      logger.info("bind {} to override {}", clazz, implementation);
    }
    else
    {
      implementation = defaultImplementation;

      if (logger.isDebugEnabled())
      {
        logger.debug("bind {} to default implementation {}", clazz,
                     implementation);
      }
    }

    bind(clazz).to(implementation);
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
   * Load ScmConfiguration with JAXB
   *
   *
   * @param context
   *
   * @return
   */
  private ScmConfiguration getScmConfiguration(SCMContextProvider context)
  {
    ScmConfiguration configuration = new ScmConfiguration();

    ScmConfigurationUtil.getInstance().load(configuration);

    return configuration;
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private BindingExtensionProcessor bindExtProcessor;

  /** Field description */
  private ClassOverrides overrides;

  /** Field description */
  private PluginLoader pluginLoader;
}
