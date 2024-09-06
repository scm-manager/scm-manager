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
import sonia.scm.repository.RepositoryRole;
import sonia.scm.repository.RepositoryRolePermissions;

import java.util.Optional;

import static java.util.Optional.empty;
import static java.util.Optional.of;

public class RepositoryRoleCollectionToDtoMapper extends BasicCollectionToDtoMapper<RepositoryRole, RepositoryRoleDto, RepositoryRoleToRepositoryRoleDtoMapper> {

  private final ResourceLinks resourceLinks;

  @Inject
  public RepositoryRoleCollectionToDtoMapper(RepositoryRoleToRepositoryRoleDtoMapper repositoryRoleToDtoMapper, ResourceLinks resourceLinks) {
    super("repositoryRoles", repositoryRoleToDtoMapper);
    this.resourceLinks = resourceLinks;
  }

  public CollectionDto map(int pageNumber, int pageSize, PageResult<RepositoryRole> pageResult) {
    return map(pageNumber, pageSize, pageResult, this.createSelfLink(), this.createCreateLink());
  }

  Optional<String> createCreateLink() {
    return RepositoryRolePermissions.write().isPermitted() ? of(resourceLinks.repositoryRoleCollection().create()): empty();
  }

  String createSelfLink() {
    return resourceLinks.repositoryRoleCollection().self();
  }
}
