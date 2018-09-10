package sonia.scm.it;

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
  private RepositoryRequests.AppliedRepositoryGetRequest repositoryGetRequest;

  public RepositoryAccessITCase(String repositoryType) {
    this.repositoryType = repositoryType;
  }

  @Parameterized.Parameters(name = "{0}")
  public static Collection<String> createParameters() {
    return availableScmTypes();
  }

  @Before
  public void init() {
    TestData.createDefault();
    folder = tempFolder.getRoot();
    repositoryGetRequest = RepositoryRequests.start()
      .given()
      .url(TestData.getDefaultRepositoryUrl(repositoryType))
      .usernameAndPassword(ADMIN_USERNAME, ADMIN_PASSWORD)
      .get()
      .assertStatusCode(HttpStatus.SC_OK);
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

  @Test
  @SuppressWarnings("unchecked")
  public void shouldFindFileHistory() throws IOException {
    RepositoryClient repositoryClient = RepositoryUtil.createRepositoryClient(repositoryType, folder);

    String fileName_1 = "a.txt";
    Changeset changeset_1 = RepositoryUtil.createAndCommitFile(repositoryClient, ADMIN_USERNAME, fileName_1, "a");

    repositoryGetRequest
      .usingRepositoryResponse()
      .requestSources()
      .usingSourcesResponse()
      .requestFileHistory(fileName_1)
      .assertStatusCode(HttpStatus.SC_OK)
      .usingChangesetsResponse()
      .assertChangesets(changesets -> {
          assertThat(changesets).hasSize(1);
          assertThat(changesets.get(0)).containsEntry("id", changeset_1.getId());
          assertThat(changesets.get(0)).containsEntry("description", changeset_1.getDescription());
        }
      );
  }
}

