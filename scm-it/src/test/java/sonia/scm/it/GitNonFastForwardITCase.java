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

package sonia.scm.it;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import jakarta.servlet.http.HttpServletResponse;
import org.eclipse.jgit.api.CommitCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.PushResult;
import org.eclipse.jgit.transport.RemoteRefUpdate;
import org.eclipse.jgit.transport.RemoteRefUpdate.Status;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junitpioneer.jupiter.RetryingTest;
import sonia.scm.it.utils.RestUtil;
import sonia.scm.it.utils.TestData;
import sonia.scm.web.VndMediaType;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static sonia.scm.it.utils.RestUtil.given;

/**
 * Integration Tests for Git with non fast-forward pushes.
 */
class GitNonFastForwardITCase {

  private File workingCopy;
  private Git git;

  @TempDir
  private Path tempFolder;

  @BeforeEach
  void createAndCloneTestRepository() throws GitAPIException {
    TestData.createDefault();
    this.workingCopy = tempFolder.toFile();

    this.git = clone(RestUtil.BASE_URL.toASCIIString() + "repo/scmadmin/HeartOfGold-git");
  }

  @AfterEach
  void cleanup() {
    TestData.cleanup();
  }

  /**
   * Ensures that the normal behaviour (non fast-forward is allowed), is restored after the tests are executed.
   */
  @AfterAll
  public static void allowNonFastForward() {
    setNonFastForwardDisallowed(false);
  }

  @Test
  void testGitPushAmendWithoutForce() throws IOException, GitAPIException {
    setNonFastForwardDisallowed(false);

    addTestFileToWorkingCopyAndCommit("a");
    pushAndAssert(false, Status.OK);

    addTestFileToWorkingCopyAndCommitAmend("c");
    pushAndAssert(false, Status.REJECTED_NONFASTFORWARD);
  }

  @Test
  void testGitPushAmendWithForce() throws IOException, GitAPIException {
    setNonFastForwardDisallowed(false);

    addTestFileToWorkingCopyAndCommit("a");
    pushAndAssert(false, Status.OK);

    addTestFileToWorkingCopyAndCommitAmend("c");
    pushAndAssert(true, Status.OK);
  }

  @RetryingTest(3)
  void testGitPushAmendForceWithDisallowNonFastForward() throws GitAPIException, IOException {
    setNonFastForwardDisallowed(true);

    addTestFileToWorkingCopyAndCommit("a");
    pushAndAssert(false, Status.OK);

    addTestFileToWorkingCopyAndCommitAmend("c");
    pushAndAssert(true, Status.REJECTED_OTHER_REASON);

    setNonFastForwardDisallowed(false);
  }

  private CredentialsProvider createCredentialProvider() {
    return new UsernamePasswordCredentialsProvider(
      RestUtil.ADMIN_USERNAME, RestUtil.ADMIN_PASSWORD
    );
  }

  private Git clone(String url) throws GitAPIException {
    return Git.cloneRepository()
      .setDirectory(workingCopy)
      .setURI(url)
      .setCredentialsProvider(createCredentialProvider())
      .call();
  }

  private void addTestFileToWorkingCopyAndCommit(String name) throws IOException, GitAPIException {
    addTestFile(name);
    prepareCommit()
      .setMessage("added ".concat(name))
      .call();
  }

  private void addTestFile(String name) throws IOException, GitAPIException {
    String filename = name.concat(".txt");
    Files.write(name, new File(workingCopy, filename), Charsets.UTF_8);
    git.add().addFilepattern(filename).call();
  }

  private CommitCommand prepareCommit() {
    return git.commit()
      .setAuthor("Trillian McMillian", "trillian@hitchhiker.com");
  }

  private void pushAndAssert(boolean force, Status expectedStatus) throws GitAPIException {
    Iterable<PushResult> results = push(force);
    assertStatus(results, expectedStatus);
  }

  private Iterable<PushResult> push(boolean force) throws GitAPIException {
    return git.push()
      .setRemote("origin")
      .add("master")
      .setForce(force)
      .setCredentialsProvider(createCredentialProvider())
      .call();
  }

  private void assertStatus(Iterable<PushResult> results, Status expectedStatus) {
    for ( PushResult pushResult : results ) {
      assertStatus(pushResult, expectedStatus);
    }
  }

  private void assertStatus(PushResult pushResult, Status expectedStatus) {
    for ( RemoteRefUpdate remoteRefUpdate : pushResult.getRemoteUpdates() ) {
      assertThat(remoteRefUpdate.getStatus()).isEqualTo(expectedStatus);
    }
  }

  private void addTestFileToWorkingCopyAndCommitAmend(String name) throws IOException, GitAPIException {
    addTestFile(name);
    prepareCommit()
      .setMessage("amend commit, because of missing ".concat(name))
      .setAmend(true)
      .call();
  }

  private static void setNonFastForwardDisallowed(boolean nonFastForwardDisallowed) {
    String config = String.format("{'disabled': false, 'gcExpression': null, 'defaultBranch': 'main', 'lfsWriteAuthorizationExpirationInMinutes': 5, 'nonFastForwardDisallowed': %s}", nonFastForwardDisallowed)
      .replace('\'', '"');

    given(VndMediaType.PREFIX + "gitConfig" + VndMediaType.SUFFIX)
      .body(config)

      .when()
      .put(RestUtil.REST_BASE_URL.toASCIIString() + "config/git" )

      .then()
      .statusCode(HttpServletResponse.SC_NO_CONTENT);
  }

}
