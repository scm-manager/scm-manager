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



package sonia.scm.security;

//~--- non-JDK imports --------------------------------------------------------

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;

import org.apache.shiro.authz.Permission;

import sonia.scm.repository.PermissionType;
import sonia.scm.repository.Repository;

//~--- JDK imports ------------------------------------------------------------

import java.io.Serializable;

/**
 * This class represents the permission to a repository of a user.
 *
 * @author Sebastian Sdorra
 * @since 1.21
 */
public final class RepositoryPermission
  implements StringablePermission, Serializable
{

  /**
   * Type string of the permission
   * @since 1.31
   */
  public static final String TYPE = "repository";

  /** Field description */
  public static final String WILDCARD = "*";

  /** Field description */
  private static final long serialVersionUID = 3832804235417228043L;

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   *
   * @param repository
   * @param permissionType
   */
  public RepositoryPermission(Repository repository,
    PermissionType permissionType)
  {
    this(repository.getId(), permissionType);
  }

  /**
   * Constructs ...
   *
   *
   * @param repositoryId
   * @param permissionType
   */
  public RepositoryPermission(String repositoryId,
    PermissionType permissionType)
  {
    this.repositoryId = repositoryId;
    this.permissionType = permissionType;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param obj
   *
   * @return
   */
  @Override
  public boolean equals(Object obj)
  {
    if (obj == null)
    {
      return false;
    }

    if (getClass() != obj.getClass())
    {
      return false;
    }

    final RepositoryPermission other = (RepositoryPermission) obj;

    return Objects.equal(repositoryId, other.repositoryId)
      && Objects.equal(permissionType, other.permissionType);
  }

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  public int hashCode()
  {
    return Objects.hashCode(repositoryId, permissionType);
  }

  /**
   * Method description
   *
   *
   * @param p
   *
   * @return
   */
  @Override
  public boolean implies(Permission p)
  {
    boolean result = false;

    if (p instanceof RepositoryPermission)
    {
      RepositoryPermission rp = (RepositoryPermission) p;

      //J-
      result = (repositoryId.equals(WILDCARD) || repositoryId.equals(rp.repositoryId)) 
                && (permissionType.getValue() >= rp.permissionType.getValue());
      //J+
    }

    return result;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  public String toString()
  {
    //J-
    return MoreObjects.toStringHelper(this)
                      .add("repositoryId", repositoryId)
                      .add("permissionType", permissionType)
                      .toString();
    //J+
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  public String getAsString()
  {
    StringBuilder buffer = new StringBuilder(TYPE);

    buffer.append(":").append(repositoryId).append(":").append(permissionType);

    return buffer.toString();
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public PermissionType getPermissionType()
  {
    return permissionType;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public String getRepositoryId()
  {
    return repositoryId;
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private PermissionType permissionType;

  /** Field description */
  private String repositoryId;
}
