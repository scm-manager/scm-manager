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

package sonia.scm.web.plugin;

//~--- non-JDK imports --------------------------------------------------------

import com.google.inject.Module;

//~--- JDK imports ------------------------------------------------------------

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.ServletContext;

/**
 *
 * @author Sebastian Sdorra
 */
public class ScmWebPluginContext
{

  /**
   * Constructs ...
   *
   *
   * @param servletContext
   */
  public ScmWebPluginContext(ServletContext servletContext)
  {
    this.servletContext = servletContext;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param module
   */
  public void addInjectModule(Module module)
  {
    injectModules.add(module);
  }

  /**
   * Method description
   *
   *
   * @param resource
   */
  public void addScriptResource(WebResource resource)
  {
    scriptResources.add(resource);
  }

  /**
   * Method description
   *
   *
   * @param module
   */
  public void removeInjectModule(Module module)
  {
    injectModules.remove(module);
  }

  /**
   * Method description
   *
   *
   * @param resource
   */
  public void removeScriptResource(WebResource resource)
  {
    scriptResources.remove(resource);
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  public Collection<Module> getInjectModules()
  {
    return injectModules;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public Collection<WebResource> getScriptResources()
  {
    return scriptResources;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public ServletContext getServletContext()
  {
    return servletContext;
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private Set<WebResource> scriptResources = new HashSet<WebResource>();

  /** Field description */
  private Set<Module> injectModules = new HashSet<Module>();

  /** Field description */
  private ServletContext servletContext;
}
