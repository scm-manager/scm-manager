/**
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

//~--- non-JDK imports --------------------------------------------------------

import org.apache.http.HttpStatus;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import sonia.scm.web.VndMediaType;

import java.io.IOException;
import java.util.Collection;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static sonia.scm.it.RegExMatcher.matchesPattern;
import static sonia.scm.it.RestUtil.createResourceUrl;
import static sonia.scm.it.RestUtil.given;
import static sonia.scm.it.RestUtil.readJson;

@RunWith(Parameterized.class)
public class RepositoriesITCase {

  private final String repositoryType;

  public RepositoriesITCase(String repositoryType) {
    this.repositoryType = repositoryType;
  }

  @Parameters(name = "{0}")
  public static Collection<String> createParameters() {
    return ScmParameterizedIntegrationTestUtil.createParameters();
  }

  @After
  public void cleanup() {
    TestData.cleanup();
  }

  @Test
  public void shouldCreateSuccessfully() throws IOException {
    String repositoryUrl = createRepository();

    given(VndMediaType.REPOSITORY)

      .when()
      .get(repositoryUrl)

      .then()
      .statusCode(HttpStatus.SC_OK)
      .body(
        "name", equalTo("HeartOfGold-" + repositoryType),
        "type", equalTo(repositoryType),
        "creationDate", matchesPattern("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d+Z"),
        "lastModified", is(nullValue()),
        "_links.self.href", equalTo(repositoryUrl)
      );
  }

  @Test
  public void shouldDeleteSuccessfully() throws IOException {
    String repositoryUrl = createRepository();

    given(VndMediaType.REPOSITORY)

      .when()
      .delete(repositoryUrl)

      .then()
      .statusCode(HttpStatus.SC_NO_CONTENT);

    given(VndMediaType.REPOSITORY)

      .when()
      .get(repositoryUrl)

      .then()
      .statusCode(HttpStatus.SC_NOT_FOUND);
  }

  @Test
  public void shouldRejectMultipleCreations() throws IOException {
    String repositoryJson = readJson("repository-" + repositoryType + ".json");
    createRepository();

    given(VndMediaType.REPOSITORY)
      .body(repositoryJson)

      .when()
      .post(createResourceUrl("repositories"))

      .then()
      .statusCode(HttpStatus.SC_CONFLICT);
  }

  private String createRepository() throws IOException {
    return given(VndMediaType.REPOSITORY)
      .body(readJson("repository-" + repositoryType + ".json"))

      .when()
      .post(createResourceUrl("repositories"))

      .then()
      .statusCode(HttpStatus.SC_CREATED)
      .extract()
      .header("location");
  }
}
