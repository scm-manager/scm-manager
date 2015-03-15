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

import sonia.scm.security.PermissionDescriptor;
import sonia.scm.user.User;

//~--- JDK imports ------------------------------------------------------------

import java.util.Collection;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * This class represents the current state of the SCM-Manager.
 *
 * @author Sebastian Sdorra
 */
@XmlRootElement(name = "state")
@XmlAccessorType(XmlAccessType.FIELD)
public final class ScmState
{

  /**
   * Constructs {@link ScmState} object.
   * This constructor is required by JAXB.
   *
   */
  ScmState() {}

  /**
   * Constructs {@link ScmState} object.
   *
   *
   * @param version scm-manager version
   * @param user current user
   * @param groups groups of the current user
   * @param token authentication token
   * @param repositoryTypes available repository types
   * @param defaultUserType default user type
   * @param clientConfig client configuration
   * @param assignedPermission assigned permissions
   * @param availablePermissions list of available permissions
   *
   * @since 2.0.0
   */
  public ScmState(String version, User user, Collection<String> groups,
    String token, Collection<Type> repositoryTypes, String defaultUserType,
    ScmClientConfig clientConfig, List<String> assignedPermission,
    List<PermissionDescriptor> availablePermissions)
  {
    this.version = version;
    this.user = user;
    this.groups = groups;
    this.token = token;
    this.repositoryTypes = repositoryTypes;
    this.clientConfig = clientConfig;
    this.defaultUserType = defaultUserType;
    this.assignedPermissions = assignedPermission;
    this.availablePermissions = availablePermissions;
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Return a list of assigned permissions.
   *
   *
   * @return list of assigned permissions
   * @since 1.31
   */
  public List<String> getAssignedPermissions()
  {
    return assignedPermissions;
  }

  /**
   * Returns a list of available global permissions.
   *
   *
   * @return available global permissions
   * @since 1.31
   */
  public List<PermissionDescriptor> getAvailablePermissions()
  {
    return availablePermissions;
  }

  /**
   * Returns configuration for SCM-Manager clients.
   *
   *
   * @return configuration for SCM-Manager clients
   */
  public ScmClientConfig getClientConfig()
  {
    return clientConfig;
  }

  /**
   * Returns the default user type
   *
   *
   * @return default user type
   *
   * @since 1.14
   */
  public String getDefaultUserType()
  {
    return defaultUserType;
  }

  /**
   * Returns a {@link java.util.Collection} of groups names which are associated
   * to the current user.
   *
   *
   * @return a {@link java.util.Collection} of groups names
   */
  public Collection<String> getGroups()
  {
    return groups;
  }

  /**
   * Returns all available repository types.
   *
   *
   * @return all available repository types
   */
  public Collection<Type> getRepositoryTypes()
  {
    return repositoryTypes;
  }

  /**
   * Returns authentication token or {@code null}.
   *
   *
   * @return authentication token or {@code null}
   * 
   * @since 2.0.0
   */
  public String getToken()
  {
    return token;
  }

  /**
   * Returns the current logged in user.
   *
   *
   * @return current logged in user
   */
  public User getUser()
  {
    return user;
  }

  /**
   * Returns the version of the SCM-Manager.
   *
   *
   * @return version of the SCM-Manager
   */
  public String getVersion()
  {
    return version;
  }

  /**
   * Returns true if the request was successful.
   * This method is required by extjs.
   *
   * @return true if the request was successful
   */
  public boolean isSuccess()
  {
    return success;
  }

  //~--- fields ---------------------------------------------------------------

  /** marker for extjs */
  private final boolean success = true;

  /** authentication token */
  private String token;

  /** Field description */
  private List<String> assignedPermissions;

  /**
   * Avaliable global permission
   * @since 1.31
   */
  private List<PermissionDescriptor> availablePermissions;

  /** Field description */
  private ScmClientConfig clientConfig;

  /** Field description */
  private String defaultUserType;

  /** Field description */
  private Collection<String> groups;

  /** Field description */
  @XmlElement(name = "repositoryTypes")
  private Collection<Type> repositoryTypes;

  /** Field description */
  private User user;

  /** Field description */
  private String version;
}
