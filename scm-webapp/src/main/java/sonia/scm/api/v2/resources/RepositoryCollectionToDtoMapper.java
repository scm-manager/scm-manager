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

package sonia.scm.api.v2.resources;

import jakarta.inject.Inject;
import sonia.scm.PageResult;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryPermissions;

import java.util.Optional;

import static java.util.Optional.empty;
import static java.util.Optional.of;

public class RepositoryCollectionToDtoMapper extends BasicCollectionToDtoMapper<Repository, RepositoryDto, RepositoryToRepositoryDtoMapper> {

  private final ResourceLinks resourceLinks;

  @Inject
  public RepositoryCollectionToDtoMapper(RepositoryToRepositoryDtoMapper repositoryToDtoMapper, ResourceLinks resourceLinks) {
    super("repositories", repositoryToDtoMapper);
    this.resourceLinks = resourceLinks;
  }

  public CollectionDto map(String namespace, int pageNumber, int pageSize, PageResult<Repository> pageResult) {
    return map(pageNumber, pageSize, pageResult, this.createSelfLink(namespace), this.createCreateLink());
  }

  public CollectionDto map(int pageNumber, int pageSize, PageResult<Repository> pageResult) {
    return map(pageNumber, pageSize, pageResult, this.createSelfLink(), this.createCreateLink());
  }

  Optional<String> createCreateLink() {
    return RepositoryPermissions.create().isPermitted() ? of(resourceLinks.repositoryCollection().create()): empty();
  }

  String createSelfLink() {
    return resourceLinks.repositoryCollection().self();
  }

  String createSelfLink(String namespace) {
    return resourceLinks.repositoryCollection().forNamespace(namespace);
  }
}
