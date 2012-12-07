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

import com.google.common.collect.Lists;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.servlet.GuiceServletContextListener;

import org.apache.shiro.guice.web.ShiroWebModule;

import sonia.scm.cache.CacheManager;
import sonia.scm.group.GroupManager;
import sonia.scm.plugin.DefaultPluginLoader;
import sonia.scm.plugin.PluginLoader;
import sonia.scm.repository.RepositoryManager;
import sonia.scm.store.StoreFactory;
import sonia.scm.upgrade.UpgradeManager;
import sonia.scm.user.UserManager;
import sonia.scm.util.IOUtil;
import sonia.scm.web.security.AuthenticationManager;

//~--- JDK imports ------------------------------------------------------------

import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;

/**
 *
 * @author Sebastian Sdorra
 */
public class ScmContextListener extends GuiceServletContextListener
{

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

      // close Authenticator
      IOUtil.close(globalInjector.getInstance(AuthenticationManager.class));

      // close GroupManager
      IOUtil.close(globalInjector.getInstance(GroupManager.class));

      // close UserManager
      IOUtil.close(globalInjector.getInstance(UserManager.class));

      // close StoreFactory
      IOUtil.close(globalInjector.getInstance(StoreFactory.class));

      // close CacheManager
      IOUtil.close(globalInjector.getInstance(CacheManager.class));

      // call destroy event
      globalInjector.getInstance(
        ServletContextListenerHolder.class).contextDestroyed(
        servletContextEvent);
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
      globalInjector.getInstance(
        ServletContextListenerHolder.class).contextInitialized(
        servletContextEvent);
    }
  }

  //~--- get methods ----------------------------------------------------------

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

  /**
   * Method description
   *
   *
   *
   * @param servletContext
   * @return
   */
  private Injector getDefaultInjector(ServletContext servletContext)
  {
    PluginLoader pluginLoader = new DefaultPluginLoader();
    BindingExtensionProcessor bindExtProcessor =
      new BindingExtensionProcessor();

    pluginLoader.processExtensions(bindExtProcessor);

    ClassOverrides overrides = ClassOverrides.findOverrides();
    ScmServletModule main = new ScmServletModule(pluginLoader,
                              bindExtProcessor, overrides);
    List<Module> moduleList = Lists.newArrayList();

    moduleList.add(new ScmInitializerModule());
    moduleList.add(new ScmSubscriberModule());
    moduleList.add(ShiroWebModule.guiceFilterModule());
    moduleList.add(main);
    moduleList.add(new ScmSecurityModule(servletContext));
    moduleList.addAll(bindExtProcessor.getModuleSet());
    moduleList.addAll(overrides.getModules());

    return Guice.createInjector(moduleList);
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
  private Injector globalInjector;

  /** Field description */
  private ServletContext servletContext;

  /** Field description */
  private boolean startupError = false;
}
