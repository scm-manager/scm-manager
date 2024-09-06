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

import de.otto.edison.hal.Links;
import jakarta.inject.Inject;
import org.mapstruct.AfterMapping;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import sonia.scm.repository.Namespace;
import sonia.scm.repository.NamespacePermissions;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryPermission;
import sonia.scm.repository.RepositoryPermissions;

import java.util.Optional;

import static de.otto.edison.hal.Link.link;
import static de.otto.edison.hal.Links.linkingTo;
import static sonia.scm.api.v2.resources.RepositoryPermissionDto.GROUP_PREFIX;

@Mapper
public abstract class RepositoryPermissionToRepositoryPermissionDtoMapper {

  @Inject
  private ResourceLinks resourceLinks;

  @Mapping(target = "attributes", ignore = true) // We do not map HAL attributes
  public abstract RepositoryPermissionDto map(RepositoryPermission permission, @Context Repository repository);

  @Mapping(target = "attributes", ignore = true) // We do not map HAL attributes
  public abstract RepositoryPermissionDto map(RepositoryPermission permission, @Context Namespace namespace);

  @AfterMapping
  void appendLinks(@MappingTarget RepositoryPermissionDto target, @Context Repository repository) {
    String permissionName = getUrlPermissionName(target);
    Links.Builder linksBuilder = linkingTo()
      .self(resourceLinks.repositoryPermission().self(repository.getNamespace(), repository.getName(), permissionName));
    if (RepositoryPermissions.permissionWrite(repository).isPermitted()) {
      linksBuilder.single(link("update", resourceLinks.repositoryPermission().update(repository.getNamespace(), repository.getName(), permissionName)));
      linksBuilder.single(link("delete", resourceLinks.repositoryPermission().delete(repository.getNamespace(), repository.getName(), permissionName)));
    }
    target.add(linksBuilder.build());
  }

  @AfterMapping
  void appendLinks(@MappingTarget RepositoryPermissionDto target, @Context Namespace namespace) {
    String permissionName = getUrlPermissionName(target);
    Links.Builder linksBuilder = linkingTo()
      .self(resourceLinks.namespacePermission().self(namespace.getNamespace(), permissionName));
    if (NamespacePermissions.permissionWrite().isPermitted(namespace)) {
      linksBuilder.single(link("update", resourceLinks.namespacePermission().update(namespace.getNamespace(), permissionName)));
      linksBuilder.single(link("delete", resourceLinks.namespacePermission().delete(namespace.getNamespace(), permissionName)));
    }
    target.add(linksBuilder.build());
  }

  public String getUrlPermissionName(RepositoryPermissionDto repositoryPermissionDto) {
    return Optional.of(repositoryPermissionDto.getName())
      .filter(p -> !repositoryPermissionDto.isGroupPermission())
      .orElse(GROUP_PREFIX + repositoryPermissionDto.getName());
  }
}
