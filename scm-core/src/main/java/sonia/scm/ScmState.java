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

import sonia.scm.user.User;
import sonia.scm.web.security.WebSecurityContext;

//~--- JDK imports ------------------------------------------------------------

import java.util.Collection;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author Sebastian Sdorra
 */
@XmlRootElement(name = "state")
@XmlAccessorType(XmlAccessType.FIELD)
public class ScmState
{

  /**
   * Constructs {@link ScmState} object. This constructor is required by JAXB.
   *
   */
  public ScmState() {}

  /**
   * Constructs {@link ScmState} object.
   *
   *
   * @param provider
   * @param securityContext
   * @param repositoryTypes
   * @param clientConfig
   */
  public ScmState(SCMContextProvider provider,
                  WebSecurityContext securityContext,
                  Collection<Type> repositoryTypes,
                  ScmClientConfig clientConfig)
  {
    this.version = provider.getVersion();
    this.user = securityContext.getUser();
    this.groups = securityContext.getGroups();
    this.repositoryTypes = repositoryTypes;
    this.clientConfig = clientConfig;
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  public ScmClientConfig getClientConfig()
  {
    return clientConfig;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public Collection<String> getGroups()
  {
    return groups;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public Collection<Type> getRepositoryTypes()
  {
    return repositoryTypes;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public User getUser()
  {
    return user;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public String getVersion()
  {
    return version;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public boolean isSuccess()
  {
    return success;
  }

  //~--- set methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param clientConfig
   */
  public void setClientConfig(ScmClientConfig clientConfig)
  {
    this.clientConfig = clientConfig;
  }

  /**
   * Method description
   *
   *
   * @param groups
   */
  public void setGroups(Collection<String> groups)
  {
    this.groups = groups;
  }

  /**
   * Method description
   *
   *
   * @param repositoryTypes
   */
  public void setRepositoryTypes(Collection<Type> repositoryTypes)
  {
    this.repositoryTypes = repositoryTypes;
  }

  /**
   * Method description
   *
   *
   * @param success
   */
  public void setSuccess(boolean success)
  {
    this.success = success;
  }

  /**
   * Method description
   *
   *
   * @param user
   */
  public void setUser(User user)
  {
    this.user = user;
  }

  /**
   * Method description
   *
   *
   * @param version
   */
  public void setVersion(String version)
  {
    this.version = version;
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private ScmClientConfig clientConfig;

  /** Field description */
  private Collection<String> groups;

  /** Field description */
  @XmlElement(name = "repositoryTypes")
  private Collection<Type> repositoryTypes;

  /** Field description */
  private boolean success = true;

  /** Field description */
  private User user;

  /** Field description */
  private String version;
}
