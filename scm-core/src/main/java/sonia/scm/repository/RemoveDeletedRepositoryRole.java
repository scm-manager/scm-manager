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
