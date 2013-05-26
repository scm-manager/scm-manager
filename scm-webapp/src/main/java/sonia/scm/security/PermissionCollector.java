/**
 * Copyright (c) 2010, Sebastian Sdorra All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer. 2. Redistributions in
 * binary form must reproduce the above copyright notice, this list of
 * conditions and the following disclaimer in the documentation and/or other
 * materials provided with the distribution. 3. Neither the name of SCM-Manager;
 * nor the names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * http://bitbucket.org/sdorra/scm-manager
 *
 */



package sonia.scm.security;

//~--- non-JDK imports --------------------------------------------------------

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.apache.shiro.authz.Permission;
import org.apache.shiro.authz.permission.PermissionResolver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sonia.scm.group.GroupNames;
import sonia.scm.repository.PermissionType;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryDAO;
import sonia.scm.user.User;
import sonia.scm.util.Util;

//~--- JDK imports ------------------------------------------------------------

import java.util.List;

/**
 *
 * @author Sebastian Sdorra
 */
@Singleton
public class PermissionCollector
{

  /**
   * the logger for PermissionCollector
   */
  private static final Logger logger =
    LoggerFactory.getLogger(PermissionCollector.class);

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   *
   * @param repositoryDAO
   * @param securitySystem
   * @param resolver
   */
  @Inject
  public PermissionCollector(RepositoryDAO repositoryDAO,
    SecuritySystem securitySystem, PermissionResolver resolver)
  {
    this.repositoryDAO = repositoryDAO;
    this.securitySystem = securitySystem;
    this.resolver = resolver;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param user
   * @param groups
   *
   * @return
   */
  public List<Permission> collect(User user, GroupNames groups)
  {
    Builder<Permission> builder = ImmutableList.builder();

    if (user.isActive())
    {
      if (user.isAdmin())
      {
        //J-
        builder.add(
          new RepositoryPermission(
            RepositoryPermission.WILDCARD, 
            PermissionType.OWNER
          )
        );
        //J+
      }
      else
      {
        collectRepositoryPermissions(builder, user, groups);
        collectGlobalPermissions(builder, user, groups);
      }
    }

    return builder.build();
  }

  /**
   * Method description
   *
   *
   *
   * @param builder
   * @param user
   * @param groups
   *
   * @return
   */
  private void collectGlobalPermissions(Builder<Permission> builder,
    final User user, final GroupNames groups)
  {
    if (logger.isTraceEnabled())
    {
      logger.trace("collect global permissions for user {}", user.getName());
    }

    List<StoredAssignedPermission> globalPermissions =
      securitySystem.getPermissions(new Predicate<AssignedPermission>()
    {

      @Override
      public boolean apply(AssignedPermission input)
      {
        return isUserPermission(user, groups, input);
      }
    });

    for (StoredAssignedPermission gp : globalPermissions)
    {
      if (logger.isTraceEnabled())
      {
        logger.trace("add permission {} for user {}", gp.getPermission(),
          user.getName());
      }

      Permission permission = resolver.resolvePermission(gp.getPermission());

      if (permission != null)
      {
        builder.add(permission);
      }
    }
  }

  /**
   * Method description
   *
   *
   *
   * @param builder
   * @param user
   * @param groups
   *
   * @return
   */
  private void collectRepositoryPermissions(Builder<Permission> builder,
    User user, GroupNames groups)
  {
    for (Repository repository : repositoryDAO.getAll())
    {
      if (logger.isTraceEnabled())
      {
        logger.trace("collect permissions for repository {} and user {}",
          repository.getName(), user.getName());
      }

      collectRepositoryPermissions(builder, repository, user, groups);
    }
  }

  /**
   * Method description
   *
   *
   * @param permissions
   *
   * @param builder
   * @param repository
   * @param user
   * @param groups
   */
  private void collectRepositoryPermissions(Builder<Permission> builder,
    Repository repository, User user, GroupNames groups)
  {
    List<sonia.scm.repository.Permission> repositoryPermissions =
      repository.getPermissions();

    if (Util.isNotEmpty(repositoryPermissions))
    {

      for (sonia.scm.repository.Permission permission : repositoryPermissions)
      {
        if (isUserPermission(user, groups, permission))
        {
          RepositoryPermission rp = new RepositoryPermission(repository,
                                      permission.getType());

          if (logger.isTraceEnabled())
          {
            logger.trace("add repository permission {} for user {}", rp,
              user.getName());
          }

          builder.add(rp);
        }
      }
    }
    else if (logger.isTraceEnabled())
    {
      logger.trace("repository {} has not permission entries",
        repository.getName());
    }
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param user
   * @param groups
   * @param perm
   *
   * @return
   */
  private boolean isUserPermission(User user, GroupNames groups,
    PermissionObject perm)
  {
    //J-
    return (perm.isGroupPermission() && groups.contains(perm.getName())) 
      || ((!perm.isGroupPermission()) && user.getName().equals(perm.getName()));
    //J+
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private RepositoryDAO repositoryDAO;

  /** Field description */
  private PermissionResolver resolver;

  /** Field description */
  private SecuritySystem securitySystem;
}
