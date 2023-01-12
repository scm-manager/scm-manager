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

import com.github.sdorra.shiro.ShiroRule;
import com.github.sdorra.shiro.SubjectAware;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.apache.shiro.subject.SimplePrincipalCollection;
import org.apache.shiro.subject.Subject;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.GpgSigner;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import sonia.scm.NoChangesMadeException;
import sonia.scm.NotFoundException;
import sonia.scm.repository.Added;
import sonia.scm.repository.GitTestHelper;
import sonia.scm.repository.GitWorkingCopyFactory;
import sonia.scm.repository.Person;
import sonia.scm.repository.api.MergeCommandResult;
import sonia.scm.repository.api.MergeStrategy;
import sonia.scm.repository.work.NoneCachingWorkingCopyPool;
import sonia.scm.repository.work.WorkdirProvider;
import sonia.scm.user.User;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;

@SubjectAware(configuration = "classpath:sonia/scm/configuration/shiro.ini", username = "admin", password = "secret")
public class GitMergeCommandTest extends AbstractGitCommandTestBase {

  private static final String REALM = "AdminRealm";

  @Rule
  public ShiroRule shiro = new ShiroRule();
  @Rule
  public BindTransportProtocolRule transportProtocolRule = new BindTransportProtocolRule();

  @BeforeClass
  public static void setSigner() {
    GpgSigner.setDefault(new GitTestHelper.SimpleGpgSigner());
  }

  @Test
  public void shouldDetectMergeableBranches() {
    GitMergeCommand command = createCommand();
    MergeCommandRequest request = new MergeCommandRequest();
    request.setBranchToMerge("mergeable");
    request.setTargetBranch("master");

    boolean mergeable = command.dryRun(request).isMergeable();

    assertThat(mergeable).isTrue();
  }

  @Test
  public void shouldDetectNotMergeableBranches() {
    GitMergeCommand command = createCommand();
    MergeCommandRequest request = new MergeCommandRequest();
    request.setBranchToMerge("test-branch");
    request.setTargetBranch("master");

    boolean mergeable = command.dryRun(request).isMergeable();

    assertThat(mergeable).isFalse();
  }

  @Test
  public void shouldMergeMergeableBranches() throws IOException, GitAPIException {
    GitMergeCommand command = createCommand();
    MergeCommandRequest request = new MergeCommandRequest();
    request.setTargetBranch("master");
    request.setBranchToMerge("mergeable");
    request.setMergeStrategy(MergeStrategy.MERGE_COMMIT);
    request.setAuthor(new Person("Dirk Gently", "dirk@holistic.det"));

    MergeCommandResult mergeCommandResult = command.merge(request);

    assertThat(mergeCommandResult.isSuccess()).isTrue();
    assertThat(mergeCommandResult.getRevisionToMerge()).isEqualTo("91b99de908fcd04772798a31c308a64aea1a5523");
    assertThat(mergeCommandResult.getTargetRevision()).isEqualTo("fcd0ef1831e4002ac43ea539f4094334c79ea9ec");

    Repository repository = createContext().open();
    Iterable<RevCommit> commits = new Git(repository).log().add(repository.resolve("master")).setMaxCount(1).call();
    RevCommit mergeCommit = commits.iterator().next();
    PersonIdent mergeAuthor = mergeCommit.getAuthorIdent();
    String message = mergeCommit.getFullMessage();
    assertThat(mergeAuthor.getName()).isEqualTo("Dirk Gently");
    assertThat(mergeAuthor.getEmailAddress()).isEqualTo("dirk@holistic.det");
    assertThat(message).contains("master", "mergeable");
    // We expect the merge result of file b.txt here by looking up the sha hash of its content.
    // If the file is missing (aka not merged correctly) this will throw a MissingObjectException:
    byte[] contentOfFileB = repository.open(repository.resolve("9513e9c76e73f3e562fd8e4c909d0607113c77c6")).getBytes();
    assertThat(new String(contentOfFileB)).isEqualTo("b\ncontent from branch\n");
  }

  @Test
  public void shouldAllowEmptyMergeCommit() throws IOException, GitAPIException {
    GitMergeCommand command = createCommand();
    MergeCommandRequest request = new MergeCommandRequest();
    request.setTargetBranch("master");
    request.setBranchToMerge("empty_merge");
    request.setMergeStrategy(MergeStrategy.MERGE_COMMIT);
    request.setAuthor(new Person("Dirk Gently", "dirk@holistic.det"));

    MergeCommandResult mergeCommandResult = command.merge(request);

    assertThat(mergeCommandResult.isSuccess()).isTrue();

    Repository repository = createContext().open();
    Iterable<RevCommit> commits = new Git(repository).log().add(repository.resolve("master")).setMaxCount(1).call();
    RevCommit mergeCommit = commits.iterator().next();
    assertThat(mergeCommit.getParentCount()).isEqualTo(2);
    assertThat(mergeCommit.getParent(0).name()).isEqualTo("fcd0ef1831e4002ac43ea539f4094334c79ea9ec");
    assertThat(mergeCommit.getParent(1).name()).isEqualTo("d81ad6c63d7e2162308d69637b339dedd1d9201c");
  }

  @Test(expected = NoChangesMadeException.class)
  public void shouldNotMergeTwice() throws IOException, GitAPIException {
    GitMergeCommand command = createCommand();
    MergeCommandRequest request = new MergeCommandRequest();
    request.setTargetBranch("master");
    request.setBranchToMerge("mergeable");
    request.setAuthor(new Person("Dirk Gently", "dirk@holistic.det"));
    request.setMergeStrategy(MergeStrategy.MERGE_COMMIT);

    MergeCommandResult mergeCommandResult = command.merge(request);

    assertThat(mergeCommandResult.isSuccess()).isTrue();

    Repository repository = createContext().open();
    new Git(repository).log().add(repository.resolve("master")).setMaxCount(1).call().iterator().next().getId();

    command.merge(request);
  }

  @Test
  public void shouldUseConfiguredCommitMessageTemplate() throws IOException, GitAPIException {
    GitMergeCommand command = createCommand();
    MergeCommandRequest request = new MergeCommandRequest();
    request.setTargetBranch("master");
    request.setBranchToMerge("mergeable");
    request.setMergeStrategy(MergeStrategy.MERGE_COMMIT);
    request.setAuthor(new Person("Dirk Gently", "dirk@holistic.det"));
    request.setMessageTemplate("simple");

    MergeCommandResult mergeCommandResult = command.merge(request);

    assertThat(mergeCommandResult.isSuccess()).isTrue();

    Repository repository = createContext().open();
    Iterable<RevCommit> commits = new Git(repository).log().add(repository.resolve("master")).setMaxCount(1).call();
    RevCommit mergeCommit = commits.iterator().next();
    String message = mergeCommit.getFullMessage();
    assertThat(message).isEqualTo("simple");
  }

  @Test
  public void shouldNotMergeConflictingBranches() {
    GitMergeCommand command = createCommand();
    MergeCommandRequest request = new MergeCommandRequest();
    request.setBranchToMerge("test-branch");
    request.setTargetBranch("master");
    request.setMergeStrategy(MergeStrategy.MERGE_COMMIT);

    MergeCommandResult mergeCommandResult = command.merge(request);

    assertThat(mergeCommandResult.isSuccess()).isFalse();
    assertThat(mergeCommandResult.getFilesWithConflict()).containsExactly("a.txt");
  }

  @Test
  public void shouldHandleUnexpectedMergeResults() {
    GitMergeCommand command = createCommand(git -> {
      try {
        FileWriter fw = new FileWriter(new File(git.getRepository().getWorkTree(), "b.txt"), true);
        BufferedWriter bw = new BufferedWriter(fw);
        bw.write("change");
        bw.newLine();
        bw.close();
      } catch (IOException e) {
        e.printStackTrace();
      }
    });
    MergeCommandRequest request = new MergeCommandRequest();
    request.setBranchToMerge("mergeable");
    request.setTargetBranch("master");
    request.setMergeStrategy(MergeStrategy.MERGE_COMMIT);
    request.setAuthor(new Person("Dirk Gently", "dirk@holistic.det"));
    request.setMessageTemplate("simple");

    Assertions.assertThrows(UnexpectedMergeResultException.class, () -> command.merge(request));
  }

  @Test
  public void shouldTakeAuthorFromSubjectIfNotSet() throws IOException, GitAPIException {
    SimplePrincipalCollection principals = new SimplePrincipalCollection();
    principals.add("admin", REALM);
    principals.add(new User("dirk", "Dirk Gently", "dirk@holistic.det"), REALM);
    shiro.setSubject(
      new Subject.Builder()
        .principals(principals)
        .authenticated(true)
        .buildSubject());
    GitMergeCommand command = createCommand();
    MergeCommandRequest request = new MergeCommandRequest();
    request.setTargetBranch("master");
    request.setBranchToMerge("mergeable");
    request.setMergeStrategy(MergeStrategy.MERGE_COMMIT);

    MergeCommandResult mergeCommandResult = command.merge(request);

    assertThat(mergeCommandResult.isSuccess()).isTrue();

    Repository repository = createContext().open();
    Iterable<RevCommit> mergeCommit = new Git(repository).log().add(repository.resolve("master")).setMaxCount(1).call();
    PersonIdent mergeAuthor = mergeCommit.iterator().next().getAuthorIdent();
    assertThat(mergeAuthor.getName()).isEqualTo("Dirk Gently");
    assertThat(mergeAuthor.getEmailAddress()).isEqualTo("dirk@holistic.det");
  }

  @Test
  public void shouldMergeIntoNotDefaultBranch() throws IOException, GitAPIException {
    GitMergeCommand command = createCommand();
    MergeCommandRequest request = new MergeCommandRequest();
    request.setAuthor(new Person("Dirk Gently", "dirk@holistic.det"));
    request.setTargetBranch("mergeable");
    request.setBranchToMerge("master");
    request.setMergeStrategy(MergeStrategy.MERGE_COMMIT);

    MergeCommandResult mergeCommandResult = command.merge(request);

    Repository repository = createContext().open();
    assertThat(mergeCommandResult.isSuccess()).isTrue();

    Iterable<RevCommit> commits = new Git(repository).log().add(repository.resolve("mergeable")).setMaxCount(1).call();
    RevCommit mergeCommit = commits.iterator().next();
    PersonIdent mergeAuthor = mergeCommit.getAuthorIdent();
    String message = mergeCommit.getFullMessage();
    assertThat(mergeAuthor.getName()).isEqualTo("Dirk Gently");
    assertThat(mergeAuthor.getEmailAddress()).isEqualTo("dirk@holistic.det");
    assertThat(message).contains("master", "mergeable");
    // We expect the merge result of file b.txt here by looking up the sha hash of its content.
    // If the file is missing (aka not merged correctly) this will throw a MissingObjectException:
    byte[] contentOfFileB = repository.open(repository.resolve("9513e9c76e73f3e562fd8e4c909d0607113c77c6")).getBytes();
    assertThat(new String(contentOfFileB)).isEqualTo("b\ncontent from branch\n");
  }

  @Test
  public void shouldSquashCommitsIfSquashIsEnabled() throws IOException, GitAPIException {
    GitMergeCommand command = createCommand();
    MergeCommandRequest request = new MergeCommandRequest();
    request.setAuthor(new Person("Dirk Gently", "dirk@holistic.det"));
    request.setBranchToMerge("squash");
    request.setTargetBranch("master");
    request.setMessageTemplate("this is a squash");
    request.setMergeStrategy(MergeStrategy.SQUASH);

    MergeCommandResult mergeCommandResult = command.merge(request);

    Repository repository = createContext().open();
    assertThat(mergeCommandResult.isSuccess()).isTrue();
    assertThat(mergeCommandResult.getRevisionToMerge()).isEqualTo("35597e9e98fe53167266583848bfef985c2adb27");
    assertThat(mergeCommandResult.getTargetRevision()).isEqualTo("fcd0ef1831e4002ac43ea539f4094334c79ea9ec");

    Iterable<RevCommit> commits = new Git(repository).log().add(repository.resolve("master")).setMaxCount(1).call();
    RevCommit mergeCommit = commits.iterator().next();
    PersonIdent mergeAuthor = mergeCommit.getAuthorIdent();
    String message = mergeCommit.getFullMessage();
    assertThat(mergeAuthor.getName()).isEqualTo("Dirk Gently");
    assertThat(message).isEqualTo("this is a squash");
  }

  @Test
  public void shouldSquashThreeCommitsIntoOne() throws IOException, GitAPIException {
    GitMergeCommand command = createCommand();
    MergeCommandRequest request = new MergeCommandRequest();
    request.setAuthor(new Person("Dirk Gently", "dirk@holistic.det"));
    request.setBranchToMerge("squash");
    request.setTargetBranch("master");
    request.setMessageTemplate("squash three commits");
    request.setMergeStrategy(MergeStrategy.SQUASH);
    Repository gitRepository = createContext().open();
    MergeCommandResult mergeCommandResult = command.merge(request);

    assertThat(mergeCommandResult.isSuccess()).isTrue();

    Iterable<RevCommit> commits = new Git(gitRepository).log().add(gitRepository.resolve("master")).setMaxCount(1).call();
    RevCommit mergeCommit = commits.iterator().next();
    PersonIdent mergeAuthor = mergeCommit.getAuthorIdent();
    String message = mergeCommit.getFullMessage();
    assertThat(mergeAuthor.getName()).isEqualTo("Dirk Gently");
    assertThat(message).isEqualTo("squash three commits");

    GitModificationsCommand modificationsCommand = new GitModificationsCommand(createContext());
    List<Added> changes = modificationsCommand.getModifications("master").getAdded();
    assertThat(changes.size()).isEqualTo(3);
  }


  @Test
  public void shouldMergeWithFastForward() throws IOException, GitAPIException {
    Repository repository = createContext().open();

    ObjectId featureBranchHead = new Git(repository).log().add(repository.resolve("squash")).setMaxCount(1).call().iterator().next().getId();

    GitMergeCommand command = createCommand();
    MergeCommandRequest request = new MergeCommandRequest();
    request.setBranchToMerge("squash");
    request.setTargetBranch("master");
    request.setMergeStrategy(MergeStrategy.FAST_FORWARD_IF_POSSIBLE);
    request.setAuthor(new Person("Dirk Gently", "dirk@holistic.det"));

    MergeCommandResult mergeCommandResult = command.merge(request);
    assertThat(mergeCommandResult.getNewHeadRevision()).isEqualTo("35597e9e98fe53167266583848bfef985c2adb27");
    assertThat(mergeCommandResult.getRevisionToMerge()).isEqualTo("35597e9e98fe53167266583848bfef985c2adb27");
    assertThat(mergeCommandResult.getTargetRevision()).isEqualTo("fcd0ef1831e4002ac43ea539f4094334c79ea9ec");

    assertThat(mergeCommandResult.isSuccess()).isTrue();

    Iterable<RevCommit> commits = new Git(repository).log().add(repository.resolve("master")).setMaxCount(1).call();
    RevCommit mergeCommit = commits.iterator().next();
    assertThat(mergeCommit.getParentCount()).isEqualTo(1);
    PersonIdent mergeAuthor = mergeCommit.getAuthorIdent();
    assertThat(mergeAuthor.getName()).isEqualTo("Philip J Fry");
    assertThat(mergeCommit.getId()).isEqualTo(featureBranchHead);
  }

  @Test
  public void shouldDoMergeCommitIfFastForwardIsNotPossible() throws IOException, GitAPIException {
    GitMergeCommand command = createCommand();
    MergeCommandRequest request = new MergeCommandRequest();
    request.setTargetBranch("master");
    request.setBranchToMerge("mergeable");
    request.setMergeStrategy(MergeStrategy.FAST_FORWARD_IF_POSSIBLE);
    request.setAuthor(new Person("Dirk Gently", "dirk@holistic.det"));

    MergeCommandResult mergeCommandResult = command.merge(request);

    assertThat(mergeCommandResult.isSuccess()).isTrue();

    Repository repository = createContext().open();
    Iterable<RevCommit> commits = new Git(repository).log().add(repository.resolve("master")).setMaxCount(1).call();
    RevCommit mergeCommit = commits.iterator().next();
    PersonIdent mergeAuthor = mergeCommit.getAuthorIdent();
    assertThat(mergeCommit.getParentCount()).isEqualTo(2);
    String message = mergeCommit.getFullMessage();
    assertThat(mergeAuthor.getName()).isEqualTo("Dirk Gently");
    assertThat(mergeAuthor.getEmailAddress()).isEqualTo("dirk@holistic.det");
    assertThat(message).contains("master", "mergeable");
  }

  @Test(expected = NotFoundException.class)
  public void shouldHandleNotExistingSourceBranchInMerge() {
    GitMergeCommand command = createCommand();
    MergeCommandRequest request = new MergeCommandRequest();
    request.setTargetBranch("mergeable");
    request.setBranchToMerge("not_existing");
    request.setMergeStrategy(MergeStrategy.MERGE_COMMIT);

    command.merge(request);
  }

  @Test(expected = NotFoundException.class)
  public void shouldHandleNotExistingTargetBranchInMerge() {
    GitMergeCommand command = createCommand();
    MergeCommandRequest request = new MergeCommandRequest();
    request.setMergeStrategy(MergeStrategy.MERGE_COMMIT);
    request.setTargetBranch("not_existing");
    request.setBranchToMerge("master");

    command.merge(request);
  }

  @Test(expected = NotFoundException.class)
  public void shouldHandleNotExistingSourceBranchInDryRun() {
    GitMergeCommand command = createCommand();
    MergeCommandRequest request = new MergeCommandRequest();
    request.setTargetBranch("mergeable");
    request.setBranchToMerge("not_existing");

    command.dryRun(request);
  }

  @Test(expected = NotFoundException.class)
  public void shouldHandleNotExistingTargetBranchInDryRun() {
    GitMergeCommand command = createCommand();
    MergeCommandRequest request = new MergeCommandRequest();
    request.setTargetBranch("not_existing");
    request.setBranchToMerge("master");

    command.dryRun(request);
  }

  @Test
  public void shouldSignMergeCommit() throws IOException, GitAPIException {
    GitMergeCommand command = createCommand();
    MergeCommandRequest request = new MergeCommandRequest();
    request.setTargetBranch("master");
    request.setBranchToMerge("empty_merge");
    request.setMergeStrategy(MergeStrategy.MERGE_COMMIT);
    request.setAuthor(new Person("Dirk Gently", "dirk@holistic.det"));

    MergeCommandResult mergeCommandResult = command.merge(request);

    assertThat(mergeCommandResult.isSuccess()).isTrue();

    Repository repository = createContext().open();
    Iterable<RevCommit> commits = new Git(repository).log().add(repository.resolve("master")).setMaxCount(1).call();
    RevCommit mergeCommit = commits.iterator().next();
    assertThat(mergeCommit.getRawGpgSignature()).isNotEmpty();
    assertThat(mergeCommit.getRawGpgSignature()).isEqualTo(GitTestHelper.SimpleGpgSigner.getSignature());

  }

  @Test
  public void shouldNotSignMergeCommitIfSigningIsDisabled() throws IOException, GitAPIException {
    GitMergeCommand command = createCommand();
    MergeCommandRequest request = new MergeCommandRequest();
    request.setTargetBranch("master");
    request.setBranchToMerge("empty_merge");
    request.setMergeStrategy(MergeStrategy.MERGE_COMMIT);
    request.setAuthor(new Person("Dirk Gently", "dirk@holistic.det"));
    request.setSign(false);

    MergeCommandResult mergeCommandResult = command.merge(request);

    assertThat(mergeCommandResult.isSuccess()).isTrue();

    Repository repository = createContext().open();
    Iterable<RevCommit> commits = new Git(repository).log().add(repository.resolve("master")).setMaxCount(1).call();
    RevCommit mergeCommit = commits.iterator().next();
    assertThat(mergeCommit.getRawGpgSignature()).isNullOrEmpty();

  }

  @Test
  public void shouldAllowMergeWithRebase() throws IOException, GitAPIException {
    GitMergeCommand command = createCommand();
    MergeCommandRequest request = new MergeCommandRequest();
    request.setTargetBranch("master");
    request.setBranchToMerge("mergeable");
    request.setMergeStrategy(MergeStrategy.REBASE);
    request.setAuthor(new Person("Dirk Gently", "dirk@holistic.det"));

    MergeCommandResult mergeCommandResult = command.merge(request);

    assertThat(mergeCommandResult.isSuccess()).isTrue();

    Repository repository = createContext().open();
    Iterable<RevCommit> commits = new Git(repository).log().add(repository.resolve("master")).setMaxCount(1).call();
    RevCommit mergeCommit = commits.iterator().next();
    assertThat(mergeCommit.getParentCount()).isEqualTo(1);
    assertThat(mergeCommit.getParent(0).name()).isEqualTo("fcd0ef1831e4002ac43ea539f4094334c79ea9ec");
    assertThat(mergeCommit.getName()).isEqualTo(mergeCommandResult.getNewHeadRevision());
    assertThat(mergeCommit.getName()).doesNotStartWith("91b99de908fcd04772798a31c308a64aea1a5523");
  }

  @Test
  public void shouldRejectRebaseMergeIfBranchCannotBeRebased() throws IOException, GitAPIException {
    GitMergeCommand command = createCommand();
    MergeCommandRequest request = new MergeCommandRequest();
    request.setTargetBranch("master");
    request.setBranchToMerge("not-rebasable");
    request.setMergeStrategy(MergeStrategy.REBASE);
    request.setAuthor(new Person("Dirk Gently", "dirk@holistic.det"));

    MergeCommandResult mergeCommandResult = command.merge(request);

    assertThat(mergeCommandResult.isSuccess()).isFalse();
    Repository repository = createContext().open();
    Iterable<RevCommit> commits = new Git(repository).log().add(repository.resolve("master")).setMaxCount(1).call();
    RevCommit headCommit = commits.iterator().next();
    assertThat(headCommit.getName()).isEqualTo("fcd0ef1831e4002ac43ea539f4094334c79ea9ec");

  }

  private GitMergeCommand createCommand() {
    return createCommand(git -> {
    });
  }

  private GitMergeCommand createCommand(Consumer<Git> interceptor) {
    return new GitMergeCommand(createContext(), new SimpleGitWorkingCopyFactory(new NoneCachingWorkingCopyPool(new WorkdirProvider(repositoryLocationResolver)), new SimpleMeterRegistry())) {
      @Override
      <R, W extends GitCloneWorker<R>> R inClone(Function<Git, W> workerSupplier, GitWorkingCopyFactory workingCopyFactory, String initialBranch) {
        Function<Git, W> interceptedWorkerSupplier = git -> {
          interceptor.accept(git);
          return workerSupplier.apply(git);
        };
        return super.inClone(interceptedWorkerSupplier, workingCopyFactory, initialBranch);
      }
    };
  }
}
