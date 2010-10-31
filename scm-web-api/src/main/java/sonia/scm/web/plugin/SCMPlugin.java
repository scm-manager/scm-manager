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

import sonia.scm.repository.RepositoryHandler;

//~--- JDK imports ------------------------------------------------------------

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author Sebastian Sdorra
 */
@XmlRootElement(name = "plugin-config")
@XmlAccessorType(XmlAccessType.FIELD)
public class SCMPlugin
{

  /**
   * Constructs ...
   *
   */
  public SCMPlugin()
  {
    handlers = new HashSet<Class<? extends RepositoryHandler>>();
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param handlerClass
   *
   * @return
   */
  public boolean addHandler(Class<? extends RepositoryHandler> handlerClass)
  {
    return handlers.add(handlerClass);
  }

  /**
   * Method description
   *
   *
   * @param handlerClass
   *
   * @return
   */
  public boolean removeHandler(Class<? extends RepositoryHandler> handlerClass)
  {
    return handlers.remove(handlerClass);
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  public Collection<Class<? extends RepositoryHandler>> getHandlers()
  {
    return handlers;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public SecurityConfig getSecurityConfig()
  {
    return securityConfig;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public Class<? extends ScmWebPlugin> getWebPlugin()
  {
    return webPlugin;
  }

  //~--- set methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param securityConfig
   */
  public void setSecurityConfig(SecurityConfig securityConfig)
  {
    this.securityConfig = securityConfig;
  }

  /**
   * Method description
   *
   *
   * @param webPlugin
   */
  public void setWebPlugin(Class<? extends ScmWebPlugin> webPlugin)
  {
    this.webPlugin = webPlugin;
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  @XmlElementWrapper(name = "repository-handlers")
  @XmlElement(name = "repository-handler")
  private Set<Class<? extends RepositoryHandler>> handlers;

  /** Field description */
  @XmlElement(name = "security")
  private SecurityConfig securityConfig;

  /** Field description */
  @XmlElement(name = "web-plugin")
  private Class<? extends ScmWebPlugin> webPlugin;
}
