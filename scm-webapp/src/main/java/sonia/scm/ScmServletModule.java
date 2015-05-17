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

import com.google.inject.Provider;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.name.Names;
import com.google.inject.servlet.RequestScoped;
import com.google.inject.servlet.ServletModule;
import com.google.inject.throwingproviders.ThrowingProviderBinder;

import org.apache.shiro.authz.permission.PermissionResolver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sonia.scm.api.rest.UriExtensionsConfig;
import sonia.scm.cache.CacheManager;
import sonia.scm.cache.GuavaCacheManager;
import sonia.scm.config.ScmConfiguration;
import sonia.scm.event.ScmEventBus;
import sonia.scm.filter.AdminSecurityFilter;
import sonia.scm.filter.BaseUrlFilter;
import sonia.scm.filter.GZipFilter;
import sonia.scm.filter.MDCFilter;
import sonia.scm.filter.SecurityFilter;
import sonia.scm.group.DefaultGroupManager;
import sonia.scm.group.GroupDAO;
import sonia.scm.group.GroupManager;
import sonia.scm.group.GroupManagerProvider;
import sonia.scm.group.xml.XmlGroupDAO;
import sonia.scm.io.DefaultFileSystem;
import sonia.scm.io.FileSystem;
import sonia.scm.net.HttpClient;
import sonia.scm.net.URLHttpClient;
import sonia.scm.plugin.DefaultPluginLoader;
import sonia.scm.plugin.DefaultPluginManager;
import sonia.scm.plugin.Plugin;
import sonia.scm.plugin.PluginLoader;
import sonia.scm.plugin.PluginManager;
import sonia.scm.repository.ChangesetViewerUtil;
import sonia.scm.repository.DefaultRepositoryManager;
import sonia.scm.repository.DefaultRepositoryProvider;
import sonia.scm.repository.HealthCheckContextListener;
import sonia.scm.repository.LastModifiedUpdateListener;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryBrowserUtil;
import sonia.scm.repository.RepositoryDAO;
import sonia.scm.repository.RepositoryManager;
import sonia.scm.repository.RepositoryManagerProvider;
import sonia.scm.repository.RepositoryProvider;
import sonia.scm.repository.api.HookContextFactory;
import sonia.scm.repository.api.RepositoryServiceFactory;
import sonia.scm.repository.spi.HookEventFacade;
import sonia.scm.repository.xml.XmlRepositoryDAO;
import sonia.scm.resources.DefaultResourceManager;
import sonia.scm.resources.DevelopmentResourceManager;
import sonia.scm.resources.ResourceManager;
import sonia.scm.resources.ScriptResourceServlet;
import sonia.scm.security.CipherHandler;
import sonia.scm.security.CipherUtil;
import sonia.scm.security.ConfigurableLoginAttemptHandler;
import sonia.scm.security.DefaultKeyGenerator;
import sonia.scm.security.DefaultSecuritySystem;
import sonia.scm.security.EncryptionHandler;
import sonia.scm.security.KeyGenerator;
import sonia.scm.security.LoginAttemptHandler;
import sonia.scm.security.MessageDigestEncryptionHandler;
import sonia.scm.security.RepositoryPermissionResolver;
import sonia.scm.security.SecurityContext;
import sonia.scm.security.SecuritySystem;
import sonia.scm.store.BlobStoreFactory;
import sonia.scm.store.ConfigurationEntryStoreFactory;
import sonia.scm.store.DataStoreFactory;
import sonia.scm.store.FileBlobStoreFactory;
import sonia.scm.store.JAXBConfigurationEntryStoreFactory;
import sonia.scm.store.JAXBDataStoreFactory;
import sonia.scm.store.JAXBStoreFactory;
import sonia.scm.store.ListenableStoreFactory;
import sonia.scm.store.StoreFactory;
import sonia.scm.template.DefaultEngine;
import sonia.scm.template.FreemarkerTemplateEngine;
import sonia.scm.template.FreemarkerTemplateHandler;
import sonia.scm.template.MustacheTemplateEngine;
import sonia.scm.template.TemplateEngine;
import sonia.scm.template.TemplateEngineFactory;
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
import sonia.scm.user.UserManagerProvider;
import sonia.scm.user.xml.XmlUserDAO;
import sonia.scm.util.DebugServlet;
import sonia.scm.util.ScmConfigurationUtil;
import sonia.scm.web.cgi.CGIExecutorFactory;
import sonia.scm.web.cgi.DefaultCGIExecutorFactory;
import sonia.scm.web.filter.AutoLoginFilter;
import sonia.scm.web.filter.LoggingFilter;
import sonia.scm.web.security.AdministrationContext;
import sonia.scm.web.security.ApiBasicAuthenticationFilter;
import sonia.scm.web.security.AuthenticationManager;
import sonia.scm.web.security.BasicSecurityContext;
import sonia.scm.web.security.ChainAuthenticatonManager;
import sonia.scm.web.security.DefaultAdministrationContext;
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
import sonia.scm.net.ahc.AdvancedHttpClient;
import sonia.scm.net.ahc.ContentTransformer;
import sonia.scm.net.ahc.DefaultAdvancedHttpClient;
import sonia.scm.net.ahc.JsonContentTransformer;
import sonia.scm.net.ahc.XmlContentTransformer;
import sonia.scm.web.UserAgentParser;

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
  private static final Logger logger =
    LoggerFactory.getLogger(ScmServletModule.class);

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   *
   * @param pluginLoader
   * @param overrides
   */
  ScmServletModule(DefaultPluginLoader pluginLoader, ClassOverrides overrides)
  {
    this.pluginLoader = pluginLoader;
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

    // bind event api
    bind(ScmEventBus.class).toInstance(ScmEventBus.getInstance());

    // bind core
    bind(StoreFactory.class, JAXBStoreFactory.class);
    bind(ListenableStoreFactory.class, JAXBStoreFactory.class);
    bind(ConfigurationEntryStoreFactory.class,
      JAXBConfigurationEntryStoreFactory.class);
    bind(DataStoreFactory.class, JAXBDataStoreFactory.class);
    bind(BlobStoreFactory.class, FileBlobStoreFactory.class);
    bind(ScmConfiguration.class).toInstance(config);
    bind(PluginLoader.class).toInstance(pluginLoader);
    bind(PluginManager.class, DefaultPluginManager.class);

    // note CipherUtil uses an other generator
    bind(KeyGenerator.class).to(DefaultKeyGenerator.class);
    bind(CipherHandler.class).toInstance(cu.getCipherHandler());
    bind(EncryptionHandler.class, MessageDigestEncryptionHandler.class);
    bind(FileSystem.class, DefaultFileSystem.class);

    // bind health check stuff
    bind(HealthCheckContextListener.class);

    // bind extensions
    pluginLoader.processExtensions(binder());

    // bind security stuff
    bind(PermissionResolver.class, RepositoryPermissionResolver.class);
    bind(AuthenticationManager.class, ChainAuthenticatonManager.class);
    bind(SecurityContext.class).to(BasicSecurityContext.class);
    bind(WebSecurityContext.class).to(BasicSecurityContext.class);
    bind(SecuritySystem.class).to(DefaultSecuritySystem.class);
    bind(AdministrationContext.class, DefaultAdministrationContext.class);
    bind(LoginAttemptHandler.class, ConfigurableLoginAttemptHandler.class);

    // bind cache
    bind(CacheManager.class, GuavaCacheManager.class);

    // bind dao
    bind(GroupDAO.class, XmlGroupDAO.class);
    bind(UserDAO.class, XmlUserDAO.class);
    bind(RepositoryDAO.class, XmlRepositoryDAO.class);

    bindDecorated(RepositoryManager.class, DefaultRepositoryManager.class,
      RepositoryManagerProvider.class);
    bindDecorated(UserManager.class, DefaultUserManager.class,
      UserManagerProvider.class);
    bindDecorated(GroupManager.class, DefaultGroupManager.class,
      GroupManagerProvider.class);
    bind(CGIExecutorFactory.class, DefaultCGIExecutorFactory.class);
    bind(ChangesetViewerUtil.class);
    bind(RepositoryBrowserUtil.class);

    // bind httpclient
    bind(HttpClient.class, URLHttpClient.class);
    
    // bind ahc
    Multibinder<ContentTransformer> transformers =
      Multibinder.newSetBinder(binder(), ContentTransformer.class);
    transformers.addBinding().to(XmlContentTransformer.class);
    transformers.addBinding().to(JsonContentTransformer.class);
    bind(AdvancedHttpClient.class).to(DefaultAdvancedHttpClient.class);

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

    // bind repository service factory
    bind(RepositoryServiceFactory.class);

    // bind new hook api
    bind(HookContextFactory.class);
    bind(HookEventFacade.class);
    
    // bind user-agent parser
    bind(UserAgentParser.class);

    // bind debug logging filter
    if ("true".equalsIgnoreCase(System.getProperty(SYSTEM_PROPERTY_DEBUG_HTTP)))
    {
      filter(PATTERN_ALL).through(LoggingFilter.class);
    }

    /*
     * filter(PATTERN_PAGE,
     *      PATTERN_STATIC_RESOURCES).through(StaticResourceFilter.class);
     */
    filter(PATTERN_ALL).through(BaseUrlFilter.class);
    filter(PATTERN_ALL).through(AutoLoginFilter.class);
    filterRegex(RESOURCE_REGEX).through(GZipFilter.class);
    filter(PATTERN_RESTAPI,
      PATTERN_DEBUG).through(ApiBasicAuthenticationFilter.class);
    filter(PATTERN_RESTAPI, PATTERN_DEBUG).through(SecurityFilter.class);
    filter(PATTERN_CONFIG, PATTERN_ADMIN).through(AdminSecurityFilter.class);

    // added mdcs for logging
    filter(PATTERN_ALL).through(MDCFilter.class);

    // debug servlet
    serve(PATTERN_DEBUG).with(DebugServlet.class);

    // plugin resources
    serve(PATTERN_PLUGIN_SCRIPT).with(ScriptResourceServlet.class);

    // template
    bind(TemplateHandler.class).to(FreemarkerTemplateHandler.class);
    serve(PATTERN_INDEX, "/").with(TemplateServlet.class);

    Multibinder<TemplateEngine> engineBinder =
      Multibinder.newSetBinder(binder(), TemplateEngine.class);

    engineBinder.addBinding().to(MustacheTemplateEngine.class);
    engineBinder.addBinding().to(FreemarkerTemplateEngine.class);
    bind(TemplateEngine.class).annotatedWith(DefaultEngine.class).to(
      MustacheTemplateEngine.class);
    bind(TemplateEngineFactory.class);

    // bind events
    bind(LastModifiedUpdateListener.class);

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
    params.put(ResourceConfig.FEATURE_DISABLE_WADL, Boolean.TRUE.toString());
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
    Class<? extends T> implementation = find(clazz, defaultImplementation);

    if (logger.isDebugEnabled())
    {
      logger.debug("bind {} to {}", clazz, implementation);
    }

    bind(clazz).to(implementation);
  }

  /**
   * Method description
   *
   *
   * @param clazz
   * @param defaultImplementation
   * @param providerClass
   * @param <T>
   */
  private <T> void bindDecorated(Class<T> clazz,
    Class<? extends T> defaultImplementation,
    Class<? extends Provider<T>> providerClass)
  {
    Class<? extends T> implementation = find(clazz, defaultImplementation);

    if (logger.isDebugEnabled())
    {
      logger.debug("bind undecorated {} to {}", clazz, implementation);
    }

    bind(clazz).annotatedWith(Undecorated.class).to(implementation);

    if (logger.isDebugEnabled())
    {
      logger.debug("bind {} to provider {}", clazz, providerClass);
    }

    bind(clazz).toProvider(providerClass);
  }

  /**
   * Method description
   *
   *
   * @param clazz
   * @param defaultImplementation
   * @param <T>
   *
   * @return
   */
  private <T> Class<? extends T> find(Class<T> clazz,
    Class<? extends T> defaultImplementation)
  {
    Class<? extends T> implementation = overrides.getOverride(clazz);

    if (implementation != null)
    {
      logger.info("found override {} for {}", implementation, clazz);
    }
    else
    {
      implementation = defaultImplementation;

      if (logger.isTraceEnabled())
      {
        logger.trace(
          "no override available for {}, using default implementation {}",
          clazz, implementation);
      }
    }

    return implementation;
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
  private ClassOverrides overrides;

  /** Field description */
  private DefaultPluginLoader pluginLoader;
}
