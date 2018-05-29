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

import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import com.google.inject.Injector;
import com.google.inject.Module;
import org.apache.shiro.guice.web.ShiroWebModule;
import org.jboss.resteasy.plugins.guice.GuiceResteasyBootstrapServletContextListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.api.rest.resources.MapperModule;
import sonia.scm.cache.CacheManager;
import sonia.scm.debug.DebugModule;
import sonia.scm.filter.WebElementModule;
import sonia.scm.group.GroupManager;
import sonia.scm.plugin.DefaultPluginLoader;
import sonia.scm.plugin.ExtensionProcessor;
import sonia.scm.plugin.PluginWrapper;
import sonia.scm.repository.RepositoryManager;
import sonia.scm.schedule.Scheduler;
import sonia.scm.upgrade.UpgradeManager;
import sonia.scm.user.UserManager;
import sonia.scm.util.IOUtil;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import java.util.Collections;
import java.util.List;
import java.util.Set;

//~--- JDK imports ------------------------------------------------------------

/**
 *
 * @author Sebastian Sdorra
 */
public class ScmContextListener extends GuiceResteasyBootstrapServletContextListener
{

  /**
   * the logger for ScmContextListener
   */
  private static final Logger LOG = LoggerFactory.getLogger(ScmContextListener.class);
  
  private final ClassLoader parent;
  private final Set<PluginWrapper> plugins;
  private Injector injector;
  
  //~--- constructors ---------------------------------------------------------
  
  public ScmContextListener(ClassLoader parent, Set<PluginWrapper> plugins)
  {
    this.parent = parent;
    this.plugins = plugins;
  }

  public Set<PluginWrapper> getPlugins() {
    return plugins;
  }

  @Override
  public void contextInitialized(ServletContextEvent servletContextEvent) {
    beforeInjectorCreation();
    super.contextInitialized(servletContextEvent);
    afterInjectorCreation(servletContextEvent);
  }
  
  private void beforeInjectorCreation() {
    upgradeIfNecessary();
  }
  
  private void upgradeIfNecessary() {
    if (!hasStartupErrors()) {
      UpgradeManager upgradeHandler = new UpgradeManager();

      upgradeHandler.doUpgrade();
    }
  }
  
  private boolean hasStartupErrors() {
    return SCMContext.getContext().getStartupError() != null;
  }
  
  @Override
  protected List<? extends Module> getModules(ServletContext context) {
    if (hasStartupErrors()) {
      return getErrorModules();
    }
    return getDefaultModules(context);
  }
  
  private List<? extends Module> getDefaultModules(ServletContext context) {
    DefaultPluginLoader pluginLoader = new DefaultPluginLoader(context, parent, plugins);

    ClassOverrides overrides = ClassOverrides.findOverrides(pluginLoader.getUberClassLoader());
    List<Module> moduleList = Lists.newArrayList();

    moduleList.add(new ScmInitializerModule());
    moduleList.add(new ScmEventBusModule());
    moduleList.add(new EagerSingletonModule());
    moduleList.add(ShiroWebModule.guiceFilterModule());
    moduleList.add(new WebElementModule(pluginLoader));
    moduleList.add(new ScmServletModule(context, pluginLoader, overrides));
    moduleList.add(
      new ScmSecurityModule(context, pluginLoader.getExtensionProcessor())
    );
    appendModules(pluginLoader.getExtensionProcessor(), moduleList);
    moduleList.addAll(overrides.getModules());
    
    if (SCMContext.getContext().getStage() == Stage.DEVELOPMENT){
      moduleList.add(new DebugModule());
    }
    moduleList.add(new MapperModule());

    return moduleList;    
  }
  
  private void appendModules(ExtensionProcessor ep, List<Module> moduleList) {
    for (Class<? extends Module> module : ep.byExtensionPoint(Module.class)) {
      try {
        LOG.info("add module {}", module);
        moduleList.add(module.newInstance());
      } catch (IllegalAccessException | InstantiationException ex) {
        throw Throwables.propagate(ex);
      }
    }
  }
  
  private List<? extends Module> getErrorModules() {
    return Collections.singletonList(new ScmErrorModule());
  }

  @Override
  protected void withInjector(Injector injector) {
    this.injector = injector;
  }
  
  private void afterInjectorCreation(ServletContextEvent event) {
    if (injector != null && !hasStartupErrors()) {
      bindEagerSingletons();
      initializeServletContextListeners(event);
    } 
  }
  
  private void bindEagerSingletons() {
    injector.getInstance(EagerSingletonModule.class).initialize(injector);
  }
  
  private void initializeServletContextListeners(ServletContextEvent event) {
    injector.getInstance(ServletContextListenerHolder.class).contextInitialized(event);
  }
  
  @Override
  public void contextDestroyed(ServletContextEvent servletContextEvent)
  {
    if (injector != null &&!hasStartupErrors()) {
      closeCloseables();
      destroyServletContextListeners(servletContextEvent);
    }

    super.contextDestroyed(servletContextEvent);
  }
  
  private void closeCloseables() {
    // close Scheduler
    IOUtil.close(injector.getInstance(Scheduler.class));

    // close RepositoryManager
    IOUtil.close(injector.getInstance(RepositoryManager.class));

    // close GroupManager
    IOUtil.close(injector.getInstance(GroupManager.class));

    // close UserManager
    IOUtil.close(injector.getInstance(UserManager.class));

    // close CacheManager
    IOUtil.close(injector.getInstance(CacheManager.class));
  }

  private void destroyServletContextListeners(ServletContextEvent event) {
    injector.getInstance(ServletContextListenerHolder.class).contextDestroyed(event);
  }


}
