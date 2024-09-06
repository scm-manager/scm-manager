/*
 * Copyright (c) 2020 - present Cloudogu GmbH
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

package sonia.scm.user;

import jakarta.inject.Inject;
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
