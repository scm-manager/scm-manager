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

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import sonia.scm.repository.Person;
import sonia.scm.store.Blob;
import sonia.scm.store.BlobStore;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class GitModifyCommand_LFSTest extends GitModifyCommandTestBase {

  @BeforeClass
  public static void registerFilter() {
    new GitLfsFilterContextListener().contextInitialized(null);
  }

  @AfterClass
  public static void unregisterFilter() {
    new GitLfsFilterContextListener().contextDestroyed(null);
  }

  @Test
  public void shouldCreateCommit() throws IOException, GitAPIException {
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    String newRef = createCommit("new_lfs.png", "new content", "fe32608c9ef5b6cf7e3f946480253ff76f24f4ec0678f3d0f07f9844cbff9601", outputStream);

    try (Git git = new Git(createContext().open())) {
      RevCommit lastCommit = getLastCommit(git);
      assertThat(lastCommit.getFullMessage()).isEqualTo("test commit");
      assertThat(lastCommit.getAuthorIdent().getName()).isEqualTo("Dirk Gently");
      assertThat(newRef).isEqualTo(lastCommit.toObjectId().name());
    }

    assertThat(outputStream).hasToString("new content");
  }

  @Test
  public void shouldCreateSecondCommits() throws IOException, GitAPIException {
    createCommit("new_lfs.png", "new content", "fe32608c9ef5b6cf7e3f946480253ff76f24f4ec0678f3d0f07f9844cbff9601", new ByteArrayOutputStream());

    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    String newRef = createCommit("more_lfs.png", "more content", "2c2316737c9313956dfc0083da3a2a62ce259f66484f3e26440f0d1b02dd4128", outputStream);

    try (Git git = new Git(createContext().open())) {
      RevCommit lastCommit = getLastCommit(git);
      assertThat(lastCommit.getFullMessage()).isEqualTo("test commit");
      assertThat(lastCommit.getAuthorIdent().getName()).isEqualTo("Dirk Gently");
      assertThat(newRef).isEqualTo(lastCommit.toObjectId().name());
    }

    assertThat(outputStream).hasToString("more content");
  }

  @Test
  public void shouldMoveLfsFile() throws IOException, GitAPIException {
    BlobStore blobStore = mockBlobStore();

    createCommit("new_lfs.png", "new content", "fe32608c9ef5b6cf7e3f946480253ff76f24f4ec0678f3d0f07f9844cbff9601", new ByteArrayOutputStream());

    GitModifyCommand command = createCommand();
    ModifyCommandRequest request = new ModifyCommandRequest();
    request.setCommitMessage("Move file");
    request.addRequest(new ModifyCommandRequest.MoveRequest("new_lfs.png", "moved_lfs.png", false));
    request.setAuthor(new Person("Dirk Gently", "dirk@holistic.det"));
    command.execute(request);

    verify(blobStore, never()).get(any());

    // we have to assert, that the content of the new file has not changed (that is, the lfs pointer
    // has only been moved. Therefore, we ensure that the object id (aka hash) of "moved_lfs.png"
    // stays the same (182f...)
    try (Git git = new Git(createContext().open())) {
      RevCommit lastCommit = getLastCommit(git);
      try (RevWalk walk = new RevWalk(git.getRepository())) {
        RevCommit commit = walk.parseCommit(lastCommit);
        ObjectId treeId = commit.getTree().getId();
        TreeWalk treeWalk = TreeWalk.forPath(git.getRepository(), "moved_lfs.png", treeId);
        assertThat(treeWalk.getObjectId(0).getName()).isEqualTo("182fd989777cad6d7e4c887e39e518f6a4acc5bd");
      }
    }
  }

  private String createCommit(String fileName, String content, String hashOfContent, ByteArrayOutputStream outputStream) throws IOException {
    BlobStore blobStore = mockBlobStore();
    Blob blob = mock(Blob.class);
    when(blobStore.create(hashOfContent)).thenReturn(blob);
    when(blobStore.get(hashOfContent)).thenReturn(null, blob);
    when(blob.getOutputStream()).thenReturn(outputStream);
    when(blob.getSize()).thenReturn((long) content.length());

    File newFile = Files.write(tempFolder.newFile().toPath(), content.getBytes()).toFile();

    GitModifyCommand command = createCommand();

    ModifyCommandRequest request = new ModifyCommandRequest();
    request.setCommitMessage("test commit");
    request.addRequest(new ModifyCommandRequest.CreateFileRequest(fileName, newFile, false));
    request.setAuthor(new Person("Dirk Gently", "dirk@holistic.det"));

    return command.execute(request);
  }

  private BlobStore mockBlobStore() {
    BlobStore blobStore = mock(BlobStore.class);
    when(lfsBlobStoreFactory.getLfsBlobStore(any())).thenReturn(blobStore);
    return blobStore;
  }

  @Override
  protected String getZippedRepositoryResource() {
    return "sonia/scm/repository/spi/scm-git-spi-lfs-test.zip";
  }
}
