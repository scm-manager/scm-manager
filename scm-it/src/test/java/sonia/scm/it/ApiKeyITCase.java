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
import jakarta.ws.rs.core.MediaType;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import sonia.scm.it.utils.RepositoryUtil;
import sonia.scm.it.utils.RestUtil;
import sonia.scm.it.utils.TestData;
import sonia.scm.repository.client.api.RepositoryClient;
import sonia.scm.repository.client.api.RepositoryClientException;
import sonia.scm.web.VndMediaType;

import java.io.IOException;
import java.util.Collection;
import java.util.Objects;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static sonia.scm.it.utils.RepositoryUtil.addAndCommitRandomFile;
import static sonia.scm.it.utils.RestUtil.given;
import static sonia.scm.it.utils.ScmTypes.availableScmTypes;
import static sonia.scm.it.utils.TestData.WRITE;

@RunWith(Parameterized.class)
public class ApiKeyITCase {

  @Parameterized.Parameters(name = "{0}")
  public static Collection<String> createParameters() {
    return availableScmTypes();
  }

  private final String repositoryType;

  public ApiKeyITCase(String repositoryType) {
    this.repositoryType = repositoryType;
  }

  @Rule
  public TemporaryFolder temporaryFolder = new TemporaryFolder();

  @Before
  public void prepareEnvironment() {
    TestData.createDefault();
    TestData.createNotAdminUser("user", "user");
    TestData.createUserPermission("user", WRITE, repositoryType);
  }

  @After
  public void cleanup() {
    TestData.cleanup();
  }

  @Test
  public void shouldCloneWithRestrictedApiKey() throws IOException {
    String passphrase = registerApiKey();

    RepositoryClient client = RepositoryUtil.createRepositoryClient(repositoryType, temporaryFolder.newFolder(), "user", passphrase);

    assertEquals(1, Objects.requireNonNull(client.getWorkingCopy().list()).length);
  }

  @Test
  public void shouldFailToCommit() throws IOException {
    String passphrase = registerApiKey();

    RepositoryClient client = RepositoryUtil.createRepositoryClient(repositoryType, temporaryFolder.newFolder(), "user", passphrase);

    assertThrows(RepositoryClientException.class, () -> addAndCommitRandomFile(client, "user"));
  }


  public String registerApiKey() {
    String apiKeysUrl = given(VndMediaType.ME, "user", "user")
      .when()
      .get(RestUtil.createResourceUrl("me/"))
      .then()
      .statusCode(200)
      .extract()
      .body().jsonPath().getString("_links.apiKeys.href");
    String createUrl = given(VndMediaType.API_KEY_COLLECTION)
      .when()
      .get(apiKeysUrl)
      .then()
      .statusCode(200)
      .extract()
      .body().jsonPath().getString("_links.create.href");
    String passphrase = new String(RestAssured.given()
      .contentType(VndMediaType.API_KEY)
      .accept(MediaType.TEXT_PLAIN)
      .auth().preemptive().basic("user", "user")
      .when()
      .body("{\"displayName\":\"integration test\",\"permissionRole\":\"READ\"}")
      .post(createUrl)
      .then()
      .statusCode(201)
      .extract()
      .body()
      .asByteArray());
    return passphrase;
  }
}
