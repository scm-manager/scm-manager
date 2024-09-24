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
