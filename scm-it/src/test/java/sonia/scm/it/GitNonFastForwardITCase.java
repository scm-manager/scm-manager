/**
 * Copyright (c) 2010, Sebastian Sdorra
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 3. Neither the name of SCM-Manager; nor the names of its
 *    contributors may be used to endorse or promote products derived from this
 *    software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * http://bitbucket.org/sdorra/scm-manager
 *
 */


package sonia.scm.it;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import org.eclipse.jgit.api.CommitCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.PushResult;
import org.eclipse.jgit.transport.RemoteRefUpdate;
import org.eclipse.jgit.transport.RemoteRefUpdate.Status;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import sonia.scm.it.utils.RestUtil;
import sonia.scm.it.utils.TestData;
import sonia.scm.web.VndMediaType;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static sonia.scm.it.utils.RestUtil.given;

/**
 * Integration Tests for Git with non fast-forward pushes.
 */
public class GitNonFastForwardITCase {

  private File workingCopy;
  private Git git;

  @Rule
  public TemporaryFolder tempFolder = new TemporaryFolder();

  @Before
  public void createAndCloneTestRepository() throws IOException, GitAPIException {
    TestData.createDefault();
    this.workingCopy = tempFolder.newFolder();

    this.git = clone(RestUtil.BASE_URL.toASCIIString() + "repo/scmadmin/HeartOfGold-git");
  }

  @After
  public void cleanup() {
    TestData.cleanup();
  }

  /**
   * Ensures that the normal behaviour (non fast-forward is allowed), is restored after the tests are executed.
   */
  @AfterClass
  public static void allowNonFastForward() {
    setNonFastForwardDisallowed(false);
  }

  @Test
  public void testGitPushAmendWithoutForce() throws IOException, GitAPIException {
    setNonFastForwardDisallowed(false);

    addTestFileToWorkingCopyAndCommit("a");
    pushAndAssert(false, Status.OK);

    addTestFileToWorkingCopyAndCommitAmend("c");
    pushAndAssert(false, Status.REJECTED_NONFASTFORWARD);
  }

  @Test
  public void testGitPushAmendWithForce() throws IOException, GitAPIException {
    setNonFastForwardDisallowed(false);

    addTestFileToWorkingCopyAndCommit("a");
    pushAndAssert(false, Status.OK);

    addTestFileToWorkingCopyAndCommitAmend("c");
    pushAndAssert(true, Status.OK);
  }

  @Test
  public void testGitPushAmendForceWithDisallowNonFastForward() throws GitAPIException, IOException {
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
      assertEquals(expectedStatus, remoteRefUpdate.getStatus());
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
    String config = String.format("{'disabled': false, 'gcExpression': null, 'nonFastForwardDisallowed': %s}", nonFastForwardDisallowed)
      .replace('\'', '"');

    given(VndMediaType.PREFIX + "gitConfig" + VndMediaType.SUFFIX)
      .body(config)

      .when()
      .put(RestUtil.REST_BASE_URL.toASCIIString() + "config/git" )

      .then()
      .statusCode(HttpServletResponse.SC_NO_CONTENT);
  }

}
