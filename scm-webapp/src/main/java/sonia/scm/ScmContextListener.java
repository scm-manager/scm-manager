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

import sonia.scm.plugin.DefaultPluginManager;
import sonia.scm.plugin.PluginManager;
import sonia.scm.repository.RepositoryManager;
import sonia.scm.store.StoreFactory;
import sonia.scm.user.UserManager;
import sonia.scm.util.IOUtil;
import sonia.scm.web.security.AuthenticationManager;

//~--- JDK imports ------------------------------------------------------------

import java.util.ArrayList;
import java.util.List;

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
    if (injector != null)
    {

      // close RepositoryManager
      IOUtil.close(injector.getInstance(RepositoryManager.class));

      // close Authenticator
      IOUtil.close(injector.getInstance(AuthenticationManager.class));

      // close UserManager
      IOUtil.close(injector.getInstance(UserManager.class));

      // close StoreFactory
      IOUtil.close(injector.getInstance(StoreFactory.class));
    }

    super.contextDestroyed(servletContextEvent);
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
    PluginManager manager = new DefaultPluginManager();
    BindingExtensionProcessor bindExtProcessor =
      new BindingExtensionProcessor();

    manager.processExtensions(bindExtProcessor);

    ScmServletModule main = new ScmServletModule(manager, bindExtProcessor);
    List<Module> moduleList =
      new ArrayList<Module>(bindExtProcessor.getModuleSet());

    moduleList.add(0, main);
    injector = Guice.createInjector(moduleList);

    SCMContextProvider context = SCMContext.getContext();

    // init StoreFactory
    injector.getInstance(StoreFactory.class).init(context);

    // init RepositoryManager
    injector.getInstance(RepositoryManager.class).init(context);

    // init UserManager
    injector.getInstance(UserManager.class).init(context);

    // init Authenticator
    injector.getInstance(AuthenticationManager.class).init(context);

    return injector;
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private Injector injector;
}
