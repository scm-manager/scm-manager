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

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.servlet.GuiceServletContextListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sonia.scm.repository.RepositoryManager;
import sonia.scm.util.Util;
import sonia.scm.web.plugin.SCMPlugin;
import sonia.scm.web.plugin.SCMPluginManager;
import sonia.scm.web.plugin.ScmWebPlugin;
import sonia.scm.web.plugin.ScmWebPluginContext;
import sonia.scm.web.security.Authenticator;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.ServletContextEvent;

/**
 *
 * @author Sebastian Sdorra
 */
public class ContextListener extends GuiceServletContextListener
{

  /** the logger for ContextListener */
  private static final Logger logger =
    LoggerFactory.getLogger(ContextListener.class);

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
    for (ScmWebPlugin plugin : webPluginSet)
    {
      plugin.contextDestroyed(webPluginContext);
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
    pluginManager = new SCMPluginManager();

    try
    {
      pluginManager.load();
      webPluginContext =
        new ScmWebPluginContext(servletContextEvent.getServletContext());

      for (SCMPlugin plugin : pluginManager.getPlugins())
      {
        try
        {
          webPluginSet.add(plugin.getWebPlugin().newInstance());
        }
        catch (InstantiationException ex)
        {
          logger.error(ex.getMessage(), ex);
        }
        catch (IllegalAccessException ex)
        {
          logger.error(ex.getMessage(), ex);
        }
      }

      for (ScmWebPlugin plugin : webPluginSet)
      {
        plugin.contextInitialized(webPluginContext);
      }
    }
    catch (IOException ex)
    {
      throw new RuntimeException(ex);
    }

    super.contextInitialized(servletContextEvent);
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
    List<Module> modules = new ArrayList<Module>();

    modules.add(new ScmServletModule(pluginManager, webPluginContext));

    Collection<Module> pluginModules = webPluginContext.getInjectModules();

    if (Util.isNotEmpty(pluginModules))
    {
      modules.addAll(pluginModules);
    }

    Injector injector = Guice.createInjector(modules);

    // init RepositoryManager
    injector.getInstance(RepositoryManager.class).init(SCMContext.getContext());

    // init Authenticator
    injector.getInstance(Authenticator.class).init(SCMContext.getContext());

    return injector;
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private SCMPluginManager pluginManager;

  /** Field description */
  private ScmWebPluginContext webPluginContext;

  /** Field description */
  private Set<ScmWebPlugin> webPluginSet = new LinkedHashSet<ScmWebPlugin>();
}
