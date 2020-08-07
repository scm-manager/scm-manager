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
import org.assertj.core.api.Assertions;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import sonia.scm.it.utils.RepositoryUtil;
import sonia.scm.it.utils.RestUtil;
import sonia.scm.it.utils.TestData;
import sonia.scm.repository.Person;
import sonia.scm.repository.client.api.RepositoryClient;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static java.lang.String.format;
import static java.util.Collections.singletonList;
import static sonia.scm.it.utils.TestData.USER_SCM_ADMIN;

public class MergeDetectionITCase {

  private static final Person ARTHUR = new Person("arthur", "arthur@hitchhiker.com");

  @Rule
  public TemporaryFolder temporaryFolder = new TemporaryFolder();

  RepositoryClient client;
  String masterFile;
  String developFile;

  @Before
  public void createRepository() throws IOException {
    TestData.createDefault();

    client = RepositoryUtil.createRepositoryClient("git", temporaryFolder.getRoot());

    masterFile = createFile("hg2g.md");
    developFile = createFile("how_to_make_tea.md");

    client.getAddCommand().add(masterFile);
    client.getCommitCommand().commit(ARTHUR, "Add base file");
    client.getPushCommand().push();

    client.getBranchCommand().branch("develop");
    client.getCheckoutCommand().checkout("develop");
    client.getAddCommand().add(developFile);
    client.getCommitCommand().commit(ARTHUR, "add more important things");
    client.getPushCommand().push();
  }

  @After
  public void disableMergeDetection() {
    RestAssured.given()
      .auth().preemptive().basic(USER_SCM_ADMIN, USER_SCM_ADMIN)
      .when()
      .contentType("application/json")
      .accept("application/json")
      .body(toJson("{}"))
      .post(RestUtil.createResourceUrl("integration-test/merge-detection/"))
      .then()
      .statusCode(204);
  }

  @Test
  public void shouldDetectSimpleMergeAsMerged() throws IOException {
    client.getCheckoutCommand().checkout("master");
    client.getMergeCommand().noFf().merge("develop");

    initializeMergeDetection("master", "develop");

    client.getPushCommand().push();

    Assertions.assertThat(getMergeDetectionResult("preMergeDetection", 0)).isTrue();
    Assertions.assertThat(getMergeDetectionResult("postMergeDetection", 0)).isTrue();
  }

  @Test
  public void shouldDetectFastForwardAsMerged() throws IOException {
    client.getCheckoutCommand().checkout("master");
    client.getMergeCommand().merge("develop");

    initializeMergeDetection("master", "develop");

    client.getPushCommand().push();

    Assertions.assertThat(getMergeDetectionResult("preMergeDetection", 0)).isTrue();
    Assertions.assertThat(getMergeDetectionResult("postMergeDetection", 0)).isTrue();
  }

  @Test
  public void shouldDetectMergeWhenBranchHasBeenDeletedAsMerged() throws IOException {
    client.getCheckoutCommand().checkout("master");
    client.getMergeCommand().merge("develop");
    client.getPushCommand().push();

    initializeMergeDetection("master", "develop");

    client.getDeleteRemoteBranchCommand().delete("develop");
    client.getPushCommand().push();

    Assertions.assertThat(getMergeDetectionResult("preMergeDetection", 0)).isTrue();
    Assertions.assertThat(getMergeDetectionResult("postMergeDetection", 0)).isTrue();
  }

  @Test
  public void shouldDetectNormalPushAsNotMerged() throws IOException {
    client.getCheckoutCommand().checkout("develop");
    writeFile(developFile, "other content");
    client.getAddCommand().add(developFile);
    client.getCommitCommand().commit(ARTHUR, "simple commit");

    initializeMergeDetection("master", "develop");

    client.getPushCommand().push();

    Assertions.assertThat(getMergeDetectionResult("preMergeDetection", 0)).isFalse();
    Assertions.assertThat(getMergeDetectionResult("postMergeDetection", 0)).isFalse();
  }

  private boolean getMergeDetectionResult(String type, int n) {
    return RestAssured.given()
      .auth().preemptive().basic(USER_SCM_ADMIN, USER_SCM_ADMIN)
      .when()
      .accept("application/json")
      .get(RestUtil.createResourceUrl("integration-test/"))
      .then()
      .statusCode(200)
      .extract()
      .jsonPath()
      .getBoolean("_embedded." +
        type +
        "[" + n + "].merged");
  }

  private void initializeMergeDetection(String target, String branch) {
    RestAssured.given()
      .auth().preemptive().basic(USER_SCM_ADMIN, USER_SCM_ADMIN)
      .when()
      .contentType("application/json")
      .accept("application/json")
      .body(toJson(format("{'target': '%s', 'branch': '%s'}", target, branch)))
      .post(RestUtil.createResourceUrl("integration-test/merge-detection/"))
      .then()
      .statusCode(204);
  }

  private String createFile(String name) throws IOException {
    temporaryFolder.newFile(name);
    writeFile(name, "Some content");
    return name;
  }

  private void writeFile(String name, String content) throws IOException {
    Path file = temporaryFolder.getRoot().toPath().resolve(name);
    Files.write(file, singletonList(content));
  }

  private String toJson(String json) {
    return json.replaceAll("'", "\"");
  }
}
