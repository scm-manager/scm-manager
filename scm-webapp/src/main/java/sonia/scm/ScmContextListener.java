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

import com.google.common.base.Stopwatch;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.servlet.GuiceServletContextListener;

import org.apache.shiro.guice.web.ShiroWebModule;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sonia.scm.cache.CacheManager;
import sonia.scm.group.GroupManager;
import sonia.scm.plugin.DefaultPluginLoader;
import sonia.scm.plugin.ExtensionProcessor;
import sonia.scm.plugin.PluginWrapper;
import sonia.scm.repository.RepositoryManager;
import sonia.scm.upgrade.UpgradeManager;
import sonia.scm.user.UserManager;
import sonia.scm.util.IOUtil;

//~--- JDK imports ------------------------------------------------------------

import java.util.List;
import java.util.Set;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import sonia.scm.filter.WebElementModule;

/**
 *
 * @author Sebastian Sdorra
 */
public class ScmContextListener extends GuiceServletContextListener
{

  /**
   * the logger for ScmContextListener
   */
  private static final Logger logger =
    LoggerFactory.getLogger(ScmContextListener.class);

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   *
   * @param parent
   * @param plugins
   */
  public ScmContextListener(ClassLoader parent, Set<PluginWrapper> plugins)
  {
    this.parent = parent;
    this.plugins = plugins;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param servletContextEvent
   */
  @Override
  public void contextDestroyed(ServletContextEvent servletContextEvent)
  {
    if ((globalInjector != null) &&!startupError)
    {

      // close RepositoryManager
      IOUtil.close(globalInjector.getInstance(RepositoryManager.class));

      // close GroupManager
      IOUtil.close(globalInjector.getInstance(GroupManager.class));

      // close UserManager
      IOUtil.close(globalInjector.getInstance(UserManager.class));

      // close CacheManager
      IOUtil.close(globalInjector.getInstance(CacheManager.class));

      //J-
      // call destroy of servlet context listeners
      globalInjector.getInstance(ServletContextListenerHolder.class)
                    .contextDestroyed(servletContextEvent);
      //J+
    }

    super.contextDestroyed(servletContextEvent);
  }

  /**
   * Method description
   *
   *
   * @param servletContextEvent
   */
  @Override
  public void contextInitialized(ServletContextEvent servletContextEvent)
  {
    this.servletContext = servletContextEvent.getServletContext();

    if (SCMContext.getContext().getStartupError() == null)
    {
      UpgradeManager upgradeHandler = new UpgradeManager();

      upgradeHandler.doUpgrade();
    }
    else
    {
      startupError = true;
    }

    super.contextInitialized(servletContextEvent);

    // call destroy event
    if ((globalInjector != null) &&!startupError)
    {
      //J-
      // bind eager singletons
      globalInjector.getInstance(EagerSingletonModule.class)
                    .initialize(globalInjector);
      // init servlet context listeners
      globalInjector.getInstance(ServletContextListenerHolder.class)
                    .contextInitialized(servletContextEvent);
      //J+
    }
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  public Set<PluginWrapper> getPlugins()
  {
    return plugins;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  protected Injector getInjector()
  {
    if (startupError)
    {
      globalInjector = getErrorInjector();
    }
    else
    {
      globalInjector = getDefaultInjector(servletContext);
    }

    return globalInjector;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param ep
   * @param moduleList
   */
  private void appendModules(ExtensionProcessor ep, List<Module> moduleList)
  {
    for (Class<? extends Module> module : ep.byExtensionPoint(Module.class))
    {
      try
      {
        logger.info("add module {}", module);
        moduleList.add(module.newInstance());
      }
      catch (IllegalAccessException | InstantiationException ex)
      {
        throw Throwables.propagate(ex);
      }
    }
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   *
   * @param servletCtx
   * @return
   */
  private Injector getDefaultInjector(ServletContext servletCtx)
  {
    Stopwatch sw = Stopwatch.createStarted();
    DefaultPluginLoader pluginLoader = new DefaultPluginLoader(servletCtx,
                                         parent, plugins);

    ClassOverrides overrides =
      ClassOverrides.findOverrides(pluginLoader.getUberClassLoader());
    List<Module> moduleList = Lists.newArrayList();

    moduleList.add(new ScmInitializerModule());
    moduleList.add(new ScmEventBusModule());
    moduleList.add(new EagerSingletonModule());
    moduleList.add(ShiroWebModule.guiceFilterModule());
    moduleList.add(new WebElementModule(pluginLoader));
    moduleList.add(new ScmServletModule(servletCtx, pluginLoader, overrides));
    moduleList.add(
      new ScmSecurityModule(servletCtx, pluginLoader.getExtensionProcessor())
    );
    appendModules(pluginLoader.getExtensionProcessor(), moduleList);
    moduleList.addAll(overrides.getModules());

    SCMContextProvider ctx = SCMContext.getContext();

    Injector injector =
      Guice.createInjector(ctx.getStage().getInjectionStage(), moduleList);

    logger.info("created injector in {}", sw.stop());

    return injector;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  private Injector getErrorInjector()
  {
    return Guice.createInjector(new ScmErrorModule());
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private final ClassLoader parent;

  /** Field description */
  private final Set<PluginWrapper> plugins;

  /** Field description */
  private Injector globalInjector;

  /** Field description */
  private ServletContext servletContext;

  /** Field description */
  private boolean startupError = false;
}
