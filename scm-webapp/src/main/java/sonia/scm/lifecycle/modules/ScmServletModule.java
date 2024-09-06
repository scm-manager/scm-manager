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

package sonia.scm.lifecycle.modules;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Provider;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.servlet.RequestScoped;
import com.google.inject.servlet.ServletModule;
import com.google.inject.throwingproviders.ThrowingProviderBinder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.Default;
import sonia.scm.DefaultRootURL;
import sonia.scm.PushStateDispatcher;
import sonia.scm.PushStateDispatcherProvider;
import sonia.scm.RootURL;
import sonia.scm.Undecorated;
import sonia.scm.admin.ScmConfigurationStore;
import sonia.scm.api.rest.ObjectMapperProvider;
import sonia.scm.api.v2.resources.BranchLinkProvider;
import sonia.scm.api.v2.resources.DefaultBranchLinkProvider;
import sonia.scm.api.v2.resources.DefaultRepositoryLinkProvider;
import sonia.scm.api.v2.resources.RepositoryLinkProvider;
import sonia.scm.auditlog.AuditLogConfigurationStoreDecoratorFactory;
import sonia.scm.config.ScmConfiguration;
import sonia.scm.event.ScmEventBus;
import sonia.scm.group.DefaultGroupCollector;
import sonia.scm.group.DefaultGroupDisplayManager;
import sonia.scm.group.DefaultGroupManager;
import sonia.scm.group.GroupCollector;
import sonia.scm.group.GroupDAO;
import sonia.scm.group.GroupDisplayManager;
import sonia.scm.group.GroupManager;
import sonia.scm.group.GroupManagerProvider;
import sonia.scm.group.xml.XmlGroupDAO;
import sonia.scm.importexport.FullScmRepositoryExporter;
import sonia.scm.importexport.FullScmRepositoryImporter;
import sonia.scm.initialization.DefaultInitializationFinisher;
import sonia.scm.initialization.InitializationCookieIssuer;
import sonia.scm.initialization.InitializationFinisher;
import sonia.scm.io.ContentTypeResolver;
import sonia.scm.io.DefaultContentTypeResolver;
import sonia.scm.migration.MigrationDAO;
import sonia.scm.net.SSLContextProvider;
import sonia.scm.net.TrustManagerProvider;
import sonia.scm.net.ahc.AdvancedHttpClient;
import sonia.scm.net.ahc.ContentTransformer;
import sonia.scm.net.ahc.DefaultAdvancedHttpClient;
import sonia.scm.net.ahc.JsonContentTransformer;
import sonia.scm.net.ahc.XmlContentTransformer;
import sonia.scm.notifications.DefaultNotificationSender;
import sonia.scm.notifications.NotificationSender;
import sonia.scm.plugin.DefaultPluginManager;
import sonia.scm.plugin.PluginLoader;
import sonia.scm.plugin.PluginManager;
import sonia.scm.repository.DefaultHealthCheckService;
import sonia.scm.repository.DefaultNamespaceManager;
import sonia.scm.repository.DefaultRepositoryManager;
import sonia.scm.repository.DefaultRepositoryProvider;
import sonia.scm.repository.DefaultRepositoryRoleManager;
import sonia.scm.repository.FullRepositoryExporter;
import sonia.scm.repository.FullRepositoryImporter;
import sonia.scm.repository.HealthCheckContextListener;
import sonia.scm.repository.HealthCheckService;
import sonia.scm.repository.NamespaceManager;
import sonia.scm.repository.NamespaceStrategy;
import sonia.scm.repository.NamespaceStrategyProvider;
import sonia.scm.repository.PermissionProvider;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryDAO;
import sonia.scm.repository.RepositoryManager;
import sonia.scm.repository.RepositoryManagerProvider;
import sonia.scm.repository.RepositoryProvider;
import sonia.scm.repository.RepositoryRoleDAO;
import sonia.scm.repository.RepositoryRoleManager;
import sonia.scm.repository.api.HookContextFactory;
import sonia.scm.repository.api.RepositoryServiceFactory;
import sonia.scm.repository.spi.HookEventFacade;
import sonia.scm.repository.xml.XmlRepositoryDAO;
import sonia.scm.repository.xml.XmlRepositoryRoleDAO;
import sonia.scm.schedule.CronScheduler;
import sonia.scm.schedule.Scheduler;
import sonia.scm.search.DefaultIndexLogStore;
import sonia.scm.search.IndexLogStore;
import sonia.scm.search.LuceneSearchEngine;
import sonia.scm.search.SearchEngine;
import sonia.scm.security.AccessTokenCookieIssuer;
import sonia.scm.security.AuthorizationChangedEventProducer;
import sonia.scm.security.ConfigurableLoginAttemptHandler;
import sonia.scm.security.DefaultAccessTokenCookieIssuer;
import sonia.scm.security.DefaultSecuritySystem;
import sonia.scm.security.LoginAttemptHandler;
import sonia.scm.security.RepositoryPermissionProvider;
import sonia.scm.security.SecuritySystem;
import sonia.scm.store.ConfigurationStoreDecoratorFactory;
import sonia.scm.store.FileStoreExporter;
import sonia.scm.store.StoreExporter;
import sonia.scm.template.MustacheTemplateEngine;
import sonia.scm.template.TemplateEngine;
import sonia.scm.template.TemplateEngineFactory;
import sonia.scm.template.TemplateServlet;
import sonia.scm.update.repository.DefaultMigrationStrategyDAO;
import sonia.scm.user.DefaultUserDisplayManager;
import sonia.scm.user.DefaultUserManager;
import sonia.scm.user.UserDAO;
import sonia.scm.user.UserDisplayManager;
import sonia.scm.user.UserManager;
import sonia.scm.user.UserManagerProvider;
import sonia.scm.user.xml.XmlUserDAO;
import sonia.scm.util.DebugServlet;
import sonia.scm.web.UserAgentParser;
import sonia.scm.web.cgi.CGIExecutorFactory;
import sonia.scm.web.cgi.DefaultCGIExecutorFactory;
import sonia.scm.web.filter.LoggingFilter;
import sonia.scm.web.security.AdministrationContext;
import sonia.scm.web.security.DefaultAdministrationContext;
import sonia.scm.work.CentralWorkQueue;
import sonia.scm.work.DefaultCentralWorkQueue;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;


class ScmServletModule extends ServletModule {

  private static final String PATTERN_ALL = "/*";
  private static final String PATTERN_DEBUG = "/debug.html";
  private static final String PATTERN_INDEX = "/index.html";
  private static final String SYSTEM_PROPERTY_DEBUG_HTTP = "scm.debug.http";

  private static final Logger logger = LoggerFactory.getLogger(ScmServletModule.class);

  private final ClassOverrides overrides;
  private final PluginLoader pluginLoader;

  ScmServletModule(PluginLoader pluginLoader, ClassOverrides overrides) {
    this.pluginLoader = pluginLoader;
    this.overrides = overrides;
  }

  @Override
  protected void configureServlets() {
    install(ThrowingProviderBinder.forModule(this));

    bind(NamespaceStrategy.class).toProvider(NamespaceStrategyProvider.class);

    // bind store decorators
    Multibinder<ConfigurationStoreDecoratorFactory> storeDecoratorMultiBinder = Multibinder.newSetBinder(binder(), ConfigurationStoreDecoratorFactory.class);
    storeDecoratorMultiBinder.addBinding().to(AuditLogConfigurationStoreDecoratorFactory.class);

    // bind repository provider
    ThrowingProviderBinder.create(binder())
      .bind(RepositoryProvider.class, Repository.class)
      .to(DefaultRepositoryProvider.class)
      .in(RequestScoped.class);

    // bind event api
    bind(ScmEventBus.class).toInstance(ScmEventBus.getInstance());

    // bind core
    bind(ScmConfigurationStore.class);
    bind(ScmConfiguration.class).toInstance(new ScmConfigurationLoader().load());
    bind(PluginManager.class, DefaultPluginManager.class);

    // bind scheduler
    bind(Scheduler.class).to(CronScheduler.class);

    // bind health check stuff
    bind(HealthCheckContextListener.class);

    // bind extensions
    pluginLoader.getExtensionProcessor().processAutoBindExtensions(binder());


    // bind security stuff
    bind(LoginAttemptHandler.class).to(ConfigurableLoginAttemptHandler.class);
    bind(AuthorizationChangedEventProducer.class);

    bind(SecuritySystem.class).to(DefaultSecuritySystem.class);
    bind(AdministrationContext.class, DefaultAdministrationContext.class);

    // bind dao
    bind(GroupDAO.class, XmlGroupDAO.class);
    bind(UserDAO.class, XmlUserDAO.class);
    bind(RepositoryDAO.class, XmlRepositoryDAO.class);
    bind(RepositoryRoleDAO.class, XmlRepositoryRoleDAO.class);
    bind(RepositoryRoleManager.class).to(DefaultRepositoryRoleManager.class);
    bind(MigrationDAO.class).to(DefaultMigrationStrategyDAO.class);

    bindDecorated(RepositoryManager.class, DefaultRepositoryManager.class,
      RepositoryManagerProvider.class);
    bindDecorated(UserManager.class, DefaultUserManager.class,
      UserManagerProvider.class);
    bind(UserDisplayManager.class, DefaultUserDisplayManager.class);
    bindDecorated(GroupManager.class, DefaultGroupManager.class,
      GroupManagerProvider.class);
    bind(GroupDisplayManager.class, DefaultGroupDisplayManager.class);
    bind(NamespaceManager.class, DefaultNamespaceManager.class);
    bind(GroupCollector.class, DefaultGroupCollector.class);
    bind(CGIExecutorFactory.class, DefaultCGIExecutorFactory.class);
    bind(StoreExporter.class, FileStoreExporter.class);

    // bind ssl context provider
    bind(SSLContext.class).toProvider(SSLContextProvider.class);

    // bind trust manager provider
    bind(TrustManager.class).toProvider(TrustManagerProvider.class);

    // bind ahc
    Multibinder<ContentTransformer> transformers =
      Multibinder.newSetBinder(binder(), ContentTransformer.class);
    transformers.addBinding().to(XmlContentTransformer.class);
    transformers.addBinding().to(JsonContentTransformer.class);
    bind(AdvancedHttpClient.class).to(DefaultAdvancedHttpClient.class);

    // bind repository service factory
    bind(RepositoryServiceFactory.class);

    // bind new hook api
    bind(HookContextFactory.class);
    bind(HookEventFacade.class);

    // bind user-agent parser
    bind(UserAgentParser.class);

    // bind debug logging filter
    if ("true".equalsIgnoreCase(System.getProperty(SYSTEM_PROPERTY_DEBUG_HTTP))) {
      filter(PATTERN_ALL).through(LoggingFilter.class);
    }

    // debug servlet
    serve(PATTERN_DEBUG).with(DebugServlet.class);

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

    bind(AccessTokenCookieIssuer.class).to(DefaultAccessTokenCookieIssuer.class);
    bind(InitializationCookieIssuer.class).to(DefaultAccessTokenCookieIssuer.class);
    bind(PushStateDispatcher.class).toProvider(PushStateDispatcherProvider.class);

    // bind api link provider
    bind(BranchLinkProvider.class).to(DefaultBranchLinkProvider.class);
    bind(RepositoryLinkProvider.class).to(DefaultRepositoryLinkProvider.class);

    // bind url helper
    bind(RootURL.class).to(DefaultRootURL.class);

    bind(PermissionProvider.class).to(RepositoryPermissionProvider.class);

    bind(HealthCheckService.class).to(DefaultHealthCheckService.class);

    bind(NotificationSender.class).to(DefaultNotificationSender.class);

    bind(InitializationFinisher.class).to(DefaultInitializationFinisher.class);

    // bind search stuff
    bind(SearchEngine.class, LuceneSearchEngine.class);
    bind(IndexLogStore.class, DefaultIndexLogStore.class);

    bind(CentralWorkQueue.class, DefaultCentralWorkQueue.class);

    bind(ContentTypeResolver.class).to(DefaultContentTypeResolver.class);

    bind(FullRepositoryImporter.class).to(FullScmRepositoryImporter.class);
    bind(FullRepositoryExporter.class).to(FullScmRepositoryExporter.class);
  }

  private <T> void bind(Class<T> clazz, Class<? extends T> defaultImplementation) {
    Class<? extends T> implementation = find(clazz, defaultImplementation);
    logger.debug("bind {} to {}", clazz, implementation);
    bind(clazz).to(implementation);
  }

  private <T> void bindDecorated(
    Class<T> clazz, Class<? extends T> defaultImplementation, Class<? extends Provider<T>> providerClass
  ) {
    Class<? extends T> implementation = find(clazz, defaultImplementation);

    logger.debug("bind undecorated {} to {}", clazz, implementation);
    bind(clazz).annotatedWith(Undecorated.class).to(implementation);

    logger.debug("bind {} to provider {}", clazz, providerClass);
    bind(clazz).toProvider(providerClass);
  }

  private <T> Class<? extends T> find(Class<T> clazz, Class<? extends T> defaultImplementation) {
    Class<? extends T> implementation = overrides.getOverride(clazz);

    if (implementation != null) {
      logger.info("found override {} for {}", implementation, clazz);
    } else {
      implementation = defaultImplementation;
      logger.trace("no override available for {}, using default implementation {}", clazz, implementation);
    }

    return implementation;
  }
}
