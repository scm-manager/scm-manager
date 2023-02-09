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

package sonia.scm.user;

import sonia.scm.group.GroupCollector;
import sonia.scm.group.GroupManager;
import sonia.scm.repository.Namespace;
import sonia.scm.repository.NamespaceManager;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryManager;
import sonia.scm.repository.RepositoryPermission;
import sonia.scm.repository.RepositoryPermissionHolder;
import sonia.scm.security.PermissionAssigner;
import sonia.scm.security.PermissionDescriptor;
import sonia.scm.security.PermissionPermissions;
import sonia.scm.user.PermissionOverview.GroupEntry;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static java.util.stream.Collectors.toList;

public class PermissionOverviewCollector {

  private final GroupCollector groupCollector;
  private final PermissionAssigner permissionAssigner;
  private final GroupManager groupManager;
  private final RepositoryManager repositoryManager;
  private final NamespaceManager namespaceManager;

  @Inject
  public PermissionOverviewCollector(GroupCollector groupCollector, PermissionAssigner permissionAssigner, GroupManager groupManager, RepositoryManager repositoryManager, NamespaceManager namespaceManager) {
    this.groupCollector = groupCollector;
    this.permissionAssigner = permissionAssigner;
    this.groupManager = groupManager;
    this.repositoryManager = repositoryManager;
    this.namespaceManager = namespaceManager;
  }

  public PermissionOverview create(String userId) {
    PermissionPermissions.read().check();
    Collection<String> groupsFromLastLogin = groupCollector.fromLastLoginPlusInternal(userId);

    return new PermissionOverview(
      collectGroups(groupsFromLastLogin),
      collectNamespaces(userId, groupsFromLastLogin),
      collectRepositories(userId, groupsFromLastLogin)
    );
  }

  private Collection<GroupEntry> collectGroups(Collection<String> groupsFromLastLogin) {
    Collection<GroupEntry> groupEntries = new ArrayList<>();
    Collection<String> allGroups = groupManager.getAllNames();
    groupsFromLastLogin.forEach(groupName -> {
      Collection<PermissionDescriptor> permissionDescriptors = permissionAssigner.readPermissionsForGroup(groupName);
      groupEntries.add(
        new GroupEntry(
          groupName,
          !permissionDescriptors.isEmpty(),
          !allGroups.contains(groupName)));
    });
    return groupEntries;
  }

  private Collection<String> collectNamespaces(String userId, Collection<String> groupsFromLastLogin) {
    return namespaceManager
      .getAll()
      .stream()
      .filter(namespace -> isRelevant(userId, groupsFromLastLogin, namespace))
      .map(Namespace::getNamespace)
      .collect(toList());
  }

  private List<Repository> collectRepositories(String userId, Collection<String> groupsFromLastLogin) {
    return repositoryManager
      .getAll()
      .stream()
      .filter(repo -> isRelevant(userId, groupsFromLastLogin, repo))
      .collect(toList());
  }

  private static boolean isRelevant(String userId, Collection<String> groupsFromLastLogin, RepositoryPermissionHolder permissionHolder) {
    return permissionHolder.getPermissions().stream().anyMatch(permission -> isRelevant(userId, groupsFromLastLogin, permission));
  }

  private static boolean isRelevant(String userId, Collection<String> groupsFromLastLogin, RepositoryPermission permission) {
    return permission.isGroupPermission() && groupsFromLastLogin.contains(permission.getName())
      || !permission.isGroupPermission() && userId.equals(permission.getName());
  }
}
