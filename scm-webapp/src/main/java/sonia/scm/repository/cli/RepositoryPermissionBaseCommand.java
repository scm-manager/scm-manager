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
import sonia.scm.repository.NamespaceAndName;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryManager;
import sonia.scm.repository.RepositoryPermission;
import sonia.scm.repository.RepositoryRoleManager;

import javax.inject.Inject;
import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.function.Predicate;

class RepositoryPermissionBaseCommand {

  private final RepositoryManager repositoryManager;
  private final RepositoryRoleManager roleManager;
  @CommandLine.Mixin
  private final RepositoryTemplateRenderer templateRenderer;

  @Inject
  RepositoryPermissionBaseCommand(RepositoryManager repositoryManager, RepositoryRoleManager roleManager, RepositoryTemplateRenderer templateRenderer) {
    this.repositoryManager = repositoryManager;
    this.roleManager = roleManager;
    this.templateRenderer = templateRenderer;
  }

  void modifyRepository(String repositoryName, Predicate<Repository> modifier) {
    NamespaceAndName namespaceAndName;
    try {
      namespaceAndName = NamespaceAndName.fromString(repositoryName);
    } catch (IllegalArgumentException e) {
      templateRenderer.renderInvalidInputError();
      return;
    }

    Repository repository = repositoryManager.get(namespaceAndName);
    if (repository != null) {
      if (modifier.test(repository)) {
        repositoryManager.modify(repository);
      }
    } else {
      templateRenderer.renderNotFoundError();
    }
  }

  void replacePermission(Repository repository, RepositoryPermission permission) {
    this.removeExistingPermission(repository, permission.getName(), permission.isGroupPermission());
    repository.addPermission(permission);
  }

  void removeExistingPermission(Repository repository, String name, boolean forGroup) {
    if (!forGroup) {
      repository.findUserPermission(name).ifPresent(repository::removePermission);
    } else {
      repository.findGroupPermission(name).ifPresent(repository::removePermission);
    }
  }

  HashSet<String> getPermissionsAsModifiableSet(Repository repository, String name, boolean forGroup) {
    return this.getExistingPermissions(repository, name, forGroup)
      .map(this::getVerbs)
      .map(HashSet::new)
      .orElseGet(HashSet::new);
  }

  private Optional<RepositoryPermission> getExistingPermissions(Repository repo, String name, boolean forGroup) {
    if (!forGroup) {
      return repo.findUserPermission(name);
    } else {
      return repo.findGroupPermission(name);
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
