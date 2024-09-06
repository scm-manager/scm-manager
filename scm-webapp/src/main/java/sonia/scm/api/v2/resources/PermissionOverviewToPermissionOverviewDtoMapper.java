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

import de.otto.edison.hal.Embedded;
import jakarta.inject.Inject;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.ObjectFactory;
import sonia.scm.group.GroupManager;
import sonia.scm.repository.Repository;
import sonia.scm.user.PermissionOverview;

import java.util.List;
import java.util.Objects;

import static de.otto.edison.hal.Links.linkingTo;
import static java.util.stream.Collectors.toList;

@Mapper
abstract class PermissionOverviewToPermissionOverviewDtoMapper {

  @Inject
  private ResourceLinks resourceLinks;
  @Inject
  private RepositoryToRepositoryDtoMapper repositoryToRepositoryDtoMapper;
  @Inject
  private NamespaceToNamespaceDtoMapper namespaceToNamespaceDtoMapper;
  @Inject
  private GroupManager groupManager;
  @Inject
  private GroupToGroupDtoMapper groupToGroupDtoMapper;

  abstract PermissionOverviewDto toDto(PermissionOverview permissionOverview, @Context String userName);

  abstract PermissionOverviewDto.GroupEntryDto toDto(PermissionOverview.GroupEntry groupEntry);

  abstract PermissionOverviewDto.RepositoryEntry toDto(Repository repository);

  @ObjectFactory
  PermissionOverviewDto createDto(PermissionOverview permissionOverview, @Context String userName) {
    List<NamespaceDto> relevantNamespaces = permissionOverview
      .getRelevantNamespaces()
      .stream()
      .map(namespaceToNamespaceDtoMapper::map)
      .collect(toList());
    List<NamespaceDto> otherNamespaces = permissionOverview
      .getRelevantRepositories()
      .stream()
      .map(Repository::getNamespace)
      .distinct()
      .filter(namespace -> !permissionOverview.getRelevantNamespaces().contains(namespace))
      .map(namespaceToNamespaceDtoMapper::map)
      .collect(toList());
    List<RepositoryDto> repositories = permissionOverview
      .getRelevantRepositories()
      .stream()
      .map(repositoryToRepositoryDtoMapper::map)
      .collect(toList());
    List<GroupDto> groups = permissionOverview
      .getRelevantGroups()
      .stream()
      .map(PermissionOverview.GroupEntry::getName)
      .map(groupManager::get)
      .filter(Objects::nonNull)
      .map(groupToGroupDtoMapper::map)
      .collect(toList());
    Embedded.Builder embedded = new Embedded.Builder()
      .with("relevantNamespaces", relevantNamespaces)
      .with("otherNamespaces", otherNamespaces)
      .with("repositories", repositories)
      .with("groups", groups);
    return new PermissionOverviewDto(
      linkingTo().self(resourceLinks.user().permissionOverview(userName)).build(),
      embedded.build()
    );
  }
}
