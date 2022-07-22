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
import sonia.scm.repository.Namespace;
import sonia.scm.repository.NamespaceManager;
import sonia.scm.repository.RepositoryPermission;
import sonia.scm.repository.RepositoryRoleManager;

import javax.inject.Inject;
import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.function.Predicate;

class NamespacePermissionBaseCommand {

  private final NamespaceManager namespaceManager;
  private final RepositoryRoleManager roleManager;
  @CommandLine.Mixin
  private final RepositoryTemplateRenderer templateRenderer;

  @Inject
  NamespacePermissionBaseCommand(NamespaceManager namespaceManager, RepositoryRoleManager roleManager, RepositoryTemplateRenderer templateRenderer) {
    this.namespaceManager = namespaceManager;
    this.roleManager = roleManager;
    this.templateRenderer = templateRenderer;
  }

  void modifyNamespace(String namespace, Predicate<Namespace> modifier) {
    Optional<Namespace> ns = namespaceManager.get(namespace);
    if (ns.isPresent()) {
      if (modifier.test(ns.get())) {
        namespaceManager.modify(ns.get());
      }
    } else {
      templateRenderer.renderNotFoundError();
    }
  }

  void replacePermission(Namespace namespace, RepositoryPermission permission) {
    removeExistingPermission(namespace, permission.getName(), permission.isGroupPermission());
    namespace.addPermission(permission);
  }

  void removeExistingPermission(Namespace namespace, String name, boolean forGroup) {
    if (!forGroup) {
      namespace.findUserPermission(name).ifPresent(namespace::removePermission);
    } else {
      namespace.findGroupPermission(name).ifPresent(namespace::removePermission);
    }
  }

  HashSet<String> getPermissionsAsModifiableSet(Namespace namespace, String name, boolean forGroup) {
    return getExistingPermissions(namespace, name, forGroup)
      .map(this::getVerbs)
      .map(HashSet::new)
      .orElseGet(HashSet::new);
  }

  private Optional<RepositoryPermission> getExistingPermissions(Namespace namespace, String name, boolean forGroup) {
    if (!forGroup) {
      return namespace.findUserPermission(name);
    } else {
      return namespace.findGroupPermission(name);
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
