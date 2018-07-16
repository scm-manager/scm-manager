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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Provider;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.servlet.RequestScoped;
import com.google.inject.servlet.ServletModule;
import com.google.inject.throwingproviders.ThrowingProviderBinder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.api.rest.ObjectMapperProvider;
import sonia.scm.cache.CacheManager;
import sonia.scm.cache.GuavaCacheManager;
import sonia.scm.config.ScmConfiguration;
import sonia.scm.event.ScmEventBus;
import sonia.scm.group.DefaultGroupManager;
import sonia.scm.group.GroupDAO;
import sonia.scm.group.GroupManager;
import sonia.scm.group.GroupManagerProvider;
import sonia.scm.group.xml.XmlGroupDAO;
import sonia.scm.io.DefaultFileSystem;
import sonia.scm.io.FileSystem;
import sonia.scm.net.SSLContextProvider;
import sonia.scm.net.ahc.*;
import sonia.scm.plugin.*;
import sonia.scm.repository.*;
import sonia.scm.repository.api.HookContextFactory;
import sonia.scm.repository.api.RepositoryServiceFactory;
import sonia.scm.repository.spi.HookEventFacade;
import sonia.scm.repository.xml.XmlRepositoryDAO;
import sonia.scm.resources.DefaultResourceManager;
import sonia.scm.resources.DevelopmentResourceManager;
import sonia.scm.resources.ResourceManager;
import sonia.scm.resources.ScriptResourceServlet;
import sonia.scm.schedule.QuartzScheduler;
import sonia.scm.schedule.Scheduler;
import sonia.scm.security.*;
import sonia.scm.store.*;
import sonia.scm.template.MustacheTemplateEngine;
import sonia.scm.template.TemplateEngine;
import sonia.scm.template.TemplateEngineFactory;
import sonia.scm.template.TemplateServlet;
import sonia.scm.user.DefaultUserManager;
import sonia.scm.user.UserDAO;
import sonia.scm.user.UserManager;
import sonia.scm.user.UserManagerProvider;
import sonia.scm.user.xml.XmlUserDAO;
import sonia.scm.util.DebugServlet;
import sonia.scm.util.ScmConfigurationUtil;
import sonia.scm.web.UserAgentParser;
import sonia.scm.web.cgi.CGIExecutorFactory;
import sonia.scm.web.cgi.DefaultCGIExecutorFactory;
import sonia.scm.web.filter.LoggingFilter;
import sonia.scm.web.security.AdministrationContext;
import sonia.scm.web.security.DefaultAdministrationContext;

import javax.net.ssl.SSLContext;
import javax.servlet.ServletContext;

//~--- JDK imports ------------------------------------------------------------

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
   * @param servletContext
   * @param pluginLoader
   * @param overrides
   * @param extensionProcessor
   */
  ScmServletModule(ServletContext servletContext,
    DefaultPluginLoader pluginLoader, ClassOverrides overrides, ExtensionProcessor extensionProcessor)
  {
    this.servletContext = servletContext;
    this.pluginLoader = pluginLoader;
    this.overrides = overrides;
    this.extensionProcessor = extensionProcessor;
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

    ScmConfiguration config = getScmConfiguration();
    CipherUtil cu = CipherUtil.getInstance();
    
    // bind repository provider
    ThrowingProviderBinder.create(binder()).bind(
      RepositoryProvider.class, Repository.class).to(
      DefaultRepositoryProvider.class).in(RequestScoped.class);

    // bind servlet context
    bind(ServletContext.class).annotatedWith(Default.class).toInstance(
      servletContext);

    // bind event api
    bind(ScmEventBus.class).toInstance(ScmEventBus.getInstance());

    // bind core
    bind(ConfigurationStoreFactory.class, JAXBConfigurationStoreFactory.class);
    bind(ConfigurationEntryStoreFactory.class, JAXBConfigurationEntryStoreFactory.class);
    bind(DataStoreFactory.class, JAXBDataStoreFactory.class);
    bind(BlobStoreFactory.class, FileBlobStoreFactory.class);
    bind(ScmConfiguration.class).toInstance(config);
    bind(PluginLoader.class).toInstance(pluginLoader);
    bind(PluginManager.class, DefaultPluginManager.class);

    // bind scheduler
    bind(Scheduler.class).to(QuartzScheduler.class);
    
    // note CipherUtil uses an other generator
    bind(KeyGenerator.class).to(DefaultKeyGenerator.class);
    bind(CipherHandler.class).toInstance(cu.getCipherHandler());
    bind(FileSystem.class, DefaultFileSystem.class);

    // bind health check stuff
    bind(HealthCheckContextListener.class);

    // bind extensions
    pluginLoader.getExtensionProcessor().processAutoBindExtensions(binder());

    // bind security stuff
    bind(LoginAttemptHandler.class).to(ConfigurableLoginAttemptHandler.class);
    bind(AuthorizationChangedEventProducer.class);

    bind(SecuritySystem.class).to(DefaultSecuritySystem.class);
    bind(AdministrationContext.class, DefaultAdministrationContext.class);

    // bind cache
    bind(CacheManager.class, GuavaCacheManager.class);
    bind(org.apache.shiro.cache.CacheManager.class, GuavaCacheManager.class);

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

    // bind sslcontext provider
    bind(SSLContext.class).toProvider(SSLContextProvider.class);
    
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
    
    // debug servlet
    serve(PATTERN_DEBUG).with(DebugServlet.class);

    // plugin resources
    serve(PATTERN_PLUGIN_SCRIPT).with(ScriptResourceServlet.class);

    // template
    serve(PATTERN_INDEX, "/").with(TemplateServlet.class);

    Multibinder<TemplateEngine> engineBinder =
      Multibinder.newSetBinder(binder(), TemplateEngine.class);

    engineBinder.addBinding().to(MustacheTemplateEngine.class);
    bind(TemplateEngine.class).annotatedWith(Default.class).to(
      MustacheTemplateEngine.class);
    bind(TemplateEngineFactory.class);
    bind(ObjectMapper.class).toProvider(ObjectMapperProvider.class);

    // bind events
    // bind(LastModifiedUpdateListener.class);

    Class<? extends NamespaceStrategy> namespaceStrategy = extensionProcessor.byExtensionPoint(NamespaceStrategy.class).iterator().next();
    bind(NamespaceStrategy.class, namespaceStrategy);
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
   * Load ScmConfiguration with JAXB
   *
   *
   * @param context
   *
   * @return
   */
  private ScmConfiguration getScmConfiguration()
  {
    ScmConfiguration configuration = new ScmConfiguration();

    ScmConfigurationUtil.getInstance().load(configuration);

    return configuration;
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private final ClassOverrides overrides;

  /** Field description */
  private final DefaultPluginLoader pluginLoader;

  /** Field description */
  private final ServletContext servletContext;

  private final ExtensionProcessor extensionProcessor;
}
