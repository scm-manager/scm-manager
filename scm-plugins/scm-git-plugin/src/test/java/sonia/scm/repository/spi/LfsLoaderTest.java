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

package sonia.scm.repository.spi;

import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lfs.Lfs;
import org.eclipse.jgit.lfs.LfsPointer;
import org.eclipse.jgit.lfs.lib.AnyLongObjectId;
import org.eclipse.jgit.lfs.lib.LongObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.transport.http.HttpConnectionFactory;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import sonia.scm.repository.GitUtil;
import sonia.scm.repository.api.MirrorCommandResult;
import sonia.scm.store.BlobStore;
import sonia.scm.web.lfs.LfsBlobStoreFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static java.util.stream.Collectors.toSet;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

public class LfsLoaderTest extends ZippedRepositoryTestBase {

  @Rule
  public MockitoRule rule = MockitoJUnit.rule();

  @Mock
  private LfsBlobStoreFactory lfsBlobStoreFactory;
  @Mock
  private BlobStore lfsBlobStore;
  @Mock
  private LfsLoader.LfsLoaderLogger lfsLoaderLogger;
  @Mock
  private MirrorCommandResult.LfsUpdateResult lfsUpdateResult;
  @Mock
  private HttpConnectionFactory httpConnectionFactory;

  private LfsLoader lfsLoader;

  private final Map<AnyLongObjectId, Path> storedBlobs = new HashMap<>();
  private Path lfsTemp;

  @Before
  public void initLfsBlobStore() {
    when(lfsBlobStoreFactory.getLfsBlobStore(repository)).thenReturn(lfsBlobStore);
  }

  @Before
  public void initLfsLoader() {
    lfsLoader = new LfsLoader(lfsBlobStoreFactory) {
      @Override
      Path downloadLfsResource(Lfs lfs, Repository gitRepository, HttpConnectionFactory connectionFactory, LfsPointer lfsPointer) throws IOException {
        Path file = lfsTemp.resolve(lfsPointer.getOid().getName());
        Files.createFile(file);
        return file;
      }

      @Override
      void storeLfsBlob(AnyLongObjectId oid, Path tempFilePath, BlobStore lfsBlobStore) {
        storedBlobs.put(oid, tempFilePath);
      }
    };
  }

  @Before
  public void initLfsTemp() throws IOException {
    lfsTemp = tempFolder.newFolder("lfs").toPath();
  }

  @Test
  public void shouldLoadAllLfsFiles() throws IOException {
    try (Repository gitRepository = GitUtil.open(repositoryDirectory)) {
      lfsLoader.loadComplete(
        gitRepository,
        lfsLoaderLogger,
        httpConnectionFactory,
        "http://localhost:8081/scm/repo.git",
        repository,
        lfsUpdateResult
      );
    }

    assertThat(storedBlobs)
      .hasSize(4)
      .containsAllEntriesOf(
        Map.of(
          LongObjectId.fromString("53c234e5e8472b6ac51c1ae1cab3fe06fad053beb8ebfd8977b010655bfdd3c3"), lfsTemp.resolve("53c234e5e8472b6ac51c1ae1cab3fe06fad053beb8ebfd8977b010655bfdd3c3"),
          LongObjectId.fromString("4355a46b19d348dc2f57c046f8ef63d4538ebb936000f3c9ee954a27460dd865"), lfsTemp.resolve("4355a46b19d348dc2f57c046f8ef63d4538ebb936000f3c9ee954a27460dd865"),
          LongObjectId.fromString("1121cfccd5913f0a63fec40a6ffd44ea64f9dc135c66634ba001d10bcf4302a2"), lfsTemp.resolve("1121cfccd5913f0a63fec40a6ffd44ea64f9dc135c66634ba001d10bcf4302a2"),
          LongObjectId.fromString("7de1555df0c2700329e815b93b32c571c3ea54dc967b89e81ab73b9972b72d1d"), lfsTemp.resolve("7de1555df0c2700329e815b93b32c571c3ea54dc967b89e81ab73b9972b72d1d")
        ));
    assertThat(lfsTemp).isEmptyDirectory();
  }

  @Test
  public void shouldIgnoreUninterestingRevisions() throws IOException {
    try (Repository gitRepository = GitUtil.open(repositoryDirectory)) {
      Collection<ObjectId> existingRefs = gatherAllRefs(gitRepository);

      lfsLoader.load(
        gitRepository,
        lfsLoaderLogger,
        httpConnectionFactory,
        "http://localhost:8081/scm/repo.git",
        repository,
        lfsUpdateResult,
        existingRefs,
        existingRefs
      );
    }

    assertThat(storedBlobs).isEmpty();
    assertThat(lfsTemp).isEmptyDirectory();
  }

  private Collection<ObjectId> gatherAllRefs(Repository gitRepository) throws IOException {
    return gitRepository.getRefDatabase().getRefs()
      .stream()
      .map(Ref::getObjectId)
      .filter(Objects::nonNull)
      .collect(toSet());
  }

  @Override
  protected String getType() {
    return "git";
  }

  @Override
  protected String getZippedRepositoryResource() {
    return "sonia/scm/repository/spi/scm-git-spi-lfs-loader-test.zip";
  }
}
