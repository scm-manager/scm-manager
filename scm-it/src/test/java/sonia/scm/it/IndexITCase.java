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
