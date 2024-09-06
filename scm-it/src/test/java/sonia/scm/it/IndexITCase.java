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

import io.restassured.RestAssured;
import org.apache.http.HttpStatus;
import org.junit.Test;
import sonia.scm.it.utils.RestUtil;
import sonia.scm.web.VndMediaType;

import static sonia.scm.it.utils.RegExMatcher.matchesPattern;
import static sonia.scm.it.utils.RestUtil.given;

public class IndexITCase {

  @Test
  public void shouldLinkEverythingForAdmin() {
    given(VndMediaType.INDEX)

      .when()
      .get(RestUtil.createResourceUrl(""))

      .then()
      .statusCode(HttpStatus.SC_OK)
      .body(
        "_links.repositories.href", matchesPattern(".+/repositories/"),
        "_links.users.href", matchesPattern(".+/users/"),
        "_links.groups.href", matchesPattern(".+/groups/"),
        "_links.config.href", matchesPattern(".+/config"),
        "_links.gitConfig.href", matchesPattern(".+/config/git"),
        "_links.hgConfig.href", matchesPattern(".+/config/hg"),
        "_links.svnConfig.href", matchesPattern(".+/config/svn")
      );
  }

  @Test
  public void shouldCreateLoginLinksForAnonymousAccess() {
    RestAssured.given() // do not specify user credentials

      .when()
      .get(RestUtil.createResourceUrl(""))

      .then()
      .statusCode(HttpStatus.SC_OK)
      .body(
        "_links.login.href", matchesPattern(".+/auth/.+")
      );
  }

}
