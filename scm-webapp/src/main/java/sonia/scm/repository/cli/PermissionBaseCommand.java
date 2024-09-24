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
import picocli.CommandLine;
import sonia.scm.repository.RepositoryPermission;
import sonia.scm.repository.RepositoryPermissionHolder;
import sonia.scm.repository.RepositoryRoleManager;

import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.function.Predicate;

abstract class PermissionBaseCommand<T extends RepositoryPermissionHolder> {

  private final RepositoryRoleManager roleManager;
  @CommandLine.Mixin
  private final RepositoryTemplateRenderer templateRenderer;
  private final PermissionBaseAdapter<T> adapter;

  @Inject
  PermissionBaseCommand(RepositoryRoleManager roleManager, RepositoryTemplateRenderer templateRenderer, PermissionBaseAdapter<T> adapter) {
    this.roleManager = roleManager;
    this.templateRenderer = templateRenderer;
    this.adapter = adapter;
  }

  Optional<T> get(String identifier) {
    return adapter.get(identifier);
  }

  void set(T object) {
    adapter.modify(object);
  }

  void modify(String identifier, Predicate<T> modifier) {
    Optional<T> ns = get(identifier);
    if (ns.isPresent() && modifier.test(ns.get())) {
      set(ns.get());
    }
  }

  void replacePermission(T namespace, RepositoryPermission permission) {
    removeExistingPermission(namespace, permission.getName(), permission.isGroupPermission());
    namespace.addPermission(permission);
  }

  void removeExistingPermission(T permissionHolder, String name, boolean forGroup) {
    if (!forGroup) {
      permissionHolder.findUserPermission(name).ifPresent(permissionHolder::removePermission);
    } else {
      permissionHolder.findGroupPermission(name).ifPresent(permissionHolder::removePermission);
    }
  }

  HashSet<String> getPermissionsAsModifiableSet(T permissionHolder, String name, boolean forGroup) {
    return getExistingPermissions(permissionHolder, name, forGroup)
      .map(this::getVerbs)
      .map(HashSet::new)
      .orElseGet(HashSet::new);
  }

  private Optional<RepositoryPermission> getExistingPermissions(T permissionHolder, String name, boolean forGroup) {
    if (!forGroup) {
      return permissionHolder.findUserPermission(name);
    } else {
      return permissionHolder.findGroupPermission(name);
    }
  }

  private Collection<String> getVerbs(RepositoryPermission permission) {
    if (permission.getRole() == null) {
      return permission.getVerbs();
    } else {
      return roleManager.get(permission.getRole()).getVerbs();
    }
  }

  void renderRoleNotFoundError() {
    templateRenderer.renderRoleNotFoundError();
  }

  void renderVerbNotFoundError(String verb) {
    templateRenderer.renderVerbNotFoundError(verb);
  }
}
