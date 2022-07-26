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

package sonia.scm.repository.cli;

import picocli.CommandLine;
import sonia.scm.repository.RepositoryPermission;
import sonia.scm.repository.RepositoryPermissionHolder;
import sonia.scm.repository.RepositoryRoleManager;

import javax.inject.Inject;
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
    adapter.set(object);
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

  void renderVerbNotFoundError() {
    templateRenderer.renderVerbNotFoundError();
  }
}
