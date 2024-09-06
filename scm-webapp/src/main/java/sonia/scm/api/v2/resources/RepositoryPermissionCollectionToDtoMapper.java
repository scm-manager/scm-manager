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

import com.google.inject.Inject;
import de.otto.edison.hal.Embedded;
import de.otto.edison.hal.HalRepresentation;
import de.otto.edison.hal.Links;
import sonia.scm.repository.Namespace;
import sonia.scm.repository.NamespacePermissions;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryPermissions;

import java.util.List;

import static de.otto.edison.hal.Embedded.embeddedBuilder;
import static de.otto.edison.hal.Link.link;
import static de.otto.edison.hal.Links.linkingTo;
import static java.util.stream.Collectors.toList;

public class RepositoryPermissionCollectionToDtoMapper {

  private final ResourceLinks resourceLinks;
  private final RepositoryPermissionToRepositoryPermissionDtoMapper repositoryPermissionToRepositoryPermissionDtoMapper;

  @Inject
  public RepositoryPermissionCollectionToDtoMapper(RepositoryPermissionToRepositoryPermissionDtoMapper repositoryPermissionToRepositoryPermissionDtoMapper, ResourceLinks resourceLinks) {
    this.resourceLinks = resourceLinks;
    this.repositoryPermissionToRepositoryPermissionDtoMapper = repositoryPermissionToRepositoryPermissionDtoMapper;
  }

  public HalRepresentation map(Repository repository) {
    List<RepositoryPermissionDto> repositoryPermissionDtoList = repository.getPermissions()
      .stream()
      .map(permission -> repositoryPermissionToRepositoryPermissionDtoMapper.map(permission, repository))
      .collect(toList());
    return new HalRepresentation(createLinks(repository), embedDtos(repositoryPermissionDtoList));
  }

  public HalRepresentation map(Namespace namespace) {
    List<RepositoryPermissionDto> repositoryPermissionDtoList = namespace.getPermissions()
      .stream()
      .map(permission -> repositoryPermissionToRepositoryPermissionDtoMapper.map(permission, namespace))
      .collect(toList());
    return new HalRepresentation(createLinks(namespace), embedDtos(repositoryPermissionDtoList));
  }

  private Links createLinks(Repository repository) {
    RepositoryPermissions.permissionRead(repository).check();
    Links.Builder linksBuilder = linkingTo()
      .with(Links.linkingTo().self(resourceLinks.repositoryPermission().all(repository.getNamespace(), repository.getName())).build());
    if (RepositoryPermissions.permissionWrite(repository).isPermitted()) {
      linksBuilder.single(link("create", resourceLinks.repositoryPermission().create(repository.getNamespace(), repository.getName())));
    }
    return linksBuilder.build();
  }

  private Links createLinks(Namespace namespace) {
    NamespacePermissions.permissionRead().check(namespace);
    Links.Builder linksBuilder = linkingTo()
      .with(Links.linkingTo().self(resourceLinks.namespacePermission().all(namespace.getNamespace())).build());
    if (NamespacePermissions.permissionWrite().isPermitted(namespace)) {
      linksBuilder.single(link("create", resourceLinks.namespacePermission().create(namespace.getNamespace())));
    }
    return linksBuilder.build();
  }

  private Embedded embedDtos(List<RepositoryPermissionDto> repositoryPermissionDtoList) {
    return embeddedBuilder()
      .with("permissions", repositoryPermissionDtoList)
      .build();
  }
}
