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

import com.github.sdorra.shiro.ShiroRule;
import com.github.sdorra.shiro.SubjectAware;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.apache.shiro.subject.SimplePrincipalCollection;
import org.apache.shiro.subject.Subject;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.GpgConfig;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.RefUpdate;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.Signers;
import org.eclipse.jgit.revwalk.RevCommit;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import sonia.scm.ConcurrentModificationException;
import sonia.scm.NoChangesMadeException;
import sonia.scm.NotFoundException;
import sonia.scm.repository.Added;
import sonia.scm.repository.GitTestHelper;
import sonia.scm.repository.GitWorkingCopyFactory;
import sonia.scm.repository.Person;
import sonia.scm.repository.RepositoryHookEvent;
import sonia.scm.repository.RepositoryManager;
import sonia.scm.repository.api.MergeCommandResult;
import sonia.scm.repository.api.MergeDryRunCommandResult;
import sonia.scm.repository.api.MergePreventReason;
import sonia.scm.repository.api.MergePreventReasonType;
import sonia.scm.repository.api.MergeStrategy;
import sonia.scm.repository.work.NoneCachingWorkingCopyPool;
import sonia.scm.repository.work.WorkdirProvider;
import sonia.scm.user.User;

import java.io.IOException;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
@SubjectAware(configuration = "classpath:sonia/scm/configuration/shiro.ini", username = "admin", password = "secret")
public class GitMergeCommandTest extends AbstractGitCommandTestBase {

  private static final String REALM = "AdminRealm";

  @Rule
  public ShiroRule shiro = new ShiroRule();
  @Mock
  private AttributeAnalyzer attributeAnalyzer;
  @Mock
  private RepositoryManager repositoryManager;
  @Mock
  private GitRepositoryHookEventFactory eventFactory;

  @BeforeClass
  public static void setSigner() {
    Signers.set(GpgConfig.GpgFormat.OPENPGP, new GitTestHelper.SimpleGpgSigner());
  }

  @Test
  public void shouldDetectMergeableBranches() {
    GitMergeCommand command = createCommand();
    MergeCommandRequest request = new MergeCommandRequest();
    request.setBranchToMerge("mergeable");
    request.setTargetBranch("master");

    MergeDryRunCommandResult result = command.dryRun(request);

    assertThat(result.isMergeable()).isTrue();
    assertThat(result.getReasons()).isEmpty();
  }

  @Test
  public void shouldDetectNotMergeableBranches_FileConflict() {
    GitMergeCommand command = createCommand();
    MergeCommandRequest request = new MergeCommandRequest();
    request.setBranchToMerge("test-branch");
    request.setTargetBranch("master");

    MergeDryRunCommandResult result = command.dryRun(request);

    assertThat(result.isMergeable()).isFalse();
    assertThat(result.getReasons()).hasSize(1);
    MergePreventReason mergePreventReason = result.getReasons().stream().toList().get(0);
    assertThat(mergePreventReason.getType()).isEqualTo(MergePreventReasonType.FILE_CONFLICTS);
    assertThat(mergePreventReason.getAffectedPaths()).containsExactly("a.txt");
  }

  @Test
  public void shouldDetectNotMergeableBranches_ExternalMergeTool() {
    String source = "mergeable";
    String target = "master";
    when(attributeAnalyzer.hasExternalMergeToolConflicts(source, target)).thenReturn(true);
    GitMergeCommand command = createCommand();
    MergeCommandRequest request = new MergeCommandRequest();
    request.setBranchToMerge(source);
    request.setTargetBranch(target);

    MergeDryRunCommandResult result = command.dryRun(request);

    assertThat(result.isMergeable()).isFalse();
    assertThat(result.getReasons()).hasSize(1);
    assertThat(result.getReasons().stream().toList().get(0).getType()).isEqualTo(MergePreventReasonType.EXTERNAL_MERGE_TOOL);
  }

  @Test
  public void shouldDetectNotMergeableBranches_ExternalMergeToolAndFileConflict() {
    String source = "test-branch";
    String target = "master";
    when(attributeAnalyzer.hasExternalMergeToolConflicts(source, target)).thenReturn(true);
    GitMergeCommand command = createCommand();
    MergeCommandRequest request = new MergeCommandRequest();
    request.setBranchToMerge(source);
    request.setTargetBranch(target);

    MergeDryRunCommandResult result = command.dryRun(request);

    assertThat(result.isMergeable()).isFalse();
    assertThat(result.getReasons()).hasSize(2);
    List<MergePreventReason> reasons = result.getReasons().stream().toList();
    assertThat(reasons.get(0).getType()).isEqualTo(MergePreventReasonType.EXTERNAL_MERGE_TOOL);
    assertThat(reasons.get(1).getType()).isEqualTo(MergePreventReasonType.FILE_CONFLICTS);
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
  public void shouldNotMergeConflictingBranches_FileConflict() {
    GitMergeCommand command = createCommand();
    MergeCommandRequest request = new MergeCommandRequest();
    request.setBranchToMerge("test-branch");
    request.setTargetBranch("master");
    request.setMergeStrategy(MergeStrategy.MERGE_COMMIT);

    MergeCommandResult mergeCommandResult = command.merge(request);

    assertThat(mergeCommandResult.isSuccess()).isFalse();
    assertThat(mergeCommandResult.getFilesWithConflict()).containsExactly("a.txt");
  }

  @Test(expected = ConcurrentModificationException.class)
  public void shouldHandleConcurrentBranchModification() {
    GitMergeCommand command = createCommand();
    MergeCommandRequest request = new MergeCommandRequest();
    request.setTargetBranch("master");
    request.setBranchToMerge("mergeable");
    request.setMergeStrategy(MergeStrategy.MERGE_COMMIT);
    request.setAuthor(new Person("Dirk Gently", "dirk@holistic.det"));

    // create concurrent modification after the pre commit hook was fired
    doAnswer(invocation -> {
      RefUpdate refUpdate = createCommand()
        .open()
        .updateRef("refs/heads/master");
      refUpdate.setNewObjectId(ObjectId.fromString("2f95f02d9c568594d31e78464bd11a96c62e3f91"));
      refUpdate.update();
      return null;
    }).when(repositoryManager).fireHookEvent(any());

    command.merge(request);
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
    assertThat(mergeCommit.getParentCount()).isEqualTo(1);
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
    assertThat(mergeCommit.getParentCount()).isEqualTo(1);
    assertThat(mergeCommit.getParent(0).name()).isEqualTo("fcd0ef1831e4002ac43ea539f4094334c79ea9ec");

    GitModificationsCommand modificationsCommand = new GitModificationsCommand(createContext());
    List<Added> changes = modificationsCommand.getModifications("master").getAdded();
    assertThat(changes).hasSize(3);
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
  public void shouldRebaseMultipleCommits() throws IOException, GitAPIException {
    GitMergeCommand command = createCommand();
    MergeCommandRequest request = new MergeCommandRequest();
    request.setTargetBranch("master");
    request.setBranchToMerge("squash");
    request.setMergeStrategy(MergeStrategy.REBASE);
    request.setAuthor(new Person("Dirk Gently", "dirk@holistic.det"));

    command.merge(request);

    Repository repository = createContext().open();
    Iterable<RevCommit> commits = new Git(repository).log().add(repository.resolve("master")).setMaxCount(6).call();

    assertThat(commits)
      .extracting("shortMessage")
      .containsExactly(
        "third",
        "second commit",
        "first commit",
        "added new line for blame",
        "added file f",
        "added file d and e in folder c"
      );
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
    assertThat(mergeCommandResult.getFilesWithConflict()).containsExactly("a.txt");
    Repository repository = createContext().open();
    Iterable<RevCommit> commits = new Git(repository).log().add(repository.resolve("master")).setMaxCount(1).call();
    RevCommit headCommit = commits.iterator().next();
    assertThat(headCommit.getName()).isEqualTo("fcd0ef1831e4002ac43ea539f4094334c79ea9ec");
  }

  @Test
  public void shouldFireEvents() {
    RepositoryHookEvent preReceive = mock(RepositoryHookEvent.class);
    RepositoryHookEvent postReceive = mock(RepositoryHookEvent.class);
    when(eventFactory.createPreReceiveEvent(any(), eq(List.of("master")), any(), any())).thenReturn(preReceive);
    when(eventFactory.createPostReceiveEvent(any(), eq(List.of("master")), any(), any())).thenReturn(postReceive);

    GitMergeCommand command = createCommand();
    MergeCommandRequest request = new MergeCommandRequest();
    request.setTargetBranch("master");
    request.setBranchToMerge("mergeable");
    request.setMergeStrategy(MergeStrategy.MERGE_COMMIT);
    request.setAuthor(new Person("Dirk Gently", "dirk@holistic.det"));

    command.merge(request);

    verify(repositoryManager).fireHookEvent(preReceive);
    verify(repositoryManager).fireHookEvent(postReceive);
  }

  private GitMergeCommand createCommand() {
    return createCommand(git -> {
    });
  }

  private GitMergeCommand createCommand(Consumer<Git> interceptor) {
    return new GitMergeCommand(createContext(), new SimpleGitWorkingCopyFactory(new NoneCachingWorkingCopyPool(new WorkdirProvider(null, repositoryLocationResolver)), new SimpleMeterRegistry()), attributeAnalyzer, repositoryManager, eventFactory) {
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
