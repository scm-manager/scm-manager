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

package sonia.scm.it;

import org.apache.http.HttpStatus;
import org.junit.Before;
import org.junit.Test;
import sonia.scm.it.utils.ScmRequests;
import sonia.scm.it.utils.TestData;
import sonia.scm.web.VndMediaType;

import static org.junit.Assert.assertNotNull;
import static sonia.scm.it.PermissionsITCase.USER_PASS;
import static sonia.scm.it.utils.RestUtil.ADMIN_PASSWORD;
import static sonia.scm.it.utils.RestUtil.ADMIN_USERNAME;
import static sonia.scm.it.utils.RestUtil.given;
import static sonia.scm.it.utils.TestData.USER_SCM_ADMIN;
import static sonia.scm.it.utils.TestData.callRepository;

public class RoleITCase {

  private static final String USER = "user";
  public static final String ROLE_NAME = "permission-role";

  @Before
  public void init() {
    TestData.createDefault();
    TestData.createNotAdminUser(USER, USER_PASS);
  }

  @Test
  public void userShouldSeePermissionsAfterAddingRoleToUser() {
    callRepository(USER, USER_PASS, "git", HttpStatus.SC_FORBIDDEN);

    String repositoryRolesUrl = new ScmRequests()
      .requestIndexResource(ADMIN_USERNAME, ADMIN_PASSWORD)
      .getUrl("repositoryRoles");

    given()
      .when()
      .delete(repositoryRolesUrl + ROLE_NAME)
      .then()
      .statusCode(HttpStatus.SC_NO_CONTENT);

    given(VndMediaType.REPOSITORY_ROLE)
      .when()
      .body("{" +
        "\"name\": \"" + ROLE_NAME + "\"," +
        "\"verbs\": [\"read\",\"permissionRead\"]" +
        "}")
      .post(repositoryRolesUrl)
      .then()
      .statusCode(HttpStatus.SC_CREATED);

    String permissionUrl = given(VndMediaType.REPOSITORY, USER_SCM_ADMIN, USER_SCM_ADMIN)
      .when()
      .get(TestData.getDefaultRepositoryUrl("git"))
      .then()
      .statusCode(HttpStatus.SC_OK)
      .extract()
      .body().jsonPath().getString("_links.permissions.href");

    given(VndMediaType.REPOSITORY_PERMISSION)
      .when()
      .body("{\n" +
        "\t\"role\": \"" + ROLE_NAME + "\",\n" +
        "\t\"name\": \"" + USER + "\",\n" +
        "\t\"groupPermission\": false\n" +
        "\t\n" +
        "}")
      .post(permissionUrl)
      .then()
      .statusCode(HttpStatus.SC_CREATED);

    assertNotNull(callRepository(USER, USER_PASS, "git", HttpStatus.SC_OK)
      .extract()
      .body().jsonPath().getString("_links.permissions.href"));
  }
}
