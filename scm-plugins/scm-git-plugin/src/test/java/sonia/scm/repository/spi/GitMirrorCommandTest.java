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

import com.google.inject.util.Providers;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.junit.http.AppServer;
import org.eclipse.jgit.junit.http.SimpleHttpServer;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevObject;
import org.eclipse.jgit.revwalk.RevWalk;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import sonia.scm.api.v2.resources.GitRepositoryConfigStoreProvider;
import sonia.scm.config.ScmConfiguration;
import sonia.scm.net.GlobalProxyConfiguration;
import sonia.scm.net.HttpURLConnectionFactory;
import sonia.scm.repository.GitChangesetConverterFactory;
import sonia.scm.repository.GitConfig;
import sonia.scm.repository.GitHeadModifier;
import sonia.scm.repository.GitRepositoryConfig;
import sonia.scm.repository.GitUtil;
import sonia.scm.repository.api.MirrorCommandResult;
import sonia.scm.repository.api.MirrorFilter;
import sonia.scm.repository.api.SimpleUsernamePasswordCredential;
import sonia.scm.repository.work.NoneCachingWorkingCopyPool;
import sonia.scm.repository.work.SimpleWorkingCopyFactory;
import sonia.scm.repository.work.WorkdirProvider;
import sonia.scm.security.GPG;
import sonia.scm.store.BlobStore;
import sonia.scm.store.ConfigurationStore;
import sonia.scm.store.InMemoryConfigurationStoreFactory;
import sonia.scm.util.IOUtil;
import sonia.scm.web.lfs.LfsBlobStoreFactory;

import javax.net.ssl.TrustManager;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;
import static org.mockito.AdditionalMatchers.not;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static sonia.scm.repository.api.MirrorCommandResult.ResultType.FAILED;
import static sonia.scm.repository.api.MirrorCommandResult.ResultType.OK;
import static sonia.scm.repository.api.MirrorCommandResult.ResultType.REJECTED_UPDATES;
import static sonia.scm.repository.spi.GitMirrorCommand.RefType.BRANCH;

public class GitMirrorCommandTest extends AbstractGitCommandTestBase {

  @Rule
  public BindTransportProtocolRule transportProtocolRule = new BindTransportProtocolRule();

  public static final Consumer<MirrorCommandRequest> ACCEPT_ALL = r -> {
  };
  public static final Consumer<MirrorCommandRequest> REJECT_ALL = r -> r.setFilter(new DenyAllMirrorFilter());
  private final GPG gpg = mock(GPG.class);
  private final GitChangesetConverterFactory gitChangesetConverterFactory = new GitChangesetConverterFactory(gpg);
  private final GitTagConverter gitTagConverter = new GitTagConverter(gpg);

  private File clone;
  private File workdirAfterClose;
  private GitMirrorCommand command;

  private final GitHeadModifier gitHeadModifier = mock(GitHeadModifier.class);

  private final GitRepositoryConfigStoreProvider storeProvider = mock(GitRepositoryConfigStoreProvider.class);
  private final ConfigurationStore<GitRepositoryConfig> configurationStore = mock(ConfigurationStore.class);
  private final LfsBlobStoreFactory lfsBlobStoreFactory = mock(LfsBlobStoreFactory.class);
  private final BlobStore lfsBlobStore = mock(BlobStore.class);

  private final GitRepositoryConfig gitRepositoryConfig = new GitRepositoryConfig();

  @Before
  public void bendContextToNewRepository() throws IOException, GitAPIException {
    clone = tempFolder.newFolder();
    Git.init()
      .setInitialBranch("master")
      .setBare(true)
      .setDirectory(clone)
      .call();

    GitContext emptyContext = createMirrorContext(clone);
    SimpleGitWorkingCopyFactory workingCopyFactory =
      new SimpleGitWorkingCopyFactory(
        new KeepingWorkingCopyPool(new WorkdirProvider(repositoryLocationResolver)),
        new SimpleMeterRegistry()
      );

    MirrorHttpConnectionProvider mirrorHttpConnectionProvider = new MirrorHttpConnectionProvider(
      new HttpURLConnectionFactory(
        new GlobalProxyConfiguration(new ScmConfiguration()),
        Providers.of(mock(TrustManager.class))
      )
    );

    command = new GitMirrorCommand(
      emptyContext,
      mirrorHttpConnectionProvider,
      gitChangesetConverterFactory,
      gitTagConverter,
      workingCopyFactory,
      gitHeadModifier,
      storeProvider,
      lfsBlobStoreFactory);
  }

  @Before
  public void initializeStores() {
    when(storeProvider.get(repository)).thenReturn(configurationStore);
    when(configurationStore.get()).thenReturn(gitRepositoryConfig);

    when(lfsBlobStoreFactory.getLfsBlobStore(repository)).thenReturn(lfsBlobStore);
  }

  @After
  public void cleanupWorkdir() {
    if (workdirAfterClose != null) {
      IOUtil.deleteSilently(workdirAfterClose);
    }
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
  }

  @Test
  public void shouldAcceptEmptyInitialMirror() throws IOException, GitAPIException {
    MirrorCommandResult result = callMirrorCommand(repositoryDirectory.getAbsolutePath(), c -> {
      c.setFilter(new MirrorFilter() {
        @Override
        public Filter getFilter(FilterContext context) {
          return new Filter() {
            @Override
            public Result acceptBranch(BranchUpdate branch) {
              return Result.reject("nothing accepted");
            }

            @Override
            public Result acceptTag(TagUpdate tag) {
              return Result.reject("nothing accepted");
            }
          };
        }
      });
    });

    assertThat(result.getResult()).isEqualTo(REJECTED_UPDATES);
    assertThat(result.getLog()).contains("Branches:")
      .contains("- 000000000..fcd0ef183 master (nothing accepted)")
      .contains("- 000000000..3f76a12f0 test-branch (nothing accepted)")
      .contains("Tags:")
      .contains("- 000000000..86a6645ec test-tag (nothing accepted)");

    try (Git createdMirror = Git.open(clone)) {
      assertThat(createdMirror.branchList().call()).isEmpty();
      assertThat(createdMirror.tagList().call()).isEmpty();
    }
  }

  @Test
  public void shouldAcceptOnlyTagInInitialMirror() {
    assertThrows(IllegalStateException.class, () ->
      callMirrorCommand(repositoryDirectory.getAbsolutePath(), c -> {
        c.setFilter(new MirrorFilter() {
          @Override
          public Filter getFilter(FilterContext context) {
            return new Filter() {
              @Override
              public Result acceptBranch(BranchUpdate branch) {
                return Result.reject("nothing accepted");
              }
            };
          }
        });
      }));
  }

  @Test
  public void shouldFilterMasterBranchWhenFilteredOnInitialMirror() throws IOException, GitAPIException {
    MirrorCommandResult result = callMirrorCommand(repositoryDirectory.getAbsolutePath(), c -> {
      c.setFilter(new MirrorFilter() {
        @Override
        public Filter getFilter(FilterContext context) {
          return new Filter() {
            @Override
            public Result acceptBranch(BranchUpdate branch) {
              if (branch.getBranchName().equals("master")) {
                return Result.reject("master not accepted");
              } else {
                return Result.accept();
              }
            }
          };
        }
      });
    });

    assertThat(result.getResult()).isEqualTo(REJECTED_UPDATES);
    assertThat(result.getLog())
      .contains("- 000000000..fcd0ef183 master (master not accepted)");

    try (Git createdMirror = Git.open(clone)) {
      assertThat(createdMirror.branchList().call().stream().filter(r -> r.getName().contains("master")).findAny())
        .isEmpty();
    }
    try (Repository workdirRepository = GitUtil.open(workdirAfterClose)) {
      assertThat(workdirRepository.findRef(Constants.HEAD).getTarget().getName()).isNotEqualTo("refs/heads/master");
    }
    verify(gitHeadModifier)
      .ensure(eq(repository), not(eq("master")));
  }

  @Test
  public void shouldCreateEmptyLogWhenNoChangesFound() {
    callMirrorCommand();

    MirrorCommandResult result = callUpdate(ACCEPT_ALL);

    assertThat(result.getResult()).isEqualTo(OK);
    assertThat(result.getLog()).containsExactly("No updates found");
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
      "- 000000000..fcd0ef183 added-branch (rejected due to filter)",
      "No effective changes detected"
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
      "- 3f76a12f0..9e93d8631 test-branch (rejected due to filter)",
      "No effective changes detected"
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
      "- 3f76a12f0..000000000 test-branch (rejected due to filter)",
      "No effective changes detected"
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
      "- 000000000..9e93d8631 added-tag (rejected due to filter)",
      "No effective changes detected"
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
      "- 86a6645ec..9e93d8631 test-tag (rejected due to filter)",
      "No effective changes detected"
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
      "- 86a6645ec..000000000 test-tag (rejected due to filter)",
      "No effective changes detected"
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
      "- 86a6645ec..000000000 test-tag (thou shalt not pass)",
      "No effective changes detected"
    );
  }

  @Test
  public void shouldLogExceptionsFromFilter() throws IOException, GitAPIException {
    callMirrorCommand();

    try (Git existingClone = Git.open(repositoryDirectory)) {
      existingClone.tagDelete().setTags("test-tag").call();
    }

    MirrorCommandResult result = callUpdate(r -> r.setFilter(new ErroneousMirrorFilterThrowingExceptions()));

    assertThat(result.getResult()).isEqualTo(REJECTED_UPDATES);
    assertThat(result.getLog()).containsExactly(
      "! got error checking filter for update: this tag creates an exception",
      "Tags:",
      "- 86a6645ec..000000000 test-tag (exception in filter)",
      "No effective changes detected"
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
    } finally {
      simpleHttpServer.stop();
    }
  }

  @Test
  public void shouldCreateUpdateObjectForCreatedTags() throws IOException, GitAPIException {
    try (Git updatedSource = Git.open(repositoryDirectory)) {
      RevObject revObject = getRevObject(updatedSource, "9e93d8631675a89615fac56b09209686146ff3c0");
      updatedSource.tag().setAnnotated(true).setName("42").setMessage("annotated tag").setObjectId(revObject).call();
    }

    List<MirrorFilter.TagUpdate> collectedTagUpdates = callMirrorAndCollectUpdates().tagUpdates;

    assertThat(collectedTagUpdates)
      .anySatisfy(update -> {
        assertThat(update.getUpdateType()).get().isEqualTo(MirrorFilter.UpdateType.CREATE);
        assertThat(update.getTagName()).isEqualTo("42");
        assertThat(update.getNewRevision()).get().isEqualTo("9e93d8631675a89615fac56b09209686146ff3c0");
        assertThat(update.getOldRevision()).isEmpty();
        assertThat(update.getTag()).get().extracting("name").isEqualTo("42");
        assertThat(update.getTag()).get().extracting("revision").isEqualTo("9e93d8631675a89615fac56b09209686146ff3c0");
      })
      .anySatisfy(tagUpdate -> {
        assertThat(tagUpdate.getUpdateType()).get().isEqualTo(MirrorFilter.UpdateType.CREATE);
        assertThat(tagUpdate.getTagName()).isEqualTo("test-tag");
        assertThat(tagUpdate.getNewRevision()).get().isEqualTo("86a6645eceefe8b9a247db5eb16e3d89a7e6e6d1");
        assertThat(tagUpdate.getOldRevision()).isEmpty();
        assertThat(tagUpdate.getTag()).get().extracting("name").isEqualTo("test-tag");
        assertThat(tagUpdate.getTag()).get().extracting("revision").isEqualTo("86a6645eceefe8b9a247db5eb16e3d89a7e6e6d1");
      });
  }

  @Test
  public void shouldCreateUpdateObjectForDeletedTags() throws IOException, GitAPIException {
    callMirrorCommand();

    try (Git updatedSource = Git.open(repositoryDirectory)) {
      updatedSource.tagDelete().setTags("test-tag").call();
    }

    List<MirrorFilter.TagUpdate> collectedTagUPdates = callMirrorAndCollectUpdates().tagUpdates;

    assertThat(collectedTagUPdates)
      .anySatisfy(update -> {
        assertThat(update.getUpdateType()).get().isEqualTo(MirrorFilter.UpdateType.DELETE);
        assertThat(update.getTagName()).isEqualTo("test-tag");
        assertThat(update.getNewRevision()).isEmpty();
        assertThat(update.getOldRevision()).get().isEqualTo("86a6645eceefe8b9a247db5eb16e3d89a7e6e6d1");
        assertThat(update.getTag()).isEmpty();
      });
  }

  @Test
  public void shouldCreateUpdateObjectForUpdatedTags() throws IOException, GitAPIException {
    callMirrorCommand();

    try (Git updatedSource = Git.open(repositoryDirectory)) {
      RevObject revObject = getRevObject(updatedSource, "9e93d8631675a89615fac56b09209686146ff3c0");
      updatedSource.tag().setName("test-tag").setObjectId(revObject).setForceUpdate(true).call();
    }

    List<MirrorFilter.TagUpdate> collectedTagUpdates = callMirrorAndCollectUpdates().tagUpdates;

    assertThat(collectedTagUpdates)
      .anySatisfy(update -> {
        assertThat(update.getUpdateType()).get().isEqualTo(MirrorFilter.UpdateType.UPDATE);
        assertThat(update.getTagName()).isEqualTo("test-tag");
        assertThat(update.getNewRevision()).get().isEqualTo("9e93d8631675a89615fac56b09209686146ff3c0");
        assertThat(update.getOldRevision()).get().isEqualTo("86a6645eceefe8b9a247db5eb16e3d89a7e6e6d1");
        assertThat(update.getTag()).get().extracting("name").isEqualTo("test-tag");
        assertThat(update.getTag()).get().extracting("revision").isEqualTo("9e93d8631675a89615fac56b09209686146ff3c0");
      });
  }

  @Test
  public void shouldCreateUpdateObjectForNewBranch() {
    List<MirrorFilter.BranchUpdate> collectedBranchUpdates = callMirrorAndCollectUpdates().branchUpdates;

    assertThat(collectedBranchUpdates)
      .anySatisfy(update -> {
        assertThat(update.getUpdateType()).get().isEqualTo(MirrorFilter.UpdateType.CREATE);
        assertThat(update.getBranchName()).isEqualTo("test-branch");
        assertThat(update.getNewRevision()).get().isEqualTo("3f76a12f08a6ba0dc988c68b7f0b2cd190efc3c4");
        assertThat(update.getOldRevision()).isEmpty();
        assertThat(update.getChangeset()).get().extracting("id").isEqualTo("3f76a12f08a6ba0dc988c68b7f0b2cd190efc3c4");
      });
  }

  @Test
  public void shouldCreateUpdateObjectForForcedUpdatedBranch() throws IOException, GitAPIException {
    callMirrorCommand();

    try (Git existingClone = Git.open(repositoryDirectory)) {
      existingClone.branchCreate().setStartPoint("9e93d8631675a89615fac56b09209686146ff3c0").setName("test-branch").setForce(true).call();
    }

    List<MirrorFilter.BranchUpdate> collectedBranchUpdates = callMirrorAndCollectUpdates().branchUpdates;

    assertThat(collectedBranchUpdates)
      .anySatisfy(update -> {
        assertThat(update.getUpdateType()).get().isEqualTo(MirrorFilter.UpdateType.UPDATE);
        assertThat(update.isForcedUpdate()).isTrue();
        assertThat(update.getBranchName()).isEqualTo("test-branch");
        assertThat(update.getNewRevision()).get().isEqualTo("9e93d8631675a89615fac56b09209686146ff3c0");
        assertThat(update.getOldRevision()).get().isEqualTo("3f76a12f08a6ba0dc988c68b7f0b2cd190efc3c4");
        assertThat(update.getChangeset()).get().extracting("id").isEqualTo("9e93d8631675a89615fac56b09209686146ff3c0");
      });
  }

  @Test
  public void shouldCreateUpdateObjectForFastForwardUpdatedBranch() throws IOException, GitAPIException {
    callMirrorCommand();

    try (Git existingClone = Git.open(repositoryDirectory)) {
      existingClone.branchCreate().setStartPoint("a8495c0335a13e6e432df90b3727fa91943189a7").setName("master").setForce(true).call();
    }

    List<MirrorFilter.BranchUpdate> collectedBranchUpdates = callMirrorAndCollectUpdates().branchUpdates;

    assertThat(collectedBranchUpdates)
      .anySatisfy(update -> {
        assertThat(update.getUpdateType()).get().isEqualTo(MirrorFilter.UpdateType.UPDATE);
        assertThat(update.isForcedUpdate()).isFalse();
        assertThat(update.getBranchName()).isEqualTo("master");
        assertThat(update.getNewRevision()).get().isEqualTo("a8495c0335a13e6e432df90b3727fa91943189a7");
        assertThat(update.getOldRevision()).get().isEqualTo("fcd0ef1831e4002ac43ea539f4094334c79ea9ec");
        assertThat(update.getChangeset()).get().extracting("id").isEqualTo("a8495c0335a13e6e432df90b3727fa91943189a7");
      });
  }

  @Test
  public void shouldCreateUpdateObjectForDeletedBranch() throws IOException, GitAPIException {
    callMirrorCommand();

    try (Git updatedSource = Git.open(repositoryDirectory)) {
      updatedSource.branchDelete().setBranchNames("test-branch").setForce(true).call();
    }

    List<MirrorFilter.BranchUpdate> collectedBranchUpdates = callMirrorAndCollectUpdates().branchUpdates;

    assertThat(collectedBranchUpdates)
      .anySatisfy(update -> {
        assertThat(update.getBranchName()).isEqualTo("test-branch");
        assertThat(update.getNewRevision()).isEmpty();
        assertThat(update.getOldRevision()).get().isEqualTo("3f76a12f08a6ba0dc988c68b7f0b2cd190efc3c4");
        assertThat(update.getChangeset()).isEmpty();
      });
  }

  @Test
  public void shouldSelectNewHeadIfOldHeadIsDeleted() throws IOException, GitAPIException {
    callMirrorCommand();

    try (Git updatedSource = Git.open(repositoryDirectory)) {
      updatedSource.checkout().setName("test-branch").call();
      updatedSource.branchDelete().setBranchNames("master").setForce(true).call();
    }

    List<MirrorFilter.BranchUpdate> collectedBranchUpdates = callMirrorAndCollectUpdates().branchUpdates;

    assertThat(collectedBranchUpdates)
      .anySatisfy(update -> {
        assertThat(update.getBranchName()).isEqualTo("master");
        assertThat(update.getNewRevision()).isEmpty();
      });
    verify(configurationStore).set(argThat(argument -> {
      assertThat(argument.getDefaultBranch()).isNotEqualTo("master");
      return true;
    }));
  }

  public static class DefaultBranchSelectorTest {

    public static final List<String> BRANCHES = asList("master", "one", "two", "three");

    @Test
    public void shouldKeepMasterIfMirroredInFirstSync() {
      GitMirrorCommand.DefaultBranchSelector selector =
        new GitMirrorCommand.DefaultBranchSelector("master", emptyList());

      selector.accepted(BRANCH, "master");
      selector.accepted(BRANCH, "something");

      assertThat(selector.newDefaultBranch()).isEmpty();
    }

    @Test
    public void shouldKeepDefaultIfNotDeleted() {
      GitMirrorCommand.DefaultBranchSelector selector =
        new GitMirrorCommand.DefaultBranchSelector("master", BRANCHES);

      selector.accepted(BRANCH, "new");
      selector.deleted(BRANCH, "two");

      assertThat(selector.newDefaultBranch()).isEmpty();
    }

    @Test
    public void shouldChangeDefaultIfInitialOneIsDeleted() {
      GitMirrorCommand.DefaultBranchSelector selector =
        new GitMirrorCommand.DefaultBranchSelector("master", BRANCHES);

      selector.deleted(BRANCH, "master");

      assertThat(selector.newDefaultBranch()).get().isIn("one", "two", "three");
    }

    @Test
    public void shouldChangeDefaultIfInitialOneIsDeletedButNotFromDeleted() {
      GitMirrorCommand.DefaultBranchSelector selector =
        new GitMirrorCommand.DefaultBranchSelector("master", BRANCHES);

      selector.deleted(BRANCH, "master");
      selector.deleted(BRANCH, "one");
      selector.deleted(BRANCH, "three");

      assertThat(selector.newDefaultBranch()).get().isEqualTo("two");
    }

    @Test
    public void shouldChangeDefaultToRemainingBranchIfInitialOneIsDeleted() {
      GitMirrorCommand.DefaultBranchSelector selector =
        new GitMirrorCommand.DefaultBranchSelector("master", BRANCHES);

      selector.deleted(BRANCH, "master");
      selector.deleted(BRANCH, "one");
      selector.deleted(BRANCH, "three");

      assertThat(selector.newDefaultBranch()).get().isEqualTo("two");
    }

    @Test
    public void shouldFailIfAllInitialBranchesAreDeleted() {
      GitMirrorCommand.DefaultBranchSelector selector =
        new GitMirrorCommand.DefaultBranchSelector("master", BRANCHES);

      selector.deleted(BRANCH, "master");
      selector.deleted(BRANCH, "one");
      selector.deleted(BRANCH, "two");
      selector.accepted(BRANCH, "new");
      selector.deleted(BRANCH, "three");

      assertThrows(IllegalStateException.class, selector::newDefaultBranch);
    }

    @Test
    public void shouldChangeDefaultOnInitialSyncIfMasterIsRejected() {
      GitMirrorCommand.DefaultBranchSelector selector =
        new GitMirrorCommand.DefaultBranchSelector("master", emptyList());

      selector.accepted(BRANCH, "main");
      selector.deleted(BRANCH, "master");

      assertThat(selector.newDefaultBranch()).get().isEqualTo("main");
    }

    @Test
    public void shouldChangeDefaultOnInitialSyncIfMasterIsNotAvailable() {
      GitMirrorCommand.DefaultBranchSelector selector =
        new GitMirrorCommand.DefaultBranchSelector("master", emptyList());

      selector.accepted(BRANCH, "main");

      assertThat(selector.newDefaultBranch()).get().isEqualTo("main");
    }
  }

  private Updates callMirrorAndCollectUpdates() {
    Updates updates = new Updates();

    MirrorCommandRequest request = new MirrorCommandRequest();
    request.setSourceUrl(repositoryDirectory.getAbsolutePath());
    request.setFilter(new MirrorFilter() {
      @Override
      public Filter getFilter(FilterContext context) {
        return new Filter() {
          @Override
          public Result acceptTag(TagUpdate tagUpdate) {
            tagUpdate.getTag();
            updates.tagUpdates.add(tagUpdate);
            return Result.accept();
          }

          @Override
          public Result acceptBranch(BranchUpdate branchUpdate) {
            branchUpdate.getChangeset();
            updates.branchUpdates.add(branchUpdate);
            return Result.accept();
          }
        };
      }
    });

    command.mirror(request);
    return updates;
  }

  private class Updates {
    private final List<MirrorFilter.BranchUpdate> branchUpdates = new ArrayList<>();
    private final List<MirrorFilter.TagUpdate> tagUpdates = new ArrayList<>();
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

  private static class ErroneousMirrorFilterThrowingExceptions implements MirrorFilter {

    @Override
    public Filter getFilter(FilterContext context) {
      return new Filter() {
        @Override
        public Result acceptBranch(BranchUpdate branch) {
          throw new RuntimeException("this branch creates an exception");
        }

        @Override
        public Result acceptTag(TagUpdate tag) {
          throw new RuntimeException("this tag creates an exception");
        }
      };
    }
  }

  private interface InvocationCheck {
    void invoked();
  }

  private class KeepingWorkingCopyPool extends NoneCachingWorkingCopyPool {

    public KeepingWorkingCopyPool(WorkdirProvider workdirProvider) {
      super(workdirProvider);
    }

    @Override
    public void contextClosed(SimpleWorkingCopyFactory<?, ?, ?>.WorkingCopyContext workingCopyContext, File workdir) {
      workdirAfterClose = workdir;
    }
  }
}
