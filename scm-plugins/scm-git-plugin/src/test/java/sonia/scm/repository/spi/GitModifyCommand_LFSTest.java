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

package sonia.scm.repository.spi;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.revwalk.RevCommit;
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

    assertThat(outputStream.toString()).isEqualTo("new content");
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

    assertThat(outputStream.toString()).isEqualTo("more content");
  }

  private String createCommit(String fileName, String content, String hashOfContent, ByteArrayOutputStream outputStream) throws IOException {
    BlobStore blobStore = mock(BlobStore.class);
    Blob blob = mock(Blob.class);
    when(lfsBlobStoreFactory.getLfsBlobStore(any())).thenReturn(blobStore);
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

  @Override
  protected String getZippedRepositoryResource() {
    return "sonia/scm/repository/spi/scm-git-spi-lfs-test.zip";
  }
}
