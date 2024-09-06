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

import org.junit.Test;
import sonia.scm.PageResult;
import sonia.scm.repository.Changeset;
import sonia.scm.repository.Repository;

import java.net.URI;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ChangesetCollectionToDtoMapperTest {

  public static final Repository REPOSITORY = new Repository("", "git", "space", "name");
  public static final Changeset CHANGESET = new Changeset();
  private final DefaultChangesetToChangesetDtoMapperImpl changesetToChangesetDtoMapper = mock(DefaultChangesetToChangesetDtoMapperImpl.class);

  private final ChangesetCollectionToDtoMapper changesetCollectionToDtoMapper = new ChangesetCollectionToDtoMapper(changesetToChangesetDtoMapper, ResourceLinksMock.createMock(URI.create("/")));

  @Test
  public void shouldMapCollectionEntries() {
    ChangesetDto expectedChangesetDto = new ChangesetDto();
    when(changesetToChangesetDtoMapper.map(CHANGESET, REPOSITORY)).thenReturn(expectedChangesetDto);

    CollectionDto collectionDto = changesetCollectionToDtoMapper.map(0, 1, new PageResult<>(asList(CHANGESET), 1), REPOSITORY);

    assertThat(collectionDto.getEmbedded().hasItem("changesets")).isTrue();
    assertThat(collectionDto.getEmbedded().getItemsBy("changesets")).containsExactly(expectedChangesetDto);
    assertThat(collectionDto.getEmbedded().hasItem("branch")).isFalse();
  }

  @Test
  public void shouldNotEmbedBranchIfNotSpecified() {
    ChangesetDto expectedChangesetDto = new ChangesetDto();
    when(changesetToChangesetDtoMapper.map(CHANGESET, REPOSITORY)).thenReturn(expectedChangesetDto);

    CollectionDto collectionDto = changesetCollectionToDtoMapper.map(0, 1, new PageResult<>(asList(CHANGESET), 1), REPOSITORY);

    assertThat(collectionDto.getEmbedded().hasItem("branch")).isFalse();
  }

  @Test
  public void shouldEmbedBranchIfSpecified() {
    ChangesetDto expectedChangesetDto = new ChangesetDto();
    when(changesetToChangesetDtoMapper.map(CHANGESET, REPOSITORY)).thenReturn(expectedChangesetDto);

    CollectionDto collectionDto = changesetCollectionToDtoMapper.map(0, 1, new PageResult<>(asList(CHANGESET), 1), REPOSITORY, "someBranch");

    assertThat(collectionDto.getEmbedded().hasItem("branch")).isTrue();
    assertThat(collectionDto.getEmbedded().getItemsBy("branch"))
      .hasSize(1)
      .first().matches(b -> b.getLinks().getLinkBy("self").isPresent())
      .extracting(b -> b.getLinks().getLinkBy("self").get().getHref()).isEqualTo("/v2/repositories/space/name/branches/someBranch");
    assertThat(collectionDto.getEmbedded().getItemsBy("branch"))
      .first().extracting("name").isEqualTo("someBranch");
  }
}
