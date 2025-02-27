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

import org.apache.shiro.subject.SimplePrincipalCollection;
import org.apache.shiro.subject.Subject;
import org.eclipse.jgit.lib.GpgConfig;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.Signers;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.github.sdorra.jse.ShiroExtension;
import org.github.sdorra.jse.SubjectAware;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.NoChangesMadeException;
import sonia.scm.NotFoundException;
import sonia.scm.repository.GitTestHelper;
import sonia.scm.repository.GitUtil;
import sonia.scm.repository.MultipleParentsNotAllowedException;
import sonia.scm.repository.NoParentException;
import sonia.scm.repository.Person;
import sonia.scm.repository.RepositoryManager;
import sonia.scm.repository.api.RevertCommandResult;
import sonia.scm.user.User;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertThrows;

@ExtendWith({MockitoExtension.class, ShiroExtension.class})
class GitRevertCommandTest extends AbstractGitCommandTestBase {

  static final String HEAD_REVISION = "18e22df410df66f027dc49bf0f229f4b9efb8ce5";
  static final String HEAD_MINUS_0_REVISION = "9d39c9f59030fd4e3d37e1d3717bcca43a9a5eef";
  static final String CONFLICTING_TARGET_BRANCH = "conflictingTargetBranch";
  static final String CONFLICTING_SOURCE_REVISION = "0d5be1f22687d75916c82ce10eb592375ba0fb21";
  static final String PARENTLESS_REVISION = "190bc4670197edeb724f0ee1e49d3a5307635228";
  static final String DIVERGING_BRANCH = "divergingBranch";
  static final String DIVERGING_MAIN_LATEST_ANCESTOR = "0d5be1f22687d75916c82ce10eb592375ba0fb21";
  static final String DIVERGING_BRANCH_LATEST_COMMIT = "e77fd7c8cd45be992e19a6d22170ead4fcd5f9ce";
  static final String MERGED_REVISION = "00da9cca94a507346c5b8284983f8a69840cc277";

  @Mock
  RepositoryManager repositoryManager;
  @Mock
  GitRepositoryHookEventFactory gitRepositoryHookEventFactory;

  @Override
  protected String getZippedRepositoryResource() {
    return "sonia/scm/repository/spi/scm-git-spi-revert-test.zip";
  }

  @Nested
  class Revert {

    @BeforeAll
    public static void setSigner() {
      Signers.set(GpgConfig.GpgFormat.OPENPGP, new GitTestHelper.SimpleGpgSigner());
    }

    /**
     * We expect the newly created revision to be merged into the given branch.
     */
    @Test
    void shouldBeTipOfHeadBranchAfterRevert() throws IOException {
      GitRevertCommand command = createCommand();
      RevertCommandRequest request = new RevertCommandRequest();
      request.setRevision(HEAD_REVISION);
      request.setAuthor(new Person("Luke Skywalker", "luke@je.di"));
      RevertCommandResult result = command.revert(request);

      try (
        GitContext context = createContext();
        Repository jRepository = context.open()) {
        assertThat(GitUtil.getBranchId(jRepository, "main").getObjectId().getName()).isEqualTo(result.getRevision());
      }
    }

    @Test
    void shouldBeTipOfDifferentBranchAfterRevert() throws IOException {
      GitRevertCommand command = createCommand();
      RevertCommandRequest request = new RevertCommandRequest();
      request.setRevision(DIVERGING_MAIN_LATEST_ANCESTOR);
      request.setAuthor(new Person("Luke Skywalker", "luke@je.di"));
      request.setBranch(DIVERGING_BRANCH);
      RevertCommandResult result = command.revert(request);

      try (
        GitContext context = createContext();
        Repository jRepository = context.open()) {
        assertThat(GitUtil.getBranchId(jRepository, DIVERGING_BRANCH).getObjectId().getName()).isEqualTo(result.getRevision());
      }
    }

    @Test
    void shouldNotRevertWithoutChange() {
      GitRevertCommand command = createCommand();
      RevertCommandRequest request = new RevertCommandRequest();
      request.setRevision(HEAD_REVISION);
      request.setAuthor(new Person("Luke Skywalker", "luke@je.di"));

      command.revert(request);

      assertThrows(NoChangesMadeException.class, () -> command.revert(request));
    }

    /**
     * Reverting this very commit.
     */
    @Test
    void shouldRevertHeadCommit() throws IOException {
      GitRevertCommand command = createCommand();
      RevertCommandRequest request = new RevertCommandRequest();
      request.setRevision(HEAD_REVISION);
      request.setAuthor(new Person("Luke Skywalker", "luke@je.di"));
      request.setBranch("main");
      RevertCommandResult result = command.revert(request);

      assertThat(result.isSuccessful()).isTrue();
      try (GitContext context = createContext()) {
        GitDiffCommand diffCommand = new GitDiffCommand(context);
        DiffCommandRequest diffRequest = new DiffCommandRequest();
        diffRequest.setRevision(result.getRevision());
        diffRequest.setPath("hitchhiker");
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
          diffCommand.getDiffResult(diffRequest).accept(baos);
          assertThat(baos.toString()).contains("George Lucas\n-Darth Vader");
        }
      }
    }

    /**
     * Reverting this very commit.
     * The branch is not explicitly set, so we expect the default branch.
     */
    @Test
    void shouldRevertHeadCommitImplicitly() throws IOException {
      GitRevertCommand command = createCommand();
      RevertCommandRequest request = new RevertCommandRequest();
      request.setRevision(HEAD_REVISION);
      request.setAuthor(new Person("Luke Skywalker", "luke@je.di"));
      RevertCommandResult result = command.revert(request);

      assertThat(result.isSuccessful()).isTrue();
      try (GitContext context = createContext()) {
        GitDiffCommand diffCommand = new GitDiffCommand(context);
        DiffCommandRequest diffRequest = new DiffCommandRequest();
        diffRequest.setRevision(result.getRevision());
        diffRequest.setPath("hitchhiker");
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
          diffCommand.getDiffResult(diffRequest).accept(baos);
          assertThat(baos.toString()).contains("George Lucas\n-Darth Vader");
        }
      }
    }

    /**
     * Reverting a change from one commit ago.
     */
    @Test
    void shouldRevertPreviousHistoryCommit() throws IOException {
      GitRevertCommand command = createCommand();
      RevertCommandRequest request = new RevertCommandRequest();
      request.setRevision(HEAD_MINUS_0_REVISION);
      request.setAuthor(new Person("Luke Skywalker", "luke@je.di"));
      request.setBranch("main");
      RevertCommandResult result = command.revert(request);

      assertThat(result.isSuccessful()).isTrue();
      try (GitContext context = createContext()) {
        GitDiffCommand diffCommand = new GitDiffCommand(context);
        DiffCommandRequest diffRequest = new DiffCommandRequest();
        diffRequest.setRevision(result.getRevision());
        diffRequest.setPath("kerbal");
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
          diffCommand.getDiffResult(diffRequest).accept(baos);
          assertThat(baos.toString()).contains("-deathstar\n+kerbin");
        }
      }
    }

    @Test
    void shouldRevertCommitOnDifferentBranch() throws IOException {
      GitRevertCommand command = createCommand();
      RevertCommandRequest request = new RevertCommandRequest();
      request.setRevision(DIVERGING_MAIN_LATEST_ANCESTOR);
      request.setAuthor(new Person("Luke Skywalker", "luke@je.di"));
      request.setBranch(DIVERGING_BRANCH);
      RevertCommandResult result = command.revert(request);
      assertThat(result.isSuccessful()).isTrue();

      try (
        GitContext context = createContext();
        Repository jRepository = context.open();
        RevWalk revWalk = new RevWalk(jRepository)) {
        ObjectId objectId = GitUtil.getRevisionId(jRepository, result.getRevision());
        RevCommit commit = GitUtil.getCommit(jRepository, revWalk, objectId);
        assertThat(commit.getParent(0).getName()).isEqualTo(DIVERGING_BRANCH_LATEST_COMMIT);

        GitDiffCommand diffCommand = new GitDiffCommand(context);
        DiffCommandRequest diffRequest = new DiffCommandRequest();

        diffRequest.setRevision(result.getRevision());
        diffRequest.setAncestorChangeset(DIVERGING_BRANCH_LATEST_COMMIT);
        diffRequest.setPath("hitchhiker");
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
          diffCommand.getDiffResult(diffRequest).accept(baos);
          assertThat(baos.toString()).contains("""
            -George Lucas
            +Douglas Adams"""
          );
        }
      }
    }

    @Test
    void shouldRevertTwiceOnDiffHeads() throws IOException {
      GitRevertCommand command = createCommand();
      RevertCommandRequest request = new RevertCommandRequest();
      request.setRevision(HEAD_MINUS_0_REVISION);
      request.setAuthor(new Person("Luke Skywalker", "luke@je.di"));
      request.setBranch("main");
      RevertCommandResult result1 = command.revert(request);

      assertThat(result1.isSuccessful()).isTrue();

      request.setRevision(result1.getRevision());
      RevertCommandResult result2 = command.revert(request);

      assertThat(result2.isSuccessful()).isTrue();

      try (GitContext context = createContext()) {
        GitDiffCommand diffCommand = new GitDiffCommand(context);
        DiffCommandRequest diffRequest = new DiffCommandRequest();

        // Check against original head; should be the same
        diffRequest.setRevision(HEAD_REVISION);
        diffRequest.setPath("kerbal");
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
          diffCommand.getDiffResult(diffRequest).accept(baos);
          // no difference, thus empty
          assertThat(baos.toString()).isEmpty();
        }
      }
    }

    @Test
    void shouldReportCorrectFilesAfterMergeConflict() {
      GitRevertCommand command = createCommand();
      RevertCommandRequest request = new RevertCommandRequest();
      request.setRevision(CONFLICTING_SOURCE_REVISION);
      request.setAuthor(new Person("Luke Skywalker", "luke@je.di"));
      request.setBranch(CONFLICTING_TARGET_BRANCH);
      RevertCommandResult result = command.revert(request);

      assertThat(result.isSuccessful()).isFalse();
      assertThat(result.getFilesWithConflict()).containsExactly("hitchhiker");
    }

    @Test
    void shouldSetCustomMessageIfGiven() throws IOException {
      GitRevertCommand command = createCommand();
      RevertCommandRequest request = new RevertCommandRequest();
      request.setRevision(HEAD_REVISION);
      request.setAuthor(new Person("Luke Skywalker", "luke@je.di"));
      request.setBranch("main");
      request.setMessage("I will never join you!");
      RevertCommandResult result = command.revert(request);

      assertThat(result.isSuccessful()).isTrue();

      try (
        GitContext context = createContext();
        Repository jRepository = context.open();
        RevWalk revWalk = new RevWalk(jRepository)) {
        ObjectId objectId = GitUtil.getRevisionId(jRepository, result.getRevision());
        RevCommit commit = GitUtil.getCommit(jRepository, revWalk, objectId);
        assertThat(commit.getShortMessage()).isEqualTo("I will never join you!");
      }
    }

    @Test
    void shouldSetDefaultMessageIfNoCustomMessageGiven() throws IOException {
      GitRevertCommand command = createCommand();
      RevertCommandRequest request = new RevertCommandRequest();
      request.setRevision(HEAD_REVISION);
      request.setAuthor(new Person("Luke Skywalker", "luke@je.di"));
      request.setBranch("main");
      RevertCommandResult result = command.revert(request);

      try (
        GitContext context = createContext();
        Repository jRepository = context.open();
        RevWalk revWalk = new RevWalk(jRepository)) {

        ObjectId revertedCommitId = GitUtil.getRevisionId(jRepository, request.getRevision());
        RevCommit revertedCommit = GitUtil.getCommit(jRepository, revWalk, revertedCommitId);
        ObjectId newCommitId = GitUtil.getRevisionId(jRepository, result.getRevision());
        RevCommit newCommit = GitUtil.getCommit(jRepository, revWalk, newCommitId);

        String expectedFullMessage = String.format("""
          Revert "%s"
          
          This reverts commit %s.""",
          revertedCommit.getShortMessage(), revertedCommit.getName());

        assertThat(newCommit.getShortMessage()).isEqualTo(
          "Revert \"" + revertedCommit.getShortMessage() + "\"");
        assertThat(newCommit.getFullMessage()).isEqualTo(expectedFullMessage);
      }
    }

    @Test
    void shouldSignRevertCommit() throws IOException {
      GitRevertCommand command = createCommand();
      RevertCommandRequest request = new RevertCommandRequest();
      request.setRevision(HEAD_REVISION);
      request.setAuthor(new Person("Luke Skywalker", "luke@je.di"));
      RevertCommandResult result = command.revert(request);

      try (
        GitContext context = createContext();
        Repository jRepository = context.open();
        RevWalk revWalk = new RevWalk(jRepository)) {

        ObjectId newCommitId = GitUtil.getRevisionId(jRepository, result.getRevision());
        RevCommit newCommit = GitUtil.getCommit(jRepository, revWalk, newCommitId);

        assertThat(newCommit.getRawGpgSignature()).isNotEmpty();
        assertThat(newCommit.getRawGpgSignature()).isEqualTo(GitTestHelper.SimpleGpgSigner.getSignature());
      }
    }

    @Test
    void shouldSignNoRevertCommitIfSigningIsDisabled() throws IOException {
      GitRevertCommand command = createCommand();
      RevertCommandRequest request = new RevertCommandRequest();
      request.setRevision(HEAD_REVISION);
      request.setAuthor(new Person("Luke Skywalker", "luke@je.di"));
      request.setSign(false);
      RevertCommandResult result = command.revert(request);

      try (
        GitContext context = createContext();
        Repository jRepository = context.open();
        RevWalk revWalk = new RevWalk(jRepository)) {

        ObjectId newCommitId = GitUtil.getRevisionId(jRepository, result.getRevision());
        RevCommit newCommit = GitUtil.getCommit(jRepository, revWalk, newCommitId);

        assertThat(newCommit.getRawGpgSignature()).isNullOrEmpty();
      }
    }

    @Test
    @SubjectAware(value = "admin", permissions = "*:*:*")
    void shouldTakeAuthorFromSubjectIfNotSet() throws IOException {
      SimplePrincipalCollection principals = new SimplePrincipalCollection();
      principals.add("admin", "AdminRealm");
      principals.add(new User("hitchhiker", "Douglas Adams", "ga@la.xy"), "AdminRealm");
      setSubject(new Subject.Builder()
        .principals(principals)
        .authenticated(true)
        .buildSubject());
      GitRevertCommand command = createCommand();
      RevertCommandRequest request = new RevertCommandRequest();
      request.setRevision(HEAD_REVISION);

      RevertCommandResult result = command.revert(request);

      assertThat(result.isSuccessful()).isTrue();

      try (
        GitContext context = createContext();
        Repository jRepository = context.open();
        RevWalk revWalk = new RevWalk(jRepository)) {

        ObjectId newCommitId = GitUtil.getRevisionId(jRepository, result.getRevision());
        RevCommit newCommit = GitUtil.getCommit(jRepository, revWalk, newCommitId);

        PersonIdent author = newCommit.getAuthorIdent();
        assertThat(author.getName()).isEqualTo("Douglas Adams");
        assertThat(author.getEmailAddress()).isEqualTo("ga@la.xy");
      }
    }

    @Test
    void shouldThrowNotFoundExceptionWhenBranchNotExist() {
      GitRevertCommand command = createCommand();
      RevertCommandRequest request = new RevertCommandRequest();
      request.setRevision(HEAD_REVISION);
      request.setAuthor(new Person("Luke Skywalker", "luke@je.di"));
      request.setBranch("BogusBranch");
      assertThatThrownBy(() -> command.revert(request))
        .isInstanceOf(NotFoundException.class)
        .hasMessage("could not find objectid with id BogusBranch in repository with id hitchhiker/HeartOfGold");
    }

    @Test
    void shouldThrowNotFoundExceptionWhenRevisionNotExist() {
      GitRevertCommand command = createCommand();
      RevertCommandRequest request = new RevertCommandRequest();
      request.setRevision("BogusRevision");
      request.setAuthor(new Person("Luke Skywalker", "luke@je.di"));
      assertThatThrownBy(() -> command.revert(request))
        .isInstanceOf(NotFoundException.class)
        .hasMessage("could not find objectid with id BogusRevision in repository with id hitchhiker/HeartOfGold");
    }

    @Test
    void shouldThrowNoParentExceptionWhenParentNotExist() {
      GitRevertCommand command = createCommand();
      RevertCommandRequest request = new RevertCommandRequest();
      request.setRevision(PARENTLESS_REVISION);
      request.setAuthor(new Person("Luke Skywalker", "luke@je.di"));
      assertThatThrownBy(() -> command.revert(request))
        .isInstanceOf(NoParentException.class)
        .hasMessage(PARENTLESS_REVISION + " has no parent.");
    }

    @Test
    void shouldThrowMultipleParentsExceptionWhenPickingMergedCommit() {
      GitRevertCommand command = createCommand();
      RevertCommandRequest request = new RevertCommandRequest();
      request.setRevision(MERGED_REVISION);
      request.setAuthor(new Person("Luke Skywalker", "luke@je.di"));
      assertThatThrownBy(() -> command.revert(request))
        .isInstanceOf(MultipleParentsNotAllowedException.class)
        .hasMessage(MERGED_REVISION + " has more than one parent changeset, which is not allowed with this request.");
    }

    private GitRevertCommand createCommand() {
      return new GitRevertCommand(createContext("main"), repositoryManager, gitRepositoryHookEventFactory);
    }
  }
}
