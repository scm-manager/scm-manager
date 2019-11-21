/**
 * Copyright (c) 2010, Sebastian Sdorra All rights reserved.
 * <p>
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * <p>
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer. 2. Redistributions in
 * binary form must reproduce the above copyright notice, this list of
 * conditions and the following disclaimer in the documentation and/or other
 * materials provided with the distribution. 3. Neither the name of SCM-Manager;
 * nor the names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 * <p>
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * <p>
 * http://bitbucket.org/sdorra/scm-manager
 */


package sonia.scm.lifecycle;

import com.google.common.collect.ImmutableList;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.servlet.GuiceServletContextListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.lifecycle.classloading.ClassLoaderLifeCycle;
import sonia.scm.lifecycle.modules.ApplicationModuleProvider;
import sonia.scm.lifecycle.modules.BootstrapModule;
import sonia.scm.lifecycle.modules.CloseableModule;
import sonia.scm.lifecycle.modules.EagerSingletonModule;
import sonia.scm.SCMContext;
import sonia.scm.lifecycle.modules.InjectionLifeCycle;
import sonia.scm.lifecycle.modules.ModuleProvider;
import sonia.scm.lifecycle.modules.ScmEventBusModule;
import sonia.scm.lifecycle.modules.ScmInitializerModule;
import sonia.scm.lifecycle.modules.ServletContextModule;
import sonia.scm.lifecycle.modules.UpdateStepModule;
import sonia.scm.lifecycle.view.SingleView;
import sonia.scm.plugin.PluginLoader;
import sonia.scm.update.MigrationWizardModuleProvider;
import sonia.scm.update.UpdateEngine;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Sebastian Sdorra
 */
public class BootstrapContextListener extends GuiceServletContextListener {

  private static final Logger LOG = LoggerFactory.getLogger(BootstrapContextListener.class);

  private ClassLoaderLifeCycle classLoaderLifeCycle = ClassLoaderLifeCycle.create();

  private ServletContext context;
  private InjectionLifeCycle injectionLifeCycle;

  @Override
  public void contextInitialized(ServletContextEvent sce) {
    LOG.info("start scm-manager initialization");

    context = sce.getServletContext();
    classLoaderLifeCycle.initialize();
    super.contextInitialized(sce);

    Injector injector = (Injector) context.getAttribute(Injector.class.getName());
    injectionLifeCycle = new InjectionLifeCycle(injector);
    injectionLifeCycle.initialize();
  }

  @Override
  protected Injector getInjector() {
    Throwable startupError = SCMContext.getContext().getStartupError();
    if (startupError != null) {
      return createStageOneInjector(SingleView.error(startupError));
    } else if (Versions.isTooOld()) {
      LOG.error("Existing version is too old and cannot be migrated to new version. Please update to version {} first", Versions.MIN_VERSION);
      return createStageOneInjector(SingleView.view("/templates/too-old.mustache", HttpServletResponse.SC_CONFLICT));
    } else {
      try {
        return createStageTwoInjector();
      } catch (Exception ex) {
        return createStageOneInjector(SingleView.error(ex));
      }
    }
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

  private Injector createStageTwoInjector() {
    PluginBootstrap pluginBootstrap = new PluginBootstrap(context, classLoaderLifeCycle);

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
    modules.add(new BootstrapModule(pluginLoader));
    return modules;
  }

  private List<Module> createBaseModules() {
    return ImmutableList.of(
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
