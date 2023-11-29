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
