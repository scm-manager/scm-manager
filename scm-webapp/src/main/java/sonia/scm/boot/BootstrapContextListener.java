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


package sonia.scm.boot;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import sonia.scm.EagerSingletonModule;
import sonia.scm.SCMContext;
import sonia.scm.ScmContextListener;
import sonia.scm.ScmEventBusModule;
import sonia.scm.ScmInitializerModule;
import sonia.scm.plugin.PluginLoader;
import sonia.scm.plugin.PluginWrapper;
import sonia.scm.update.MigrationWizardContextListener;
import sonia.scm.update.UpdateEngine;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.http.HttpServletResponse;
import java.util.Set;

/**
 *
 * @author Sebastian Sdorra
 */
public class BootstrapContextListener implements ServletContextListener {

  private final ClassLoaderLifeCycle classLoaderLifeCycle = ClassLoaderLifeCycle.create();


  private ServletContext context;
  private ServletContextListener contextListener;

  @Override
  public void contextInitialized(ServletContextEvent sce) {
    classLoaderLifeCycle.init();

    context = sce.getServletContext();

    createContextListener();

    contextListener.contextInitialized(sce);
  }

  @Override
  public void contextDestroyed(ServletContextEvent sce) {
    contextListener.contextDestroyed(sce);
    classLoaderLifeCycle.shutdown();

    context = null;
    contextListener = null;
  }

  private void createContextListener() {
    Throwable startupError = SCMContext.getContext().getStartupError();
    if (startupError != null) {
      contextListener = SingleView.error(startupError);
    } else if (Versions.isTooOld()) {
      contextListener = SingleView.view("/templates/too-old.mustache", HttpServletResponse.SC_CONFLICT);
    } else {
      createMigrationOrNormalContextListener();
      Versions.writeNew();
    }
  }

  private void createMigrationOrNormalContextListener() {
    PluginBootstrap pluginBootstrap = new PluginBootstrap(context, classLoaderLifeCycle);

    Injector bootstrapInjector = createBootstrapInjector(pluginBootstrap.getPluginLoader());

    startEitherMigrationOrNormalServlet(classLoaderLifeCycle.getBootstrapClassLoader(), pluginBootstrap.getPlugins(), pluginBootstrap.getPluginLoader(), bootstrapInjector);
  }

  private void startEitherMigrationOrNormalServlet(ClassLoader cl, Set<PluginWrapper> plugins, PluginLoader pluginLoader, Injector bootstrapInjector) {
    MigrationWizardContextListener wizardContextListener = prepareWizardIfNeeded(bootstrapInjector);

    if (wizardContextListener.wizardNecessary()) {
      contextListener = wizardContextListener;
    } else {
      processUpdates(pluginLoader, bootstrapInjector);
      contextListener = bootstrapInjector.getInstance(ScmContextListener.Factory.class).create(cl, plugins);
    }
  }


  private MigrationWizardContextListener prepareWizardIfNeeded(Injector bootstrapInjector) {
    return new MigrationWizardContextListener(bootstrapInjector);
  }

  private Injector createBootstrapInjector(PluginLoader pluginLoader) {
    Module scmContextListenerModule = new ScmContextListenerModule();
    BootstrapModule bootstrapModule = new BootstrapModule(pluginLoader);
    ScmInitializerModule scmInitializerModule = new ScmInitializerModule();
    EagerSingletonModule eagerSingletonModule = new EagerSingletonModule();
    ScmEventBusModule scmEventBusModule = new ScmEventBusModule();

    return Guice.createInjector(
      bootstrapModule,
      scmContextListenerModule,
      scmEventBusModule,
      scmInitializerModule,
      eagerSingletonModule
    );
  }

  private void processUpdates(PluginLoader pluginLoader, Injector bootstrapInjector) {
    Injector updateInjector = bootstrapInjector.createChildInjector(new UpdateStepModule(pluginLoader));

    UpdateEngine updateEngine = updateInjector.getInstance(UpdateEngine.class);
    updateEngine.update();
  }

  private static class ScmContextListenerModule extends AbstractModule {
    @Override
    protected void configure() {
      install(new FactoryModuleBuilder().build(ScmContextListener.Factory.class));
    }
  }
}
