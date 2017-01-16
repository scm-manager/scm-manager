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

import com.github.legman.Subscribe;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSet.Builder;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.Subject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sonia.scm.cache.Cache;
import sonia.scm.cache.CacheManager;
import sonia.scm.group.GroupEvent;
import sonia.scm.group.GroupNames;
import sonia.scm.plugin.Extension;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryDAO;
import sonia.scm.repository.RepositoryEvent;
import sonia.scm.user.User;
import sonia.scm.user.UserEvent;
import sonia.scm.util.Util;

//~--- JDK imports ------------------------------------------------------------

import java.util.List;
import java.util.Set;
import sonia.scm.group.Group;
import sonia.scm.group.GroupModificationEvent;
import sonia.scm.repository.RepositoryModificationEvent;
import sonia.scm.user.UserModificationEvent;

/**
 *
 * @author Sebastian Sdorra
 */
@Singleton
@Extension
public class DefaultAuthorizationCollector implements AuthorizationCollector
{

  // TODO move to util class
  private static final String SEPARATOR = System.getProperty("line.separator", "\n");
  
  /** Field description */
  private static final String ADMIN_PERMISSION = "*";

  /** Field description */
  private static final String CACHE_NAME = "sonia.cache.authorizing";

  /**
   * the logger for DefaultAuthorizationCollector
   */
  private static final Logger logger =
    LoggerFactory.getLogger(DefaultAuthorizationCollector.class);

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   *
   *
   * @param cacheManager
   * @param repositoryDAO
   * @param securitySystem
   */
  @Inject
  public DefaultAuthorizationCollector(CacheManager cacheManager,
    RepositoryDAO repositoryDAO, SecuritySystem securitySystem)
  {
    this.cache = cacheManager.getCache(CACHE_NAME);
    this.repositoryDAO = repositoryDAO;
    this.securitySystem = securitySystem;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  public AuthorizationInfo collect()
  {
    AuthorizationInfo authorizationInfo;
    Subject subject = SecurityUtils.getSubject();

    if (subject.hasRole(Role.USER))
    {
      authorizationInfo = collect(subject.getPrincipals());
    }
    else
    {
      authorizationInfo = new SimpleAuthorizationInfo();
    }

    return authorizationInfo;
  }

  /**
   * Invalidates the cache of a user which was modified. The cache entries for the user will be invalidated for the
   * following reasons:
   * <ul>
   * <li>Admin or Active flag was modified.</li>
   * <li>New user created, for the case of old cache values</li>
   * <li>User deleted</li>
   * </ul>
   *
   * @param event user event
   */
  @Subscribe
  public void onEvent(UserEvent event)
  {
    if (event.getEventType().isPost())
    {
      User user = event.getItem();
      String username = user.getId();
      if (event instanceof UserModificationEvent)
      {
        User beforeModification = ((UserModificationEvent) event).getItemBeforeModification();
        if (shouldCacheBeCleared(user, beforeModification))
        {
          logger.debug("invalidate cache of user {}, because of a permission relevant field has changed", username);
          invalidateUserCache(username);
        }
        else
        {
          logger.debug("cache of user {} is not invalidated, because no permission relevant field has changed", username);
        }
      }
      else
      {
        logger.debug("invalidate cache of user {}, because of user {} event", username, event.getEventType());
        invalidateUserCache(username);
      }
    }
  }

  private boolean shouldCacheBeCleared(User user, User beforeModification)
  {
    return user.isAdmin() != beforeModification.isAdmin() || user.isActive() != beforeModification.isActive();
  }

  private void invalidateUserCache(final String username)
  {
    cache.removeAll((CacheKey item) -> username.equalsIgnoreCase(item.username));
  }

  /**
   * Invalidates the whole cache, if a repository has changed. The cache get cleared for one of the following reasons:
   * <ul>
   * <li>New repository created</li>
   * <li>Repository was removed</li>
   * <li>Archived, Public readable or permission field of the repository was modified</li>
   * </ul>
   *
   * @param event repository event
   */
  @Subscribe
  public void onEvent(RepositoryEvent event)
  {
    if (event.getEventType().isPost())
    {
      Repository repository = event.getItem();
      
      if (event instanceof RepositoryModificationEvent)
      {
        Repository beforeModification = ((RepositoryModificationEvent) event).getItemBeforeModification();
        if (shouldCacheBeCleared(repository, beforeModification))
        {
          logger.debug("clear cache, because a relevant field of repository {} has changed", repository.getName());
          cache.clear();
        }
        else
        {
          logger.debug(
            "cache is not invalidated, because non relevant field of repository {} has changed",
            repository.getName()
          );
        }
      }
      else
      {
        logger.debug("clear cache, received {} event of repository {}", event.getEventType(), repository.getName());
        cache.clear();
      }
    }
  }

  private boolean shouldCacheBeCleared(Repository repository, Repository beforeModification)
  {
    return repository.isArchived() != beforeModification.isArchived()
      || repository.isPublicReadable() != beforeModification.isPublicReadable()
      || ! repository.getPermissions().equals(beforeModification.getPermissions());
  }

  /**
   * Invalidates the whole cache if a group permission has changed and invalidates the cached entries of a user, if a
   * user permission has changed.
   *
   *
   * @param event permission event
   */
  @Subscribe
  public void onEvent(StoredAssignedPermissionEvent event)
  {
    if (event.getEventType().isPost())
    {
      StoredAssignedPermission permission = event.getPermission();
      if (permission.isGroupPermission())
      {
        logger.debug("clear cache, because global group permission {} has changed", permission.getId());
        cache.clear();
      }
      else
      {
        logger.debug(
            "clear cache of user {}, because permission {} has changed", 
            permission.getName(), event.getPermission().getId()
        );
        invalidateUserCache(permission.getName());
      }
    }
  }

  /**
   * Invalidates the whole cache, if a group has changed. The cache get cleared for one of the following reasons:
   * <ul>
   * <li>New group created</li>
   * <li>Group was removed</li>
   * <li>Group members was modified</li>
   * </ul>
   *
   * @param event group event
   */
  @Subscribe
  public void onEvent(GroupEvent event)
  {
    if (event.getEventType().isPost())
    {
      Group group = event.getItem();
      if (event instanceof GroupModificationEvent)
      {
        Group beforeModification = ((GroupModificationEvent) event).getItemBeforeModification();
        if (shouldCacheBeCleared(group, beforeModification))
        {
          logger.debug("clear cache, because group {} has changed", group.getId());
          cache.clear();
        }
        else
        {
          logger.debug(
            "cache is not invalidated, because non relevant field of group {} has changed",
            group.getId()
          );
        }
      }
      else
      {
        logger.debug("clear cache, received group event {} for group {}", event.getEventType(), group.getId());
        cache.clear();
      }
    }
  }

  private boolean shouldCacheBeCleared(Group group, Group beforeModification)
  {
    return !group.getMembers().equals(beforeModification.getMembers());
  }

  /**
   * Method description
   *
   *
   *
   * @param principals
   *
   * @return
   */
  AuthorizationInfo collect(PrincipalCollection principals)
  {
    Preconditions.checkNotNull(principals, "principals parameter is required");

    User user = principals.oneByType(User.class);

    Preconditions.checkNotNull(user, "no user found in principal collection");

    GroupNames groupNames = principals.oneByType(GroupNames.class);

    CacheKey cacheKey = new CacheKey(user.getId(), groupNames);

    AuthorizationInfo info = cache.get(cacheKey);

    if (info == null)
    {
      logger.trace("collect AuthorizationInfo for user {}", user.getName());
      info = createAuthorizationInfo(user, groupNames);
      cache.put(cacheKey, info);
    }
    else if (logger.isTraceEnabled())
    {
      logger.trace("retrieve AuthorizationInfo for user {} from cache", user.getName());
    }

    return info;
  }

  private void collectGlobalPermissions(Builder<String> builder,
    final User user, final GroupNames groups)
  {
    List<StoredAssignedPermission> globalPermissions =
      securitySystem.getPermissions((AssignedPermission input) -> isUserPermitted(user, groups, input));

    for (StoredAssignedPermission gp : globalPermissions)
    {
      String permission = gp.getPermission();

      logger.trace("add permission {} for user {}", permission, user.getName());
      builder.add(permission);
    }
  }

  private void collectRepositoryPermissions(Builder<String> builder, User user,
    GroupNames groups)
  {
    for (Repository repository : repositoryDAO.getAll())
    {
      collectRepositoryPermissions(builder, repository, user, groups);
    }
  }

  private void collectRepositoryPermissions(Builder<String> builder,
    Repository repository, User user, GroupNames groups)
  {
    List<sonia.scm.repository.Permission> repositoryPermissions
      = repository.getPermissions();

    if (Util.isNotEmpty(repositoryPermissions))
    {
      boolean hasPermission = false;
      for (sonia.scm.repository.Permission permission : repositoryPermissions)
      {
        if (isUserPermitted(user, groups, permission))
        {

          String perm = permission.getType().getPermissionPrefix().concat(repository.getId());
          if (logger.isTraceEnabled())
          {
            logger.trace("add repository permission {} for user {} at repository {}", 
              perm, user.getName(), repository.getName());
          }

          builder.add(perm);
        }
      }

      if (!hasPermission && logger.isTraceEnabled())
      {
        logger.trace("no permission for user {} defined at repository {}", user.getName(), repository.getName());
      }
    }
    else if (logger.isTraceEnabled())
    {
      logger.trace("repository {} has no permission entries",
        repository.getName());
    }
  }

  private AuthorizationInfo createAuthorizationInfo(User user,
    GroupNames groups)
  {
    Set<String> roles;
    Set<String> permissions;

    if (user.isAdmin())
    {
      if (logger.isDebugEnabled())
      {
        logger.debug("grant admin role for user {}", user.getName());
      }

      roles = ImmutableSet.of(Role.USER, Role.ADMIN);

      permissions = ImmutableSet.of(ADMIN_PERMISSION);
    }
    else
    {
      roles = ImmutableSet.of(Role.USER);

      Builder<String> builder = ImmutableSet.builder();

      collectGlobalPermissions(builder, user, groups);
      collectRepositoryPermissions(builder, user, groups);
      permissions = builder.build();
    }

    SimpleAuthorizationInfo info = new SimpleAuthorizationInfo(roles);
    info.addStringPermissions(permissions);
    return info;
  }

  //~--- get methods ----------------------------------------------------------

  private boolean isUserPermitted(User user, GroupNames groups,
    PermissionObject perm)
  {
    //J-
    return (perm.isGroupPermission() && groups.contains(perm.getName()))
      || ((!perm.isGroupPermission()) && user.getName().equals(perm.getName()));
    //J+
  }

  //~--- inner classes --------------------------------------------------------

  /**
   * Cache key.
   */
  private static class CacheKey
  {
    private CacheKey(String username, GroupNames groupnames)
    {
      this.username = username;
      this.groupnames = groupnames;
    }

    //~--- methods ------------------------------------------------------------

    /**
     * {@inheritDoc}
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

      final CacheKey other = (CacheKey) obj;

      return Objects.equal(username, other.username)
        && Objects.equal(groupnames, other.groupnames);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode()
    {
      return Objects.hashCode(username, groupnames);
    }

    //~--- fields -------------------------------------------------------------

    /** group names */
    private final GroupNames groupnames;

    /** username */
    private final String username;
  }

  //~--- fields ---------------------------------------------------------------

  /** authorization cache */
  private final Cache<CacheKey, AuthorizationInfo> cache;

  /** repository dao */
  private final RepositoryDAO repositoryDAO;

  /** security system */
  private final SecuritySystem securitySystem;
}
