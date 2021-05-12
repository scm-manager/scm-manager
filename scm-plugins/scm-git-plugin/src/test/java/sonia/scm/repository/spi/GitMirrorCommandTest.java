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
import org.junit.Test;
import sonia.scm.api.v2.resources.GitRepositoryConfigStoreProvider;
import sonia.scm.repository.GitConfig;
import sonia.scm.repository.api.MirrorCommandResult;
import sonia.scm.store.InMemoryConfigurationStoreFactory;

import java.io.File;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

public class GitMirrorCommandTest extends AbstractGitCommandTestBase {

  @Test
  public void shouldCreateInitialMirror() throws IOException, GitAPIException {
    File clone = tempFolder.newFolder();

    GitContext context = createMirrorContext(clone);

    MirrorCommandResult result = callMirrorCommand(context);

    assertThat(result.isSuccess()).isTrue();

    try (Git createdMirror = Git.open(clone)) {
      assertThat(createdMirror.branchList().call()).isNotEmpty();
      assertThat(createdMirror.tagList().call()).isNotEmpty();
    }
  }

  private MirrorCommandResult callMirrorCommand(GitContext context) {
    GitMirrorCommand command = new GitMirrorCommand(context);
    MirrorCommandRequest request = new MirrorCommandRequest();
    request.setSourceUrl(repositoryDirectory.getAbsolutePath());
    return command.mirror(request);
  }

  private GitContext createMirrorContext(File clone) {
    return new GitContext(clone, repository, new GitRepositoryConfigStoreProvider(InMemoryConfigurationStoreFactory.create()), new GitConfig());
  }

  @Test
  public void shouldUpdateMirror() throws IOException, GitAPIException {
    File clone = tempFolder.newFolder();
    GitContext context = createMirrorContext(clone);
    callMirrorCommand(context);

    Git.open(repositoryDirectory).branchCreate().setName("addedBranch").call();

    GitMirrorCommand command = new GitMirrorCommand(context);
    MirrorCommandRequest request = new MirrorCommandRequest();
    request.setSourceUrl(repositoryDirectory.getAbsolutePath());
    MirrorCommandResult result = command.update(request);

    assertThat(result.isSuccess()).isTrue();

    try (Git updatedMirror = Git.open(clone)) {
      assertThat(updatedMirror.branchList().call()).anyMatch(ref -> ref.getName().equals("refs/heads/addedBranch"));
    }
  }
}
