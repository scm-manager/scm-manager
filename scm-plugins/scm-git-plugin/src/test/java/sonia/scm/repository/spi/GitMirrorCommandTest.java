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
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.revwalk.RevObject;
import org.eclipse.jgit.revwalk.RevWalk;
import org.junit.Before;
import org.junit.Test;
import sonia.scm.api.v2.resources.GitRepositoryConfigStoreProvider;
import sonia.scm.repository.GitChangesetConverter;
import sonia.scm.repository.GitChangesetConverterFactory;
import sonia.scm.repository.GitConfig;
import sonia.scm.repository.api.MirrorCommandResult;
import sonia.scm.repository.api.MirrorFilter;
import sonia.scm.repository.api.SimpleUsernamePasswordCredential;
import sonia.scm.store.InMemoryConfigurationStoreFactory;

import java.io.File;
import java.io.IOException;
import java.util.Optional;
import java.util.function.Consumer;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class GitMirrorCommandTest extends AbstractGitCommandTestBase {

  public static final Consumer<MirrorCommandRequest> ACCEPT_ALL = r -> {
  };
  public static final Consumer<MirrorCommandRequest> REJECT_ALL = r -> r.setFilter(new DenyAllMirrorFilter());
  private final PostReceiveRepositoryHookEventFactory postReceiveRepositoryHookEventFactory = mock(PostReceiveRepositoryHookEventFactory.class);
  private final MirrorHttpConnectionProvider mirrorHttpConnectionProvider = mock(MirrorHttpConnectionProvider.class);
  private final GitChangesetConverterFactory gitChangesetConverterFactory = mock(GitChangesetConverterFactory.class);
  private final GitChangesetConverter gitChangesetConverter = mock(GitChangesetConverter.class);
  private final GitTagConverter gitTagConverter = mock(GitTagConverter.class);

  private File clone;
  private GitContext emptyContext;
  private GitMirrorCommand command;

  @Before
  public void initChangesetConverter() {
    when(gitChangesetConverterFactory.create(any(), any())).thenReturn(gitChangesetConverter);
//    when(gitChangesetConverter.createChangeset(any())).thenAnswer(invocation -> new Changeset(invocation.getArgument(0, RevCommit.class).getName(), 0L, null));
  }

  @Before
  public void bendContextToNewRepository() throws IOException, GitAPIException {
    clone = tempFolder.newFolder();
    Git.init().setBare(true).setDirectory(clone).call();

    emptyContext = createMirrorContext(clone);
    command = new GitMirrorCommand(emptyContext, postReceiveRepositoryHookEventFactory, mirrorHttpConnectionProvider, gitChangesetConverterFactory, gitTagConverter);
  }

  @Test
  public void shouldCreateInitialMirror() throws IOException, GitAPIException {
    MirrorCommandResult result = callMirrorCommand();

    assertThat(result.isSuccess()).isTrue();
    assertThat(result.getLog()).contains("Branches:")
      .contains("- 000000000..fcd0ef183 master (new)")
      .contains("- 000000000..3f76a12f0 test-branch (new)")
      .contains("Tags:")
      .contains("- 000000000..86a6645ec test-tag (new)");

    try (Git createdMirror = Git.open(clone)) {
      assertThat(createdMirror.branchList().call()).isNotEmpty();
      assertThat(createdMirror.tagList().call()).isNotEmpty();
    }

    verify(postReceiveRepositoryHookEventFactory).fireForFetch(any(), any());
  }

  @Test
  public void shouldUpdateMirrorWithNewBranch() throws IOException, GitAPIException {
    callMirrorCommand();

    try (Git existingClone = Git.open(repositoryDirectory)) {
      existingClone.branchCreate().setName("added-branch").call();
    }

    MirrorCommandResult result = callUpdate(ACCEPT_ALL);

    assertThat(result.isSuccess()).isTrue();
    assertThat(result.getLog()).containsExactly(
      "Branches:",
      "- 000000000..fcd0ef183 added-branch (new)",
      "Tags:"
    );

    try (Git updatedMirror = Git.open(clone)) {
      Optional<Ref> addedBranch = findBranch(updatedMirror, "added-branch");
      assertThat(addedBranch).isPresent();
    }

    // event should be thrown two times, once for the initial clone, and once for the update
    verify(postReceiveRepositoryHookEventFactory, times(2)).fireForFetch(any(), any());
  }

  @Test
  public void shouldUpdateMirrorWithForcedBranch() throws IOException, GitAPIException {
    callMirrorCommand();

    try (Git existingClone = Git.open(repositoryDirectory)) {
      existingClone.branchCreate().setStartPoint("9e93d8631675a89615fac56b09209686146ff3c0").setName("test-branch").setForce(true).call();
    }

    MirrorCommandResult result = callUpdate(ACCEPT_ALL);

    assertThat(result.isSuccess()).isTrue();
    assertThat(result.getLog()).containsExactly(
      "Branches:",
      "- 3f76a12f0..9e93d8631 test-branch (forced)",
      "Tags:"
    );

    try (Git updatedMirror = Git.open(clone)) {
      Optional<Ref> updatedBranch = findBranch(updatedMirror, "test-branch");
      assertThat(updatedBranch.get().getObjectId().getName()).isEqualTo("9e93d8631675a89615fac56b09209686146ff3c0");
    }
  }

  @Test
  public void shouldUpdateMirrorWithDeletedBranch() throws IOException, GitAPIException {
    callMirrorCommand();

    try (Git existingClone = Git.open(repositoryDirectory)) {
      existingClone.branchDelete().setBranchNames("test-branch").setForce(true).call();
    }

    MirrorCommandResult result = callUpdate(ACCEPT_ALL);

    assertThat(result.isSuccess()).isTrue();
    assertThat(result.getLog()).containsExactly(
      "Branches:",
      "- 3f76a12f0..000000000 test-branch (deleted)",
      "Tags:"
    );

    try (Git updatedMirror = Git.open(clone)) {
      Optional<Ref> deletedBranch = findBranch(updatedMirror, "test-branch");
      assertThat(deletedBranch).isNotPresent();
    }
  }

  @Test
  public void shouldUpdateMirrorWithNewTag() throws IOException, GitAPIException {
    callMirrorCommand();

    try (Git existingClone = Git.open(repositoryDirectory)) {
      RevObject revObject = getRevObject(existingClone);
      existingClone.tag().setName("added-tag").setAnnotated(false).setObjectId(revObject).call();
    }

    MirrorCommandResult result = callUpdate(ACCEPT_ALL);

    assertThat(result.isSuccess()).isTrue();
    assertThat(result.getLog()).containsExactly(
      "Branches:",
      "Tags:",
      "- 000000000..9e93d8631 added-tag (new)"
    );

    try (Git updatedMirror = Git.open(clone)) {
      Optional<Ref> addedTag = findTag(updatedMirror, "added-tag");
      assertThat(addedTag.get().getObjectId().getName()).isEqualTo("9e93d8631675a89615fac56b09209686146ff3c0");
    }

    // event should be thrown two times, once for the initial clone, and once for the update
    verify(postReceiveRepositoryHookEventFactory, times(2)).fireForFetch(any(), any());
  }

  @Test
  public void shouldUpdateMirrorWithChangedTag() throws IOException, GitAPIException {
    callMirrorCommand();

    try (Git existingClone = Git.open(repositoryDirectory)) {
      RevObject revObject = getRevObject(existingClone);
      existingClone.tag().setName("test-tag").setObjectId(revObject).setForceUpdate(true).setAnnotated(false).call();
    }

    MirrorCommandResult result = callUpdate(ACCEPT_ALL);

    assertThat(result.isSuccess()).isTrue();
    assertThat(result.getLog()).containsExactly(
      "Branches:",
      "Tags:",
      "- 86a6645ec..9e93d8631 test-tag (forced)"
    );

    try (Git updatedMirror = Git.open(clone)) {
      Optional<Ref> updatedTag = findTag(updatedMirror, "test-tag");
      assertThat(updatedTag.get().getObjectId().getName()).isEqualTo("9e93d8631675a89615fac56b09209686146ff3c0");
    }
  }

  @Test
  public void shouldUpdateMirrorWithDeletedTag() throws IOException, GitAPIException {
    callMirrorCommand();

    try (Git existingClone = Git.open(repositoryDirectory)) {
      existingClone.tagDelete().setTags("test-tag").call();
    }

    MirrorCommandResult result = callUpdate(ACCEPT_ALL);

    assertThat(result.isSuccess()).isTrue();
    assertThat(result.getLog()).containsExactly(
      "Branches:",
      "Tags:",
      "- 86a6645ec..000000000 test-tag (deleted)"
    );

    try (Git updatedMirror = Git.open(clone)) {
      Optional<Ref> deletedTag = findTag(updatedMirror, "test-tag");
      assertThat(deletedTag).isNotPresent();
    }
  }

  @Test
  public void shouldRevertRejectedAddedBranch() throws IOException, GitAPIException {
    callMirrorCommand();

    try (Git existingClone = Git.open(repositoryDirectory)) {
      existingClone.branchCreate().setName("added-branch").call();
    }

    MirrorCommandResult result = callUpdate(REJECT_ALL);

    assertThat(result.isSuccess()).isTrue();
    assertThat(result.getLog()).containsExactly(
      "Branches:",
      "- 000000000..fcd0ef183 added-branch (rejected due to filter)",
      "Tags:"
    );

    try (Git updatedMirror = Git.open(clone)) {
      Optional<Ref> rejectedBranch = findBranch(updatedMirror, "added-branch");
      assertThat(rejectedBranch).isNotPresent();
    }
  }

  @Test
  public void shouldRevertRejectedChangedBranch() throws IOException, GitAPIException {
    callMirrorCommand();

    try (Git existingClone = Git.open(repositoryDirectory)) {
      existingClone.branchCreate().setStartPoint("9e93d8631675a89615fac56b09209686146ff3c0").setName("test-branch").setForce(true).call();
    }

    MirrorCommandResult result = callUpdate(REJECT_ALL);

    assertThat(result.isSuccess()).isTrue();
    assertThat(result.getLog()).containsExactly(
      "Branches:",
      "- 3f76a12f0..9e93d8631 test-branch (rejected due to filter)",
      "Tags:"
    );

    try (Git updatedMirror = Git.open(clone)) {
      Optional<Ref> rejectedBranch = findBranch(updatedMirror, "test-branch");
      assertThat(rejectedBranch).get().extracting("objectId.name").hasToString("3f76a12f08a6ba0dc988c68b7f0b2cd190efc3c4");
    }
  }

  @Test
  public void shouldRevertRejectedDeletedBranch() throws IOException, GitAPIException {
    callMirrorCommand();

    try (Git existingClone = Git.open(repositoryDirectory)) {
      existingClone.branchDelete().setBranchNames("test-branch").setForce(true).call();
    }

    MirrorCommandResult result = callUpdate(REJECT_ALL);

    assertThat(result.isSuccess()).isTrue();
    assertThat(result.getLog()).containsExactly(
      "Branches:",
      "- 3f76a12f0..000000000 test-branch (rejected due to filter)",
      "Tags:"
    );

    try (Git updatedMirror = Git.open(clone)) {
      Optional<Ref> rejectedBranch = findBranch(updatedMirror, "test-branch");
      assertThat(rejectedBranch).get().extracting("objectId.name").hasToString("3f76a12f08a6ba0dc988c68b7f0b2cd190efc3c4");
    }
  }

  @Test
  public void shouldRevertRejectedNewTag() throws IOException, GitAPIException {
    callMirrorCommand();

    try (Git existingClone = Git.open(repositoryDirectory)) {
      RevObject revObject = getRevObject(existingClone);
      existingClone.tag().setName("added-tag").setAnnotated(false).setObjectId(revObject).call();
    }

    MirrorCommandResult result = callUpdate(REJECT_ALL);

    assertThat(result.isSuccess()).isTrue();
    assertThat(result.getLog()).containsExactly(
      "Branches:",
      "Tags:",
      "- 000000000..9e93d8631 added-tag (rejected due to filter)"
    );

    try (Git updatedMirror = Git.open(clone)) {
      Optional<Ref> rejectedTag = findTag(updatedMirror, "added-tag");
      assertThat(rejectedTag).isNotPresent();
    }
  }

  @Test
  public void shouldRevertRejectedChangedTag() throws IOException, GitAPIException {
    callMirrorCommand();

    try (Git existingClone = Git.open(repositoryDirectory)) {
      RevObject revObject = getRevObject(existingClone);
      existingClone.tag().setName("test-tag").setObjectId(revObject).setForceUpdate(true).setAnnotated(false).call();
    }

    MirrorCommandResult result = callUpdate(REJECT_ALL);

    assertThat(result.isSuccess()).isTrue();
    assertThat(result.getLog()).containsExactly(
      "Branches:",
      "Tags:",
      "- 86a6645ec..9e93d8631 test-tag (rejected due to filter)"
    );

    try (Git updatedMirror = Git.open(clone)) {
      Optional<Ref> rejectedTag = findTag(updatedMirror, "test-tag");
      assertThat(rejectedTag.get().getObjectId().getName()).isEqualTo("86a6645eceefe8b9a247db5eb16e3d89a7e6e6d1");
    }
  }

  @Test
  public void shouldRevertRejectedDeletedTag() throws IOException, GitAPIException {
    callMirrorCommand();

    try (Git existingClone = Git.open(repositoryDirectory)) {
      existingClone.tagDelete().setTags("test-tag").call();
    }

    MirrorCommandResult result = callUpdate(r -> r.setFilter(new DenyAllMirrorFilter()));

    assertThat(result.isSuccess()).isTrue();
    assertThat(result.getLog()).containsExactly(
      "Branches:",
      "Tags:",
      "- 86a6645ec..000000000 test-tag (rejected due to filter)"
    );

    try (Git updatedMirror = Git.open(clone)) {
      Optional<Ref> rejectedTag = findTag(updatedMirror, "test-tag");
      assertThat(rejectedTag.get().getObjectId().getName()).isEqualTo("86a6645eceefe8b9a247db5eb16e3d89a7e6e6d1");
    }
  }

  @Test
  public void shouldMarkForcedBranchUpdate() throws IOException, GitAPIException {
    callMirrorCommand();

    try (Git existingClone = Git.open(repositoryDirectory)) {
      existingClone.branchCreate().setStartPoint("9e93d8631675a89615fac56b09209686146ff3c0").setName("test-branch").setForce(true).call();
    }

    InvocationCheck filterInvokedCheck = mock(InvocationCheck.class);

    callUpdate(r -> r.setFilter(new MirrorFilter() {
      @Override
      public Filter getFilter(FilterContext context) {
        filterInvokedCheck.invoked();
        context.getBranchUpdates().forEach(branchUpdate -> {
          assertThat(branchUpdate.getBranchName()).isEqualTo("test-branch");
          assertThat(branchUpdate.isForcedUpdate()).isTrue();
        });
        return MirrorFilter.super.getFilter(context);
      }
    }));

    verify(filterInvokedCheck).invoked();
  }

  @Test
  public void shouldNotMarkFastForwardBranchUpdateAsForced() throws IOException, GitAPIException {
    callMirrorCommand();

    try (Git existingClone = Git.open(repositoryDirectory)) {
      existingClone.branchCreate().setStartPoint("d81ad6c63d7e2162308d69637b339dedd1d9201c").setName("master").setForce(true).call();
    }

    InvocationCheck filterInvokedCheck = mock(InvocationCheck.class);

    callUpdate(r -> r.setFilter(new MirrorFilter() {
      @Override
      public Filter getFilter(FilterContext context) {
        filterInvokedCheck.invoked();
        context.getBranchUpdates().forEach(branchUpdate -> {
          assertThat(branchUpdate.getBranchName()).isEqualTo("master");
          assertThat(branchUpdate.isForcedUpdate()).isFalse();
        });
        return MirrorFilter.super.getFilter(context);
      }
    }));

    verify(filterInvokedCheck).invoked();
  }

  private RevObject getRevObject(Git existingClone) throws IOException {
    RevWalk walk = new RevWalk(existingClone.getRepository());
    ObjectId id = existingClone.getRepository().resolve("9e93d8631675a89615fac56b09209686146ff3c0");
    return walk.parseAny(id);
  }

  private MirrorCommandResult callUpdate(Consumer<MirrorCommandRequest> requestModifier) {
    MirrorCommandRequest request = new MirrorCommandRequest();
    request.setSourceUrl(repositoryDirectory.getAbsolutePath());
    requestModifier.accept(request);
    return command.update(request);
  }

  private Optional<Ref> findBranch(Git git, String branchName) throws GitAPIException {
    return git.branchList().call().stream().filter(ref -> ref.getName().equals("refs/heads/" + branchName)).findFirst();
  }

  private Optional<Ref> findTag(Git git, String tagName) throws GitAPIException {
    return git.tagList().call().stream().filter(ref -> ref.getName().equals("refs/tags/" + tagName)).findFirst();
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
        .contains("- 000000000..fcd0ef183 master (new)")
        .contains("- 000000000..3f76a12f0 test-branch (new)")
        .contains("Tags:")
        .contains("- 000000000..86a6645ec test-tag (new)");
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

  private MirrorCommandResult callMirrorCommand() {
    return callMirrorCommand(emptyContext, repositoryDirectory.getAbsolutePath(), c -> {
    });
  }

  private MirrorCommandResult callMirrorCommand(GitContext context, String source, Consumer<MirrorCommandRequest> requestConsumer) {
//    GitMirrorCommand command = new GitMirrorCommand(context, postReceiveRepositoryHookEventFactory, mirrorHttpConnectionProvider, gitChangesetConverterFactory, gitTagConverter);
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

  private static class DenyAllMirrorFilter implements MirrorFilter {
    @Override
    public Filter getFilter(FilterContext context) {
      return new Filter() {
        @Override
        public boolean acceptBranch(BranchUpdate branch) {
          return false;
        }

        @Override
        public boolean acceptTag(TagUpdate tag) {
          return false;
        }
      };
    }
  }

  private interface InvocationCheck {
    void invoked();
  }
}