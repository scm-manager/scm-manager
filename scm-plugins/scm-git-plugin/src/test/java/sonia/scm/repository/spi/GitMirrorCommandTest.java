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
import org.eclipse.jgit.junit.http.AppServer;
import org.eclipse.jgit.junit.http.SimpleHttpServer;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.revwalk.RevObject;
import org.eclipse.jgit.revwalk.RevWalk;
import org.junit.Before;
import org.junit.Test;
import sonia.scm.api.v2.resources.GitRepositoryConfigStoreProvider;
import sonia.scm.repository.GitConfig;
import sonia.scm.repository.api.MirrorCommandResult;
import sonia.scm.repository.api.SimpleUsernamePasswordCredential;
import sonia.scm.store.InMemoryConfigurationStoreFactory;

import java.io.File;
import java.io.IOException;
import java.util.function.Consumer;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class GitMirrorCommandTest extends AbstractGitCommandTestBase {

  private final PostReceiveRepositoryHookEventFactory postReceiveRepositoryHookEventFactory = mock(PostReceiveRepositoryHookEventFactory.class);

  private File clone;
  private GitContext emptyContext;

  @Before
  public void bendContextToNewRepository() throws IOException, GitAPIException {
    clone = tempFolder.newFolder();
    Git.init().setBare(true).setDirectory(clone).call();

    emptyContext = createMirrorContext(clone);
  }

  @Test
  public void shouldCreateInitialMirror() throws IOException, GitAPIException {
    MirrorCommandResult result = callMirrorCommand(emptyContext);

    assertThat(result.isSuccess()).isTrue();
    assertThat(result.getLog()).contains("Branches:")
      .contains("- 000000000..fcd0ef183 master")
      .contains("- 000000000..3f76a12f0 test-branch")
      .contains("Tags:")
      .contains("- 000000000..86a6645ec test-tag");

    try (Git createdMirror = Git.open(clone)) {
      assertThat(createdMirror.branchList().call()).isNotEmpty();
      assertThat(createdMirror.tagList().call()).isNotEmpty();
    }

    verify(postReceiveRepositoryHookEventFactory).fireForFetch(any(), any());
  }

  @Test
  public void shouldUpdateMirror() throws IOException, GitAPIException {
    callMirrorCommand(emptyContext);

    try (Git existingClone = Git.open(repositoryDirectory)) {
      existingClone.branchCreate().setName("addedBranch").call();
      RevWalk walk = new RevWalk(existingClone.getRepository());
      ObjectId id = existingClone.getRepository().resolve("9e93d8631675a89615fac56b09209686146ff3c0");
      RevObject revObject = walk.parseAny(id);
      existingClone.tag().setName("addedTag").setAnnotated(false).setObjectId(revObject).call();
    }

    GitMirrorCommand command = new GitMirrorCommand(emptyContext, postReceiveRepositoryHookEventFactory);
    MirrorCommandRequest request = new MirrorCommandRequest();
    request.setSourceUrl(repositoryDirectory.getAbsolutePath());
    MirrorCommandResult result = command.update(request);

    assertThat(result.isSuccess()).isTrue();
    assertThat(result.getLog()).contains("Branches:")
      .contains("- 000000000..fcd0ef183 addedBranch")
      .contains("Tags:")
      .contains("- 000000000..9e93d8631 addedTag");

    try (Git updatedMirror = Git.open(clone)) {
      assertThat(updatedMirror.branchList().call()).anyMatch(ref -> ref.getName().equals("refs/heads/addedBranch"));
    }

    // event should be thrown two times, once for the initial clone, and once for the update
    verify(postReceiveRepositoryHookEventFactory, times(2)).fireForFetch(any(), any());
  }

  @Test
  public void shouldUseCredentials() throws Exception {
    SimpleHttpServer simpleHttpServer = new SimpleHttpServer(Git.open(repositoryDirectory).getRepository());
    simpleHttpServer.start();

    try {
      MirrorCommandResult result =
        callMirrorCommand(
          emptyContext,
          simpleHttpServer.getUri().toASCIIString(),
          createCredential(AppServer.username, AppServer.password));

      assertThat(result.isSuccess()).isTrue();
      assertThat(result.getLog()).contains("Branches:")
        .contains("- 000000000..fcd0ef183 master")
        .contains("- 000000000..3f76a12f0 test-branch")
        .contains("Tags:")
        .contains("- 000000000..86a6645ec test-tag");
    } finally {
      simpleHttpServer.stop();
    }
  }

  @Test
  public void shouldFailWithIncorrectCredentials() throws Exception {
    SimpleHttpServer simpleHttpServer = new SimpleHttpServer(Git.open(repositoryDirectory).getRepository());
    simpleHttpServer.start();

    try {
      MirrorCommandResult result =
        callMirrorCommand(
          emptyContext,
          simpleHttpServer.getUri().toASCIIString(),
          createCredential("wrong", "credentials"));

      assertThat(result.isSuccess()).isFalse();

      verify(postReceiveRepositoryHookEventFactory, never()).fireForFetch(any(), any());
    } finally {
      simpleHttpServer.stop();
    }
  }

  private MirrorCommandResult callMirrorCommand(GitContext context) {
    return callMirrorCommand(context, repositoryDirectory.getAbsolutePath(), c -> {});
  }

  private MirrorCommandResult callMirrorCommand(GitContext context, String source, Consumer<MirrorCommandRequest> requestConsumer) {
    GitMirrorCommand command = new GitMirrorCommand(context, postReceiveRepositoryHookEventFactory);
    MirrorCommandRequest request = new MirrorCommandRequest();
    request.setSourceUrl(source);
    requestConsumer.accept(request);
    return command.mirror(request);
  }

  private Consumer<MirrorCommandRequest> createCredential(String wrong, String credentials) {
    return request -> request.setCredentials(singletonList(new SimpleUsernamePasswordCredential(wrong, credentials.toCharArray())));
  }

  private GitContext createMirrorContext(File clone) {
    return new GitContext(clone, repository, new GitRepositoryConfigStoreProvider(InMemoryConfigurationStoreFactory.create()), new GitConfig());
  }
}
