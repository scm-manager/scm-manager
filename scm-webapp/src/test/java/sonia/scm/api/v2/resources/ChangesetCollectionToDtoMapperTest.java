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
      .extracting(b -> b.getLinks().getLinkBy("self").get().getHref()).first().isEqualTo("/v2/repositories/space/name/branches/someBranch");
    assertThat(collectionDto.getEmbedded().getItemsBy("branch"))
      .first().extracting("name").first().isEqualTo("someBranch");
  }
}
