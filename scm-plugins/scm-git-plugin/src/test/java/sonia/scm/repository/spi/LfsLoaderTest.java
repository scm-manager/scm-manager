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

import org.eclipse.jgit.lfs.Lfs;
import org.eclipse.jgit.lfs.LfsPointer;
import org.eclipse.jgit.lfs.lib.AnyLongObjectId;
import org.eclipse.jgit.lfs.lib.LongObjectId;
import org.eclipse.jgit.lib.ObjectId;
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/*
 * This test uses a repository with the following layout:
   * 7100a0f (branch/b) Add fourth png
   * d89ee93 (branch/a) Add third png
   | * f3134d6 (master) Add second png
   |/
   * 62c8598 Add first png
   * cccd744 init lfs
 *
 * Each commit with the text "Add ..." adds exactly one lfs file to the repository.
 */
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

  private LfsLoader.EntryHandler entryHandler;
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

      @Override
      EntryHandler createEntryHandler(Repository gitRepository, LfsLoaderLogger mirrorLog, MirrorCommandResult.LfsUpdateResult lfsUpdateResult, sonia.scm.repository.Repository repository, HttpConnectionFactory httpConnectionFactory) {
        entryHandler = spy(super.createEntryHandler(gitRepository, mirrorLog, lfsUpdateResult, repository, httpConnectionFactory));
        return entryHandler;
      }
    };
  }

  @Before
  public void initLfsTemp() throws IOException {
    lfsTemp = tempFolder.newFolder("lfs").toPath();
  }

  @Test
  public void shouldLoadAllLfsFiles() throws IOException {
    lfsLoader.inspectTree(
      ObjectId.fromString("f3134d622484981329034ef63c6c1c9b0e5c5232"),
      GitUtil.open(repositoryDirectory),
      lfsLoaderLogger,
      lfsUpdateResult,
      repository,
      httpConnectionFactory,
      "http://localhost:8081/scm/repo.git"
    );

    assertThat(storedBlobs)
      .hasSize(2)
      .containsAllEntriesOf(
        Map.of(
          LongObjectId.fromString("53c234e5e8472b6ac51c1ae1cab3fe06fad053beb8ebfd8977b010655bfdd3c3"), lfsTemp.resolve("53c234e5e8472b6ac51c1ae1cab3fe06fad053beb8ebfd8977b010655bfdd3c3"),
          LongObjectId.fromString("4355a46b19d348dc2f57c046f8ef63d4538ebb936000f3c9ee954a27460dd865"), lfsTemp.resolve("4355a46b19d348dc2f57c046f8ef63d4538ebb936000f3c9ee954a27460dd865")
        ));
    assertThat(lfsTemp).isEmptyDirectory();
  }

  @Test
  public void shouldCheckLfsFilesOnlyOnce() throws IOException {
    Set<ObjectId> alreadyVisited = new HashSet<>();

    try (Repository repository = GitUtil.open(repositoryDirectory)) {
      asList(
        "d89ee931a676bec618177fb4ae6e056f8907a82b", // branch/a
        "7100a0f377b142a9a746ee0b18cb4124309c0900"  // branch/b
      )
        .forEach(
          objId -> lfsLoader.inspectTree(
            ObjectId.fromString(objId),
            repository,
            lfsLoaderLogger,
            lfsUpdateResult,
            this.repository,
            httpConnectionFactory,
            "http://localhost:8081/scm/repo.git",
            alreadyVisited
          )
        );
    }

    assertThat(storedBlobs).hasSize(3);
    // The second entry handler created for the last revision (branch/b) should be used only once, because all other
    // object ids for this revision have been handled by the other revision (branch/a) before:
    verify(entryHandler).handleTreeEntry(any());
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
