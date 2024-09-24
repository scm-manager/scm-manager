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

package sonia.scm.repository.cli;

import jakarta.inject.Inject;
import sonia.scm.cli.PermissionDescriptionResolver;
import sonia.scm.repository.RepositoryPermission;
import sonia.scm.repository.RepositoryRoleManager;

import java.util.Collection;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

class RepositoryPermissionBeanMapper {

  private final RepositoryRoleManager roleManager;
  private final PermissionDescriptionResolver permissionDescriptionResolver;

  @Inject
  RepositoryPermissionBeanMapper(RepositoryRoleManager roleManager, PermissionDescriptionResolver permissionDescriptionResolver) {
    this.roleManager = roleManager;
    this.permissionDescriptionResolver = permissionDescriptionResolver;
  }

  Collection<RepositoryPermissionBean> createPermissionBeans(Collection<RepositoryPermission> permissions, boolean keys) {
    return permissions
      .stream()
      .map(permission -> createPermissionBean(permission, keys))
      .collect(toList());
  }

  private RepositoryPermissionBean createPermissionBean(RepositoryPermission permission, boolean keys) {
    Collection<String> effectiveVerbs;
    if (permission.getRole() == null) {
      effectiveVerbs = permission.getVerbs();
    } else {
      effectiveVerbs = roleManager.get(permission.getRole()).getVerbs();
    }
    return new RepositoryPermissionBean(
      permission.isGroupPermission(),
      permission.getName(),
      permission.getRole() == null? "CUSTOM": permission.getRole(),
      keys? effectiveVerbs: getDescriptions(effectiveVerbs)
    );
  }

  private Collection<String> getDescriptions(Collection<String> effectiveVerbs) {
    return effectiveVerbs.stream().map(this::getDescription).collect(Collectors.toList());
  }

  private String getDescription(String verb) {
    return permissionDescriptionResolver.getDescription(verb).orElse(verb);
  }
}
