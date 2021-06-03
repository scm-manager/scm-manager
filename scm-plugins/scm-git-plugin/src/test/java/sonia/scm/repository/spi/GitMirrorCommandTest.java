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
import sonia.scm.repository.GitChangesetConverterFactory;
import sonia.scm.repository.GitConfig;
import sonia.scm.repository.Tag;
import sonia.scm.repository.api.MirrorCommandResult;
import sonia.scm.repository.api.MirrorFilter;
import sonia.scm.repository.api.SimpleUsernamePasswordCredential;
import sonia.scm.security.GPG;
import sonia.scm.store.InMemoryConfigurationStoreFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static sonia.scm.repository.api.MirrorCommandResult.ResultType.FAILED;
import static sonia.scm.repository.api.MirrorCommandResult.ResultType.OK;
import static sonia.scm.repository.api.MirrorCommandResult.ResultType.REJECTED_UPDATES;

public class GitMirrorCommandTest extends AbstractGitCommandTestBase {

  public static final Consumer<MirrorCommandRequest> ACCEPT_ALL = r -> {
  };
  public static final Consumer<MirrorCommandRequest> REJECT_ALL = r -> r.setFilter(new DenyAllMirrorFilter());
  private final PostReceiveRepositoryHookEventFactory postReceiveRepositoryHookEventFactory = mock(PostReceiveRepositoryHookEventFactory.class);
  private final MirrorHttpConnectionProvider mirrorHttpConnectionProvider = mock(MirrorHttpConnectionProvider.class);
  private final GPG gpg = mock(GPG.class);
  private final GitChangesetConverterFactory gitChangesetConverterFactory = new GitChangesetConverterFactory(gpg);
  private final GitTagConverter gitTagConverter = new GitTagConverter(gpg);

  private File clone;
  private GitMirrorCommand command;

  @Before
  public void bendContextToNewRepository() throws IOException, GitAPIException {
    clone = tempFolder.newFolder();
    Git.init().setBare(true).setDirectory(clone).call();

    GitContext emptyContext = createMirrorContext(clone);
    command = new GitMirrorCommand(emptyContext, postReceiveRepositoryHookEventFactory, mirrorHttpConnectionProvider, gitChangesetConverterFactory, gitTagConverter);
  }

  @Test
  public void shouldCreateInitialMirror() throws IOException, GitAPIException {
    MirrorCommandResult result = callMirrorCommand();

    assertThat(result.getResult()).isEqualTo(OK);
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

    assertThat(result.getResult()).isEqualTo(OK);
    assertThat(result.getLog()).containsExactly(
      "Branches:",
      "- 000000000..fcd0ef183 added-branch (new)"
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

    assertThat(result.getResult()).isEqualTo(OK);
    assertThat(result.getLog()).containsExactly(
      "Branches:",
      "- 3f76a12f0..9e93d8631 test-branch (forced)"
    );

    try (Git updatedMirror = Git.open(clone)) {
      Optional<Ref> updatedBranch = findBranch(updatedMirror, "test-branch");
      assertThat(updatedBranch).hasValueSatisfying(ref -> assertThat(ref.getObjectId().getName()).isEqualTo("9e93d8631675a89615fac56b09209686146ff3c0"));
    }
  }

  @Test
  public void shouldUpdateMirrorWithDeletedBranch() throws IOException, GitAPIException {
    callMirrorCommand();

    try (Git existingClone = Git.open(repositoryDirectory)) {
      existingClone.branchDelete().setBranchNames("test-branch").setForce(true).call();
    }

    MirrorCommandResult result = callUpdate(ACCEPT_ALL);

    assertThat(result.getResult()).isEqualTo(OK);
    assertThat(result.getLog()).containsExactly(
      "Branches:",
      "- 3f76a12f0..000000000 test-branch (deleted)"
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
      RevObject revObject = getRevObject(existingClone, "9e93d8631675a89615fac56b09209686146ff3c0");
      existingClone.tag().setName("added-tag").setAnnotated(false).setObjectId(revObject).call();
    }

    MirrorCommandResult result = callUpdate(ACCEPT_ALL);

    assertThat(result.getResult()).isEqualTo(OK);
    assertThat(result.getLog()).containsExactly(
      "Tags:",
      "- 000000000..9e93d8631 added-tag (new)"
    );

    try (Git updatedMirror = Git.open(clone)) {
      Optional<Ref> addedTag = findTag(updatedMirror, "added-tag");
      assertThat(addedTag).hasValueSatisfying(ref -> assertThat(ref.getObjectId().getName()).isEqualTo("9e93d8631675a89615fac56b09209686146ff3c0"));
    }

    // event should be thrown two times, once for the initial clone, and once for the update
    verify(postReceiveRepositoryHookEventFactory, times(2)).fireForFetch(any(), any());
  }

  @Test
  public void shouldUpdateMirrorWithChangedTag() throws IOException, GitAPIException {
    callMirrorCommand();

    try (Git existingClone = Git.open(repositoryDirectory)) {
      RevObject revObject = getRevObject(existingClone, "9e93d8631675a89615fac56b09209686146ff3c0");
      existingClone.tag().setName("test-tag").setObjectId(revObject).setForceUpdate(true).setAnnotated(false).call();
    }

    MirrorCommandResult result = callUpdate(ACCEPT_ALL);

    assertThat(result.getResult()).isEqualTo(OK);
    assertThat(result.getLog()).containsExactly(
      "Tags:",
      "- 86a6645ec..9e93d8631 test-tag (forced)"
    );

    try (Git updatedMirror = Git.open(clone)) {
      Optional<Ref> updatedTag = findTag(updatedMirror, "test-tag");
      assertThat(updatedTag).hasValueSatisfying(ref -> assertThat(ref.getObjectId().getName()).isEqualTo("9e93d8631675a89615fac56b09209686146ff3c0"));
    }
  }

  @Test
  public void shouldUpdateMirrorWithDeletedTag() throws IOException, GitAPIException {
    callMirrorCommand();

    try (Git existingClone = Git.open(repositoryDirectory)) {
      existingClone.tagDelete().setTags("test-tag").call();
    }

    MirrorCommandResult result = callUpdate(ACCEPT_ALL);

    assertThat(result.getResult()).isEqualTo(OK);
    assertThat(result.getLog()).containsExactly(
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

    assertThat(result.getResult()).isEqualTo(REJECTED_UPDATES);
    assertThat(result.getLog()).containsExactly(
      "Branches:",
      "- 000000000..fcd0ef183 added-branch (rejected due to filter)"
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

    assertThat(result.getResult()).isEqualTo(REJECTED_UPDATES);
    assertThat(result.getLog()).containsExactly(
      "Branches:",
      "- 3f76a12f0..9e93d8631 test-branch (rejected due to filter)"
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

    assertThat(result.getResult()).isEqualTo(REJECTED_UPDATES);
    assertThat(result.getLog()).containsExactly(
      "Branches:",
      "- 3f76a12f0..000000000 test-branch (rejected due to filter)"
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
      RevObject revObject = getRevObject(existingClone, "9e93d8631675a89615fac56b09209686146ff3c0");
      existingClone.tag().setName("added-tag").setAnnotated(false).setObjectId(revObject).call();
    }

    MirrorCommandResult result = callUpdate(REJECT_ALL);

    assertThat(result.getResult()).isEqualTo(REJECTED_UPDATES);
    assertThat(result.getLog()).containsExactly(
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
      RevObject revObject = getRevObject(existingClone, "9e93d8631675a89615fac56b09209686146ff3c0");
      existingClone.tag().setName("test-tag").setObjectId(revObject).setForceUpdate(true).setAnnotated(false).call();
    }

    MirrorCommandResult result = callUpdate(REJECT_ALL);

    assertThat(result.getResult()).isEqualTo(REJECTED_UPDATES);
    assertThat(result.getLog()).containsExactly(
      "Tags:",
      "- 86a6645ec..9e93d8631 test-tag (rejected due to filter)"
    );

    try (Git updatedMirror = Git.open(clone)) {
      Optional<Ref> rejectedTag = findTag(updatedMirror, "test-tag");
      assertThat(rejectedTag).hasValueSatisfying(ref -> assertThat(ref.getObjectId().getName()).isEqualTo("86a6645eceefe8b9a247db5eb16e3d89a7e6e6d1"));
    }
  }

  @Test
  public void shouldRevertRejectedDeletedTag() throws IOException, GitAPIException {
    callMirrorCommand();

    try (Git existingClone = Git.open(repositoryDirectory)) {
      existingClone.tagDelete().setTags("test-tag").call();
    }

    MirrorCommandResult result = callUpdate(REJECT_ALL);

    assertThat(result.getResult()).isEqualTo(REJECTED_UPDATES);
    assertThat(result.getLog()).containsExactly(
      "Tags:",
      "- 86a6645ec..000000000 test-tag (rejected due to filter)"
    );

    try (Git updatedMirror = Git.open(clone)) {
      Optional<Ref> rejectedTag = findTag(updatedMirror, "test-tag");
      assertThat(rejectedTag).hasValueSatisfying(ref -> assertThat(ref.getObjectId().getName()).isEqualTo("86a6645eceefe8b9a247db5eb16e3d89a7e6e6d1"));
    }
  }

  @Test
  public void shouldRejectWithCustomMessage() throws IOException, GitAPIException {
    callMirrorCommand();

    try (Git existingClone = Git.open(repositoryDirectory)) {
      existingClone.tagDelete().setTags("test-tag").call();
    }

    MirrorCommandResult result = callUpdate(r -> r.setFilter(new DenyAllWithReasonMirrorFilter("thou shalt not pass")));

    assertThat(result.getResult()).isEqualTo(REJECTED_UPDATES);
    assertThat(result.getLog()).containsExactly(
      "Tags:",
      "- 86a6645ec..000000000 test-tag (thou shalt not pass)"
    );
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

  private RevObject getRevObject(Git existingClone, String revision) throws IOException {
    RevWalk walk = new RevWalk(existingClone.getRepository());
    ObjectId id = existingClone.getRepository().resolve(revision);
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
          simpleHttpServer.getUri().toASCIIString(),
          createCredential(AppServer.username, AppServer.password));

      assertThat(result.getResult()).isEqualTo(OK);
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
          simpleHttpServer.getUri().toASCIIString(),
          createCredential("wrong", "credentials"));

      assertThat(result.getResult()).isEqualTo(FAILED);

      verify(postReceiveRepositoryHookEventFactory, never()).fireForFetch(any(), any());
    } finally {
      simpleHttpServer.stop();
    }
  }

  @Test
  public void shouldCreateUpdateObjectForTags() throws IOException, GitAPIException {
    try (Git updatedSource = Git.open(repositoryDirectory)) {
      RevObject revObject = getRevObject(updatedSource, "9e93d8631675a89615fac56b09209686146ff3c0");
      updatedSource.tag().setAnnotated(true).setName("42").setMessage("annotated tag").setObjectId(revObject).call();
    }

    List<Tag> collectedTags = new ArrayList<>();

    MirrorCommandRequest request = new MirrorCommandRequest();
    request.setSourceUrl(repositoryDirectory.getAbsolutePath());
    request.setFilter(new MirrorFilter() {
      @Override
      public Filter getFilter(FilterContext context) {
        return new Filter() {
          @Override
          public Result acceptTag(TagUpdate tagUpdate) {
            collectedTags.add(tagUpdate.getTag());
            return Result.accept();
          }
        };
      }
    });

    command.mirror(request);
    assertThat(collectedTags)
      .anySatisfy(c -> {
        assertThat(c.getName()).isEqualTo("42");
        assertThat(c.getRevision()).isEqualTo("9e93d8631675a89615fac56b09209686146ff3c0");
      })
      .anySatisfy(c -> {
        assertThat(c.getName()).isEqualTo("test-tag");
        assertThat(c.getRevision()).isEqualTo("86a6645eceefe8b9a247db5eb16e3d89a7e6e6d1");
      });
  }

  private MirrorCommandResult callMirrorCommand() {
    return callMirrorCommand(repositoryDirectory.getAbsolutePath(), c -> {
    });
  }

  private MirrorCommandResult callMirrorCommand(String source, Consumer<MirrorCommandRequest> requestConsumer) {
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
        public Result acceptBranch(BranchUpdate branch) {
          return Result.reject();
        }

        @Override
        public Result acceptTag(TagUpdate tag) {
          return Result.reject();
        }
      };
    }
  }

  private static class DenyAllWithReasonMirrorFilter implements MirrorFilter {

    private final String reason;

    private DenyAllWithReasonMirrorFilter(String reason) {
      this.reason = reason;
    }

    @Override
    public Filter getFilter(FilterContext context) {
      return new Filter() {
        @Override
        public Result acceptBranch(BranchUpdate branch) {
          return Result.reject(reason);
        }

        @Override
        public Result acceptTag(TagUpdate tag) {
          return Result.reject(reason);
        }
      };
    }
  }

  private interface InvocationCheck {
    void invoked();
  }
}
