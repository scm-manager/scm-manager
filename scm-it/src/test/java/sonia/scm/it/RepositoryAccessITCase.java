package sonia.scm.it;

import org.apache.http.HttpStatus;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.IOException;
import java.util.Collection;

import static java.lang.Thread.sleep;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assume.assumeFalse;
import static sonia.scm.it.RestUtil.given;
import static sonia.scm.it.ScmTypes.availableScmTypes;

@RunWith(Parameterized.class)
public class RepositoryAccessITCase {

  @Rule
  public TemporaryFolder tempFolder = new TemporaryFolder();

  private final String repositoryType;
  private RepositoryUtil repositoryUtil;

  public RepositoryAccessITCase(String repositoryType) {
    this.repositoryType = repositoryType;
  }

  @Parameterized.Parameters(name = "{0}")
  public static Collection<String> createParameters() {
    return availableScmTypes();
  }

  @Before
  public void initClient() throws IOException {
    TestData.createDefault();
    repositoryUtil = new RepositoryUtil(repositoryType, tempFolder.getRoot());
  }

  @Test
  public void shouldFindBranches() throws IOException {
    assumeFalse("There are no branches for SVN", repositoryType.equals("svn"));

    repositoryUtil.createAndCommitFile("a.txt", "a");

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
    repositoryUtil.createAndCommitFile("a.txt", "a");
    tempFolder.newFolder("subfolder");
    repositoryUtil.createAndCommitFile("subfolder/a.txt", "sub-a");

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
    System.out.println(subfolderContentUrl);
    given()
      .when()
      .get(subfolderContentUrl)
      .then()
      .statusCode(HttpStatus.SC_OK)
      .body(equalTo("sub-a"));
  }
}
