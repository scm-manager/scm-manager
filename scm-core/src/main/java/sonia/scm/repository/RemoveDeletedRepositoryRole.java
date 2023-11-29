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
    
package sonia.scm.repository;

import com.github.legman.Subscribe;
import jakarta.inject.Inject;
import sonia.scm.EagerSingleton;
import sonia.scm.plugin.Extension;

import java.util.Optional;

import static sonia.scm.HandlerEventType.DELETE;

@EagerSingleton
@Extension
public class RemoveDeletedRepositoryRole {

  private final RepositoryManager repositoryManager;

  @Inject
  public RemoveDeletedRepositoryRole(RepositoryManager repositoryManager) {
    this.repositoryManager = repositoryManager;
  }

  @Subscribe
  void handle(RepositoryRoleEvent event) {
    if (event.getEventType() == DELETE) {
      repositoryManager.getAll()
        .forEach(repository -> check(repository, event.getItem()));
    }
  }

  private void check(Repository repository, RepositoryRole role) {
    findPermission(repository, role)
      .ifPresent(permission -> removeFromPermissions(repository, permission));
  }

  private Optional<RepositoryPermission> findPermission(Repository repository, RepositoryRole item) {
    return repository.getPermissions()
      .stream()
      .filter(repositoryPermission -> item.getName().equals(repositoryPermission.getRole()))
      .findFirst();
  }

  private void removeFromPermissions(Repository repository, RepositoryPermission permission) {
    repository.removePermission(permission);
    repositoryManager.modify(repository);
  }
}
