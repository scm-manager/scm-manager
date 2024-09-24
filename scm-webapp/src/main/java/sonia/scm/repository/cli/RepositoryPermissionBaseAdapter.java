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

import sonia.scm.repository.NamespaceAndName;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryManager;

import java.util.Optional;

import static java.util.Optional.empty;
import static java.util.Optional.of;

class RepositoryPermissionBaseAdapter implements PermissionBaseAdapter<Repository> {

  private final RepositoryManager repositoryManager;
  private final RepositoryTemplateRenderer templateRenderer;

  RepositoryPermissionBaseAdapter(RepositoryManager repositoryManager, RepositoryTemplateRenderer templateRenderer) {
    this.repositoryManager = repositoryManager;
    this.templateRenderer = templateRenderer;
  }

  @Override
  public Optional<Repository> get(String repositoryNamespaceAndName) {
    NamespaceAndName namespaceAndName;
    try {
      namespaceAndName = NamespaceAndName.fromString(repositoryNamespaceAndName);
    } catch (IllegalArgumentException e) {
      templateRenderer.renderInvalidInputError();
      return empty();
    }
    Repository repository = repositoryManager.get(namespaceAndName);
    if (repository == null) {
      templateRenderer.renderNotFoundError();
      return empty();
    } else {
      return of(repository);
    }
  }

  @Override
  public void modify(Repository repository) {
    repositoryManager.modify(repository);
  }
}
