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

package sonia.scm.lifecycle;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.servlet.GuiceServletContextListener;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.SCMContext;
import sonia.scm.config.LoggingConfiguration;
import sonia.scm.lifecycle.classloading.ClassLoaderLifeCycle;
import sonia.scm.lifecycle.modules.ApplicationModuleProvider;
import sonia.scm.lifecycle.modules.BootstrapModule;
import sonia.scm.lifecycle.modules.CloseableModule;
import sonia.scm.lifecycle.modules.ConfigModule;
import sonia.scm.lifecycle.modules.EagerSingletonModule;
import sonia.scm.lifecycle.modules.InjectionLifeCycle;
import sonia.scm.lifecycle.modules.ModuleProvider;
import sonia.scm.lifecycle.modules.ScmEventBusModule;
import sonia.scm.lifecycle.modules.ScmInitializerModule;
import sonia.scm.lifecycle.modules.ServletContextModule;
import sonia.scm.lifecycle.modules.UpdateStepModule;
import sonia.scm.lifecycle.view.SingleView;
import sonia.scm.plugin.ConfigurationResolver;
import sonia.scm.plugin.PluginLoader;
import sonia.scm.update.MigrationWizardModuleProvider;
import sonia.scm.update.UpdateEngine;

import java.util.ArrayList;
import java.util.List;


public class BootstrapContextListener extends GuiceServletContextListener {

  private static final Logger LOG = LoggerFactory.getLogger(BootstrapContextListener.class);

  private final ClassLoaderLifeCycle classLoaderLifeCycle = ClassLoaderLifeCycle.create();

  private ServletContext context;
  private InjectionLifeCycle injectionLifeCycle;

  @Override
  public void contextInitialized(ServletContextEvent sce) {
    LOG.info("start scm-manager initialization");

    context = sce.getServletContext();
    classLoaderLifeCycle.initialize();
    super.contextInitialized(sce);

    configureLoggers();

    Injector injector = (Injector) context.getAttribute(Injector.class.getName());
    injectionLifeCycle = new InjectionLifeCycle(injector);
    injectionLifeCycle.initialize();
  }

  @Override
  protected Injector getInjector() {
    ConfigurationResolver configurationResolver = new ConfigurationResolver();
    LOG.info("start scm-manager version {}", SCMContext.getContext().getVersion());
    Throwable startupError = SCMContext.getContext().getStartupError();
    if (startupError != null) {
      LOG.error("received unrecoverable error during startup", startupError);
      return createStageOneInjector(SingleView.error(startupError));
    } else if (Versions.isTooOld()) {
      LOG.error("existing version is too old and cannot be migrated to new version. Please update to version {} first", Versions.MIN_VERSION);
      return createStageOneInjector(SingleView.view("/templates/too-old.mustache", HttpServletResponse.SC_CONFLICT));
    } else {
      try {
        return createStageTwoInjector(configurationResolver);
      } catch (Exception ex) {
        LOG.error("failed to create stage two injector", ex);
        return createStageOneInjector(SingleView.error(ex));
      }
    }
  }

  private void configureLoggers() {
    new LoggingConfiguration().configureLogging();
  }

  @Override
  public void contextDestroyed(ServletContextEvent sce) {
    LOG.info("shutdown scm-manager context");

    ServletContextCleaner.cleanup(context);

    injectionLifeCycle.shutdown();
    injectionLifeCycle = null;
    classLoaderLifeCycle.shutdown();

    super.contextDestroyed(sce);
  }

  private Injector createStageTwoInjector(ConfigurationResolver configurationResolver) {
    PluginBootstrap pluginBootstrap = new PluginBootstrap(context, classLoaderLifeCycle, configurationResolver);

    ModuleProvider provider = createMigrationOrNormalModuleProvider(pluginBootstrap);
    return createStageTwoInjector(provider, pluginBootstrap.getPluginLoader());
  }

  private ModuleProvider createMigrationOrNormalModuleProvider(PluginBootstrap pluginBootstrap) {
    Injector bootstrapInjector = createBootstrapInjector(pluginBootstrap.getPluginLoader());

    return startEitherMigrationOrApplication(pluginBootstrap.getPluginLoader(), bootstrapInjector);
  }

  private ModuleProvider startEitherMigrationOrApplication(PluginLoader pluginLoader, Injector bootstrapInjector) {
    MigrationWizardModuleProvider wizardModuleProvider = new MigrationWizardModuleProvider(bootstrapInjector);

    if (wizardModuleProvider.wizardNecessary()) {
      return wizardModuleProvider;
    } else {
      processUpdates(pluginLoader, bootstrapInjector);

      Versions.writeNew();

      return new ApplicationModuleProvider(context, pluginLoader);
    }
  }

  private Injector createStageOneInjector(ModuleProvider provider) {
    return Guice.createInjector(provider.createModules());
  }

  private Injector createStageTwoInjector(ModuleProvider provider, PluginLoader pluginLoader) {
    List<Module> modules = new ArrayList<>(createBootstrapModules(pluginLoader));
    modules.addAll(provider.createModules());
    return Guice.createInjector(modules);
  }

  private Injector createBootstrapInjector(PluginLoader pluginLoader) {
    return Guice.createInjector(createBootstrapModules(pluginLoader));
  }

  private List<Module> createBootstrapModules(PluginLoader pluginLoader) {
    List<Module> modules = new ArrayList<>(createBaseModules());
    modules.add(new ConfigModule(pluginLoader));
    modules.add(new BootstrapModule(pluginLoader));
    return modules;
  }

  private List<Module> createBaseModules() {
    return List.of(
      new EagerSingletonModule(),
      new ScmInitializerModule(),
      new ScmEventBusModule(),
      new ServletContextModule(),
      new CloseableModule()
    );
  }

  private void processUpdates(PluginLoader pluginLoader, Injector bootstrapInjector) {
    Injector updateInjector = bootstrapInjector.createChildInjector(new UpdateStepModule(pluginLoader));

    UpdateEngine updateEngine = updateInjector.getInstance(UpdateEngine.class);
    updateEngine.update();
  }

}
