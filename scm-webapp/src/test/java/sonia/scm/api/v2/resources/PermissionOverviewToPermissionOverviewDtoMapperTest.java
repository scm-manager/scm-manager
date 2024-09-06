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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.group.Group;
import sonia.scm.group.GroupManager;
import sonia.scm.repository.Repository;
import sonia.scm.user.PermissionOverview;

import java.net.URI;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PermissionOverviewToPermissionOverviewDtoMapperTest {

  public static final Repository REPOSITORY_1 = new Repository("1", "git", "hog", "marvin");
  public static final Repository REPOSITORY_2 = new Repository("1", "git", "vogon", "jeltz");

  public static final PermissionOverview PERMISSION_OVERVIEW = new PermissionOverview(
    asList(
      new PermissionOverview.GroupEntry("hitchhiker", true, false),
      new PermissionOverview.GroupEntry("vogons", false, true)
    ),
    asList("hog", "earth"),
    asList(
      REPOSITORY_1,
      REPOSITORY_2
    )
  );

  @Mock
  private ResourceLinks resourceLinks;
  @Mock
  private NamespaceToNamespaceDtoMapper namespaceToNamespaceDtoMapper;
  @Mock
  private RepositoryToRepositoryDtoMapper repositoryToRepositoryDtoMapper;
  @Mock
  private GroupManager groupManager;
  @Mock
  private GroupToGroupDtoMapper groupToGroupDtoMapper;

  @InjectMocks
  private PermissionOverviewToPermissionOverviewDtoMapperImpl permissionOverviewToPermissionOverviewDtoMapper;

  @BeforeEach
  void initResourceLinks() {
    when(resourceLinks.user())
      .thenReturn(new ResourceLinks.UserLinks(() -> URI.create("/")));
  }

  @BeforeEach
  void initRepositoryMapper() {
    when(repositoryToRepositoryDtoMapper.map(any()))
      .thenAnswer(invocation -> {
        Repository repository = invocation.getArgument(0, Repository.class);
        RepositoryDto repositoryDto = new RepositoryDto();
        repositoryDto.setNamespace(repository.getNamespace());
        repositoryDto.setName(repository.getName());
        return repositoryDto;
      });
  }

  @BeforeEach
  void initNamespaceMapper() {
    when(namespaceToNamespaceDtoMapper.map(any(String.class)))
      .thenAnswer(invocation -> new NamespaceDto(invocation.getArgument(0, String.class), Links.emptyLinks()));
  }

  @Nested
  class WithInternalGroups {
    @BeforeEach
    void initGroupMapper() {
      when(groupManager.get(anyString()))
        .thenAnswer(invocation -> new Group("xml", invocation.getArgument(0, String.class)));
      when(groupToGroupDtoMapper.map(any()))
        .thenAnswer(invocation -> {
          GroupDto groupDto = new GroupDto();
          groupDto.setName(invocation.getArgument(0, Group.class).getName());
          return groupDto;
        });
    }

    @Test
    void shouldMapRepositories() {
      PermissionOverviewDto dto = permissionOverviewToPermissionOverviewDtoMapper
        .toDto(PERMISSION_OVERVIEW, "Neo");

      assertThat(dto.getRelevantRepositories())
        .extracting("namespace")
        .contains("hog", "vogon");
      assertThat(dto.getRelevantRepositories())
        .extracting("name")
        .contains("marvin", "jeltz");

      assertThat(
        dto.
          getEmbedded()
          .getItemsBy("repositories")
      ).hasSize(2);
    }

    @Test
    void shouldMapNamespaces() {
      PermissionOverviewDto dto = permissionOverviewToPermissionOverviewDtoMapper
        .toDto(PERMISSION_OVERVIEW, "Neo");

      assertThat(dto.getRelevantNamespaces())
        .contains("hog", "earth");

      assertThat(dto.getEmbedded().getItemsBy("relevantNamespaces"))
        .hasSize(2)
        .extracting("namespace")
        .contains("hog", "earth");
      assertThat(dto.getEmbedded().getItemsBy("otherNamespaces"))
        .hasSize(1)
        .extracting("namespace")
        .contains("vogon");
    }

    @Test
    void shouldMapGroups() {
      PermissionOverviewDto dto = permissionOverviewToPermissionOverviewDtoMapper
        .toDto(PERMISSION_OVERVIEW, "Neo");

      assertThat(dto.getRelevantGroups())
        .extracting("name")
        .contains("hitchhiker", "vogons");

      assertThat(dto.getEmbedded().getItemsBy("groups"))
        .hasSize(2)
        .extracting("name")
        .contains("hitchhiker", "vogons");
    }
  }

  @Test
  void shouldExcludeExternalGroups() {
    when(groupManager.get(anyString()))
      .thenAnswer(invocation -> null);

    PermissionOverviewDto dto = permissionOverviewToPermissionOverviewDtoMapper
      .toDto(PERMISSION_OVERVIEW, "Neo");

    assertThat(dto.getRelevantGroups())
      .extracting("name")
      .contains("hitchhiker", "vogons");

    assertThat(dto.getEmbedded().getItemsBy("groups"))
      .isEmpty();
  }
}
