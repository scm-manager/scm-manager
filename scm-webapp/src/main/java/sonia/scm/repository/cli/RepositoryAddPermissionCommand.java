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

import com.google.common.annotations.VisibleForTesting;
import picocli.CommandLine;
import sonia.scm.cli.ParentCommand;
import sonia.scm.repository.NamespaceAndName;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryManager;
import sonia.scm.repository.RepositoryPermission;
import sonia.scm.repository.RepositoryRoleManager;

import javax.inject.Inject;
import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@CommandLine.Command(name = "add-permission")
@ParentCommand(value = RepositoryCommand.class)
class RepositoryAddPermissionCommand implements Runnable {

  private final RepositoryManager repositoryManager;
  private final RepositoryRoleManager roleManager;

  @CommandLine.Parameters(paramLabel = "namespace/name", index = "0", descriptionKey = "scm.repo.add-permission.repository")
   private String repository;
  @CommandLine.Parameters(paramLabel = "name", index = "1", descriptionKey = "scm.repo.add-permission.name")
  private String name;
  @CommandLine.Parameters(paramLabel = "verb", index = "2", descriptionKey = "scm.repo.add-permission.verb")
  private String verb;

  @CommandLine.Option(names = {"--group", "-g"}, descriptionKey = "scm.repo.add-permission.forGroup")
  private boolean forGroup;

  @Inject
  public RepositoryAddPermissionCommand(RepositoryManager repositoryManager, RepositoryRoleManager roleManager) {
    this.repositoryManager = repositoryManager;
    this.roleManager = roleManager;
  }

  @Override
  public void run() {
    NamespaceAndName namespaceAndName = NamespaceAndName.fromString(repository);
    Repository repo = repositoryManager.get(namespaceAndName);

    Set<String> permissions =
      getExistingPermissions(repo)
        .map(this::getVerbs)
        .map(HashSet::new)
        .orElseGet(HashSet::new);
    if (!forGroup) {
      repo.findUserPermission(name).ifPresent(repo::removePermission);
    } else {
      repo.findGroupPermission(name).ifPresent(repo::removePermission);
    }
    permissions.add(verb);

    repo.addPermission(new RepositoryPermission(name, permissions, forGroup));

    repositoryManager.modify(repo);
  }

  private Optional<RepositoryPermission> getExistingPermissions(Repository repo) {
    if (!forGroup) {
      return repo.findUserPermission(name);
    } else {
      return repo.findGroupPermission(name);
    }
  }

  private Collection<String> getVerbs(RepositoryPermission permission) {
    Collection<String> effectiveVerbs;
    if (permission.getRole() == null) {
      return permission.getVerbs();
    } else {
      return roleManager.get(permission.getRole()).getVerbs();
    }
  }

  @VisibleForTesting
  void setRepository(String repository) {
    this.repository = repository;
  }

  @VisibleForTesting
  void setName(String name) {
    this.name = name;
  }

  @VisibleForTesting
  void setVerb(String verb) {
    this.verb = verb;
  }

  public void setForGroup(boolean forGroup) {
    this.forGroup = forGroup;
  }
}
