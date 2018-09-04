package sonia.scm.it;

import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import org.apache.http.HttpStatus;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import sonia.scm.repository.Changeset;
import sonia.scm.repository.client.api.ClientCommand;
import sonia.scm.repository.client.api.RepositoryClient;
import sonia.scm.web.VndMediaType;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

import static java.lang.Thread.sleep;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertNotNull;
import static sonia.scm.it.RestUtil.ADMIN_PASSWORD;
import static sonia.scm.it.RestUtil.ADMIN_USERNAME;
import static sonia.scm.it.RestUtil.given;
import static sonia.scm.it.ScmTypes.availableScmTypes;

@RunWith(Parameterized.class)
public class RepositoryAccessITCase {

  @Rule
  public TemporaryFolder tempFolder = new TemporaryFolder();

  private final String repositoryType;
  private File folder;

  public RepositoryAccessITCase(String repositoryType) {
    this.repositoryType = repositoryType;
  }

  @Parameterized.Parameters(name = "{0}")
  public static Collection<String> createParameters() {
    return availableScmTypes();
  }

  @Before
  public void initClient() {
    TestData.createDefault();
    folder = tempFolder.getRoot();
  }

  @Test
  public void shouldFindBranches() throws IOException {
    RepositoryClient repositoryClient = RepositoryUtil.createRepositoryClient(repositoryType, folder);

    Assume.assumeTrue("There are no branches for " + repositoryType, repositoryClient.isCommandSupported(ClientCommand.BRANCH));

    RepositoryUtil.createAndCommitFile(repositoryClient, "scmadmin", "a.txt", "a");

    String branchesUrl = given()
      .when()
      .get(TestData.getDefaultRepositoryUrl(repositoryType))
      .then()
      .statusCode(HttpStatus.SC_OK)
      .extract()
      .path("_links.branches.href");

    Object branchName = given()
      .when()
      .get(branchesUrl)
      .then()
      .statusCode(HttpStatus.SC_OK)
      .extract()
      .path("_embedded.branches[0].name");

    assertNotNull(branchName);
  }

  @Test
  public void shouldFindTags() throws IOException {
    RepositoryClient repositoryClient = RepositoryUtil.createRepositoryClient(repositoryType, folder);

    Assume.assumeTrue("There are no tags for " + repositoryType, repositoryClient.isCommandSupported(ClientCommand.TAG));

    Changeset changeset = RepositoryUtil.createAndCommitFile(repositoryClient, ADMIN_USERNAME, "a.txt", "a");

    String tagName = "v1.0";
    String repositoryUrl = TestData.getDefaultRepositoryUrl(repositoryType);
    String tagsUrl = given()
      .when()
      .get(repositoryUrl)
      .then()
      .statusCode(HttpStatus.SC_OK)
      .extract()
      .path("_links.tags.href");

    ExtractableResponse<Response> response = given(VndMediaType.TAG_COLLECTION, ADMIN_USERNAME, ADMIN_PASSWORD)
      .when()
      .get(tagsUrl)
      .then()
      .statusCode(HttpStatus.SC_OK)
      .extract();

    assertThat(response).isNotNull();
    assertThat(response.body()).isNotNull();
    assertThat(response.body().asString())
      .isNotNull()
      .isNotBlank()
    ;

    RepositoryUtil.addTag(repositoryClient, changeset.getId(), tagName);
    response = given(VndMediaType.TAG_COLLECTION, ADMIN_USERNAME, ADMIN_PASSWORD)
      .when()
      .get(tagsUrl)
      .then()
      .statusCode(HttpStatus.SC_OK)
      .extract()
    ;

    assertThat(response).isNotNull();
    assertThat(response.body()).isNotNull();
    assertThat(response.body().asString())
      .isNotNull()
      .isNotBlank()
    ;
    assertThat(response.body().jsonPath().getString("_links.self.href"))
      .as("assert tags self link")
      .isNotNull()
      .contains(repositoryUrl + "/tags/")
    ;
    assertThat(response.body().jsonPath().getList("_embedded.tags"))
      .as("assert tag size")
      .isNotNull()
      .size()
      .isGreaterThan(0)
    ;
    assertThat(response.body().jsonPath().getMap("_embedded.tags.find{it.name=='" + tagName + "'}"))
      .as("assert tag name and revision")
      .isNotNull()
      .hasSize(3)
      .containsEntry("name", tagName)
      .containsEntry("revision", changeset.getId())
    ;
    assertThat(response.body().jsonPath().getString("_embedded.tags.find{it.name=='" + tagName + "'}._links.self.href"))
      .as("assert single tag self link")
      .isNotNull()
      .contains(String.format("%s/tags/%s", repositoryUrl, tagName))
    ;
    assertThat(response.body().jsonPath().getString("_embedded.tags.find{it.name=='" + tagName + "'}._links.sources.href"))
      .as("assert single tag source link")
      .isNotNull()
      .contains(String.format("%s/sources/%s", repositoryUrl, changeset.getId()))
    ;
    assertThat(response.body().jsonPath().getString("_embedded.tags.find{it.name=='" + tagName + "'}._links.changesets.href"))
      .as("assert single tag changesets link")
      .isNotNull()
      .contains(String.format("%s/changesets/%s", repositoryUrl, changeset.getId()))
    ;
  }

  @Test
  public void shouldReadContent() throws IOException, InterruptedException {
    RepositoryClient repositoryClient = RepositoryUtil.createRepositoryClient(repositoryType, folder);
    RepositoryUtil.createAndCommitFile(repositoryClient, "scmadmin", "a.txt", "a");
    tempFolder.newFolder("subfolder");
    RepositoryUtil.createAndCommitFile(repositoryClient, "scmadmin", "subfolder/a.txt", "sub-a");

    sleep(1000);

    String sourcesUrl = given()
      .when()
      .get(TestData.getDefaultRepositoryUrl(repositoryType))
      .then()
      .statusCode(HttpStatus.SC_OK)
      .extract()
      .path("_links.sources.href");

    String rootContentUrl = given()
      .when()
      .get(sourcesUrl)
      .then()
      .statusCode(HttpStatus.SC_OK)
      .extract()
      .path("files.find{it.name=='a.txt'}._links.self.href");
    given()
      .when()
      .get(rootContentUrl)
      .then()
      .statusCode(HttpStatus.SC_OK)
      .body(equalTo("a"));

    String subfolderSourceUrl = given()
      .when()
      .get(sourcesUrl)
      .then()
      .statusCode(HttpStatus.SC_OK)
      .extract()
      .path("files.find{it.name=='subfolder'}._links.self.href");
    String subfolderContentUrl= given()
      .when()
      .get(subfolderSourceUrl)
      .then()
      .statusCode(HttpStatus.SC_OK)
      .extract()
      .path("files[0]._links.self.href");
    given()
      .when()
      .get(subfolderContentUrl)
      .then()
      .statusCode(HttpStatus.SC_OK)
      .body(equalTo("sub-a"));
  }

  @Test
  public void shouldFindChangesets() throws IOException {
    RepositoryClient repositoryClient = RepositoryUtil.createRepositoryClient(repositoryType, folder);

    RepositoryUtil.createAndCommitFile(repositoryClient, "scmadmin", "a.txt", "a");
    RepositoryUtil.createAndCommitFile(repositoryClient, "scmadmin", "b.txt", "b");

    String changesetsUrl = given()
      .when()
      .get(TestData.getDefaultRepositoryUrl(repositoryType))
      .then()
      .statusCode(HttpStatus.SC_OK)
      .extract()
      .path("_links.changesets.href");

    List changesets = given()
      .when()
      .get(changesetsUrl)
      .then()
      .statusCode(HttpStatus.SC_OK)
      .extract()
      .path("_embedded.changesets.id");

    assertThat(changesets).size().isBetween(2, 3); // svn has an implicit root revision '0' that is extra to the two commits
  }
}

