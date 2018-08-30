package sonia.scm.it;

import org.apache.http.HttpStatus;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import sonia.scm.repository.client.api.ClientCommand;
import sonia.scm.repository.client.api.RepositoryClient;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

import static java.lang.Thread.sleep;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertNotNull;
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
}
