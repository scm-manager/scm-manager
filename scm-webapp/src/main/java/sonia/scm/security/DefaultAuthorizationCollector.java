/*
 * MIT License
 *
 * Copyright (c) 2020-present Cloudogu GmbH and Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package sonia.scm.security;

import com.github.legman.Subscribe;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSet.Builder;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.Permission;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.Subject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.cache.Cache;
import sonia.scm.cache.CacheManager;
import sonia.scm.group.GroupCollector;
import sonia.scm.group.GroupPermissions;
import sonia.scm.plugin.Extension;
import sonia.scm.repository.Namespace;
import sonia.scm.repository.NamespaceDao;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryDAO;
import sonia.scm.repository.RepositoryPermission;
import sonia.scm.user.User;
import sonia.scm.user.UserPermissions;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;

import static java.util.Collections.emptySet;
import static java.util.Collections.unmodifiableCollection;

@Singleton
@Extension
public class DefaultAuthorizationCollector implements AuthorizationCollector {

  private static final String CACHE_NAME = "sonia.cache.authorizing";

  private static final Logger logger =
    LoggerFactory.getLogger(DefaultAuthorizationCollector.class);


  /** authorization cache */
  private final Cache<CacheKey, AuthorizationInfo> cache;

  private final RepositoryDAO repositoryDAO;
  private final NamespaceDao namespaceDao;
  private final SecuritySystem securitySystem;
  private final RepositoryPermissionProvider repositoryPermissionProvider;
  private final GroupCollector groupCollector;

  @Inject
  public DefaultAuthorizationCollector(CacheManager cacheManager,
                                       RepositoryDAO repositoryDAO, SecuritySystem securitySystem, RepositoryPermissionProvider repositoryPermissionProvider, GroupCollector groupCollector, NamespaceDao namespaceDao)
  {
    this.cache = cacheManager.getCache(CACHE_NAME);
    this.repositoryDAO = repositoryDAO;
    this.securitySystem = securitySystem;
    this.repositoryPermissionProvider = repositoryPermissionProvider;
    this.groupCollector = groupCollector;
    this.namespaceDao = namespaceDao;
  }

  @VisibleForTesting
  AuthorizationInfo collect()
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

  @Override
  public AuthorizationInfo collect(PrincipalCollection principals)
  {
    Preconditions.checkNotNull(principals, "principals parameter is required");

    User user = principals.oneByType(User.class);

    Preconditions.checkNotNull(user, "no user found in principal collection");

    Set<String> groups = groupCollector.collect(user.getName());

    CacheKey cacheKey = new CacheKey(user.getId(), groups);

    AuthorizationInfo info = cache.get(cacheKey);

    if (info == null)
    {
      logger.trace("collect AuthorizationInfo for user {}", user.getName());
      info = createAuthorizationInfo(user, groups);
      cache.put(cacheKey, info);
    }
    else if (logger.isTraceEnabled())
    {
      logger.trace("retrieve AuthorizationInfo for user {} from cache", user.getName());
    }

    return new UnmodifiableAuthorizationInfo(info);
  }

  private void collectGlobalPermissions(Builder<String> builder,
    final User user, final Set<String> groups)
  {
    Collection<AssignedPermission> globalPermissions =
      securitySystem.getPermissions((AssignedPermission input) -> isUserPermitted(user, groups, input));

    for (AssignedPermission gp : globalPermissions)
    {
      String permission = gp.getPermission().getValue();

      logger.trace("add permission {} for user {}", permission, user.getName());
      builder.add(permission);
    }
  }

  private void collectNamespacePermissions(Builder<String> builder, User user, Set<String> groups) {
    for (Namespace namespace : namespaceDao.allWithPermissions()) {
      collectNamespacePermissions(builder, namespace, user, groups);
    }
  }

  private void collectNamespacePermissions(Builder<String> builder, Namespace namespace, User user, Set<String> groups) {
    for (RepositoryPermission permission : namespace.getPermissions()) {
      if (isUserPermitted(user, groups, permission)) {
        addNamespacePermission(builder, namespace, user, permission);
      }
    }
  }

  private void addNamespacePermission(Builder<String> builder, Namespace namespace, User user, RepositoryPermission permission) {
    Collection<String> verbs = getVerbs(permission);
    if (!verbs.isEmpty())
    {
      String perm = "namespace:" + String.join(",", verbs) + ":" + namespace.getId();
      if (logger.isTraceEnabled())
      {
        logger.trace("add namespace permission {} for user {} at namespace {}",
          perm, user.getName(), namespace.getNamespace());
      }

      builder.add(perm);
    }
  }

  private void collectRepositoryPermissions(Builder<String> builder, User user,
    Set<String> groups)
  {
    for (Repository repository : repositoryDAO.getAll())
    {
      collectRepositoryPermissions(builder, repository, user, groups);
    }
  }

  private void collectRepositoryPermissions(Builder<String> builder,
    Repository repository, User user, Set<String> groups)
  {
    Optional<Namespace> namespace = namespaceDao.get(repository.getNamespace());

    boolean hasPermission = false;
    for (RepositoryPermission permission : repository.getPermissions())
    {
      hasPermission = isUserPermitted(user, groups, permission);
      if (hasPermission) {
        addRepositoryPermission(builder, repository, user, permission);
      }
    }
    for (RepositoryPermission permission : namespace.map(Namespace::getPermissions).orElse(emptySet()))
    {
      hasPermission = isUserPermitted(user, groups, permission);
      if (hasPermission) {
        addRepositoryPermission(builder, repository, user, permission);
      }
    }

    if (!hasPermission && logger.isTraceEnabled())
    {
      logger.trace("no permission for user {} defined at repository {}", user.getName(), repository);
    }
  }

  private void addRepositoryPermission(Builder<String> builder, Repository repository, User user, RepositoryPermission permission) {
    Collection<String> verbs = getVerbs(permission);
    if (!verbs.isEmpty())
    {
      String perm = "repository:" + String.join(",", verbs) + ":" + repository.getId();
      if (logger.isTraceEnabled())
      {
        logger.trace("add repository permission {} for user {} at repository {}",
          perm, user.getName(), repository.getName());
      }

      builder.add(perm);
    }
  }

  private Collection<String> getVerbs(RepositoryPermission permission) {
    return permission.getRole() == null? permission.getVerbs(): getVerbsForRole(permission.getRole());
  }

  private Collection<String> getVerbsForRole(String roleName) {
    return repositoryPermissionProvider.availableRoles()
      .stream()
      .filter(role -> roleName.equals(role.getName()))
      .findFirst()
      .orElseThrow(() -> new IllegalStateException("unknown role: " + roleName))
      .getVerbs();
  }

  private AuthorizationInfo createAuthorizationInfo(User user, Set<String> groups) {
    Builder<String> builder = ImmutableSet.builder();

    collectGlobalPermissions(builder, user, groups);
    collectRepositoryPermissions(builder, user, groups);
    collectNamespacePermissions(builder, user, groups);
    builder.add(canReadOwnUser(user));
    if (!Authentications.isSubjectAnonymous(user.getName())) {
      builder.add(getUserAutocompletePermission());
      builder.add(getGroupAutocompletePermission());
      builder.add(getChangeOwnPasswordPermission(user));
      builder.add(getApiKeyPermission(user));
      builder.add(getPublicKeyPermission(user));
    }

    SimpleAuthorizationInfo info = new SimpleAuthorizationInfo(Set.of(Role.USER));
    info.addStringPermissions(builder.build());

    return info;
  }

  private String getGroupAutocompletePermission() {
    return GroupPermissions.autocomplete().asShiroString();
  }

  private String getChangeOwnPasswordPermission(User user) {
    return UserPermissions.changePassword(user).asShiroString();
  }

  private String getPublicKeyPermission(User user) {
    return UserPermissions.changePublicKeys(user).asShiroString();
  }

  private String getApiKeyPermission(User user) {
    return UserPermissions.changeApiKeys(user).asShiroString();
  }

  private String getUserAutocompletePermission() {
    return UserPermissions.autocomplete().asShiroString();
  }

  private String canReadOwnUser(User user) {
    return UserPermissions.read(user.getName()).asShiroString();
  }


  private boolean isUserPermitted(User user, Set<String> groups,
    PermissionObject perm)
  {
    //J-
    return (perm.isGroupPermission() && groups.contains(perm.getName()))
      || ((!perm.isGroupPermission()) && user.getName().equals(perm.getName()));
    //J+
  }

  @Subscribe(async = false)
  public void invalidateCache(AuthorizationChangedEvent event) {
    if (event.isEveryUserAffected()) {
      invalidateCache();
    } else {
      invalidateUserCache(event.getNameOfAffectedUser());
    }
  }

  private void invalidateUserCache(final String username) {
    logger.info("invalidate cache for user {}, because of a received authorization event", username);
    cache.removeAll((CacheKey item) -> username.equalsIgnoreCase(item.username));
  }

  private void invalidateCache() {
    logger.info("invalidate cache, because of a received authorization event");
    cache.clear();
  }

  private static class CacheKey
  {
    private final Set<String> groupnames;
    private final String username;

    private CacheKey(String username, Set<String> groupnames)
    {
      this.username = username;
      this.groupnames = groupnames;
    }

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

    @Override
    public int hashCode()
    {
      return Objects.hashCode(username, groupnames);
    }
  }

  private static class UnmodifiableAuthorizationInfo implements AuthorizationInfo {
    private final AuthorizationInfo info;

    public UnmodifiableAuthorizationInfo(AuthorizationInfo info) {
      this.info = info;
    }

    @Override
    public Collection<String> getRoles() {
      return nullsafeUnmodifiable(info.getRoles());
    }

    @Override
    public Collection<String> getStringPermissions() {
      return nullsafeUnmodifiable(info.getStringPermissions());
    }

    @Override
    public Collection<Permission> getObjectPermissions() {
      return nullsafeUnmodifiable(info.getObjectPermissions());
    }

    private <T> Collection<T> nullsafeUnmodifiable(Collection<T> collection) {
      if (collection == null) {
        return null;
      } else {
        return unmodifiableCollection(collection);
      }
    }
  }
}
