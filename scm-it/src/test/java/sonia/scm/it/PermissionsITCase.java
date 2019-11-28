/*
 * Copyright (c) 2010, Sebastian Sdorra
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * 3. Neither the name of SCM-Manager; nor the names of its
 * contributors may be used to endorse or promote products derived from this
 * software without specific prior written permission.
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
 */


package sonia.scm.it;

import org.apache.http.HttpStatus;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import sonia.scm.it.utils.RepositoryUtil;
import sonia.scm.it.utils.TestData;
import sonia.scm.repository.client.api.RepositoryClient;
import sonia.scm.repository.client.api.RepositoryClientException;
import sonia.scm.web.VndMediaType;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static sonia.scm.it.utils.RepositoryUtil.addAndCommitRandomFile;
import static sonia.scm.it.utils.RestUtil.given;
import static sonia.scm.it.utils.ScmTypes.availableScmTypes;
import static sonia.scm.it.utils.TestData.OWNER;
import static sonia.scm.it.utils.TestData.READ;
import static sonia.scm.it.utils.TestData.USER_SCM_ADMIN;
import static sonia.scm.it.utils.TestData.WRITE;
import static sonia.scm.it.utils.TestData.callRepository;

@RunWith(Parameterized.class)
public class PermissionsITCase {

  public static final String USER_READ = "user_read";
  public static final String USER_PASS = "pass";
  private static final String USER_WRITE = "user_write";
  private static final String USER_OWNER = "user_owner";
  private static final String USER_OTHER = "user_other";

  @Rule
  public TemporaryFolder temporaryFolder = new TemporaryFolder();

  private final String repositoryType;
  private Collection<String> createdPermissions;


  public PermissionsITCase(String repositoryType) {
    this.repositoryType = repositoryType;
  }

  @Parameters(name = "{0}")
  public static Collection<String> createParameters() {
    return availableScmTypes();
  }

  @Before
  public void prepareEnvironment() {
    TestData.createDefault();
    TestData.createNotAdminUser(USER_READ, USER_PASS);
    TestData.createUserPermission(USER_READ, READ, repositoryType);
    TestData.createNotAdminUser(USER_WRITE, USER_PASS);
    TestData.createUserPermission(USER_WRITE, WRITE, repositoryType);
    TestData.createNotAdminUser(USER_OWNER, USER_PASS);
    TestData.createUserPermission(USER_OWNER, OWNER, repositoryType);
    TestData.createNotAdminUser(USER_OTHER, USER_PASS);
    createdPermissions = asList(USER_READ, USER_WRITE, USER_OWNER);
  }

  @Test
  public void readUserShouldNotSeePermissions() {
    assertNull(callRepository(USER_READ, USER_PASS, repositoryType, HttpStatus.SC_OK)
      .extract()
      .body().jsonPath().getString("_links.permissions.href"));
  }

  @Test
  public void readUserShouldNotSeeBruteForcePermissions() {
    given(VndMediaType.REPOSITORY_PERMISSION, USER_READ, USER_PASS)
      .when()
      .get(TestData.getDefaultPermissionUrl(USER_SCM_ADMIN, USER_SCM_ADMIN, repositoryType))
      .then()
      .statusCode(HttpStatus.SC_FORBIDDEN);
  }

  @Test
  public void writeUserShouldNotSeePermissions() {
    assertNull(callRepository(USER_WRITE, USER_PASS, repositoryType, HttpStatus.SC_OK)
      .extract()
      .body().jsonPath().getString("_links.permissions.href"));
  }

  @Test
  public void writeUserShouldNotSeeBruteForcePermissions() {
    given(VndMediaType.REPOSITORY_PERMISSION, USER_WRITE, USER_PASS)
      .when()
      .get(TestData.getDefaultPermissionUrl(USER_SCM_ADMIN, USER_SCM_ADMIN, repositoryType))
      .then()
      .statusCode(HttpStatus.SC_FORBIDDEN);
  }

  @Test
  public void ownerShouldSeePermissions() {
    List<Map> userPermissions = TestData.getUserPermissions(USER_OWNER, USER_PASS, repositoryType);
    Assertions.assertThat(userPermissions).extracting(e -> e.get("name")).containsAll(createdPermissions);
  }

  @Test
  public void otherUserShouldNotSeeRepository() {
    callRepository(USER_OTHER, USER_PASS, repositoryType, HttpStatus.SC_FORBIDDEN);
  }

  @Test
  public void otherUserShouldNotSeeBruteForcePermissions() {
    given(VndMediaType.REPOSITORY_PERMISSION, USER_OTHER, USER_PASS)
      .when()
      .get(TestData.getDefaultPermissionUrl(USER_SCM_ADMIN, USER_SCM_ADMIN, repositoryType))
      .then()
      .statusCode(HttpStatus.SC_FORBIDDEN);
  }

  @Test
  public void readUserShouldCloneRepository() throws IOException {
    RepositoryClient client = RepositoryUtil.createRepositoryClient(repositoryType, temporaryFolder.newFolder(), USER_READ, USER_PASS);
    assertEquals(1, Objects.requireNonNull(client.getWorkingCopy().list()).length);
  }

  @Test
  public void writeUserShouldCloneRepository() throws IOException {
    RepositoryClient client = RepositoryUtil.createRepositoryClient(repositoryType, temporaryFolder.newFolder(), USER_WRITE, USER_PASS);
    assertEquals(1, Objects.requireNonNull(client.getWorkingCopy().list()).length);
  }

  @Test
  public void ownerShouldCloneRepository() throws IOException {
    RepositoryClient client = RepositoryUtil.createRepositoryClient(repositoryType, temporaryFolder.newFolder(), USER_OWNER, USER_PASS);
    assertEquals(1, Objects.requireNonNull(client.getWorkingCopy().list()).length);
  }

  @Test
  public void otherUserShouldNotCloneRepository() {
    TestData.callRepository(USER_OTHER, USER_PASS, repositoryType, HttpStatus.SC_FORBIDDEN);
  }

  @Test(expected = RepositoryClientException.class)
  public void userWithReadPermissionShouldBeNotAuthorizedToCommit() throws IOException {
    createAndCommit(USER_READ);
  }

  @Test
  public void userWithOwnerPermissionShouldBeAuthorizedToCommit() throws IOException {
    createAndCommit(USER_OWNER);
  }

  @Test
  public void userWithWritePermissionShouldBeAuthorizedToCommit() throws IOException {
    createAndCommit(USER_WRITE);
  }

  private void createAndCommit(String username) throws IOException {
    RepositoryClient client = RepositoryUtil.createRepositoryClient(repositoryType, temporaryFolder.newFolder(), username, PermissionsITCase.USER_PASS);
    addAndCommitRandomFile(client, username);
  }

  @Test
  public void userWithOwnerPermissionShouldBeAuthorizedToDeleteRepository(){
    assertDeleteRepositoryOperation(HttpStatus.SC_NO_CONTENT, HttpStatus.SC_NOT_FOUND, USER_OWNER, repositoryType);
  }

  @Test
  public void userWithReadPermissionShouldNotBeAuthorizedToDeleteRepository(){
    assertDeleteRepositoryOperation(HttpStatus.SC_FORBIDDEN, HttpStatus.SC_OK, USER_READ, repositoryType);
  }

  @Test
  public void userWithWritePermissionShouldNotBeAuthorizedToDeleteRepository(){
    assertDeleteRepositoryOperation(HttpStatus.SC_FORBIDDEN, HttpStatus.SC_OK, USER_WRITE, repositoryType);
  }

  private void assertDeleteRepositoryOperation(int expectedDeleteStatus, int expectedGetStatus, String user, String repositoryType) {
    given(VndMediaType.REPOSITORY, user, PermissionsITCase.USER_PASS)

      .when()
      .delete(TestData.getDefaultRepositoryUrl(repositoryType))

      .then()
      .statusCode(expectedDeleteStatus);

    given(VndMediaType.REPOSITORY, user, PermissionsITCase.USER_PASS)

      .when()
      .get(TestData.getDefaultRepositoryUrl(repositoryType))

      .then()
      .statusCode(expectedGetStatus);
  }
}
