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

import groovy.util.logging.Slf4j;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import org.apache.http.HttpStatus;
import org.assertj.core.util.Lists;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import sonia.scm.it.utils.RepositoryUtil;
import sonia.scm.it.utils.ScmRequests;
import sonia.scm.it.utils.TestData;
import sonia.scm.repository.Changeset;
import sonia.scm.repository.client.api.ClientCommand;
import sonia.scm.repository.client.api.RepositoryClient;
import sonia.scm.web.VndMediaType;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.lang.Thread.sleep;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertNotNull;
import static sonia.scm.it.utils.RestUtil.ADMIN_PASSWORD;
import static sonia.scm.it.utils.RestUtil.ADMIN_USERNAME;
import static sonia.scm.it.utils.RestUtil.given;
import static sonia.scm.it.utils.ScmTypes.availableScmTypes;

@RunWith(Parameterized.class)
@Slf4j
public class RepositoryAccessITCase {

  @Rule
  public TemporaryFolder tempFolder = new TemporaryFolder();

  private final String repositoryType;
  private File folder;
  private ScmRequests.RepositoryResponse<ScmRequests.IndexResponse> repositoryResponse;

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
    String namespace = ADMIN_USERNAME;
    String repo = TestData.getDefaultRepoName(repositoryType);
    repositoryResponse =
      ScmRequests.start()
        .requestIndexResource(ADMIN_USERNAME, ADMIN_PASSWORD)
        .requestRepository(namespace, repo)
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
      .isNotBlank();

    RepositoryUtil.addTag(repositoryClient, changeset.getId(), tagName);
    response = given(VndMediaType.TAG_COLLECTION, ADMIN_USERNAME, ADMIN_PASSWORD)
      .when()
      .get(tagsUrl)
      .then()
      .statusCode(HttpStatus.SC_OK)
      .extract();

    assertThat(response).isNotNull();
    assertThat(response.body()).isNotNull();
    assertThat(response.body().asString())
      .isNotNull()
      .isNotBlank();

    assertThat(response.body().jsonPath().getString("_links.self.href"))
      .as("assert tags self link")
      .isNotNull()
      .contains(repositoryUrl + "/tags/");

    assertThat(response.body().jsonPath().getList("_embedded.tags"))
      .as("assert tag size")
      .isNotNull()
      .size()
      .isPositive();

    assertThat(response.body().jsonPath().getMap("_embedded.tags.find{it.name=='" + tagName + "'}"))
      .as("assert tag has attributes for name, revision, date and links")
      .isNotNull()
      .hasSize(5)
      .containsEntry("name", tagName)
      .containsEntry("revision", changeset.getId());

    assertThat(response.body().jsonPath().getString("_embedded.tags.find{it.name=='" + tagName + "'}._links.self.href"))
      .as("assert single tag self link")
      .isNotNull()
      .contains(String.format("%s/tags/%s", repositoryUrl, tagName));

    assertThat(response.body().jsonPath().getString("_embedded.tags.find{it.name=='" + tagName + "'}._links.sources.href"))
      .as("assert single tag source link")
      .isNotNull()
      .contains(String.format("%s/sources/%s", repositoryUrl, changeset.getId()));

    assertThat(response.body().jsonPath().getString("_embedded.tags.find{it.name=='" + tagName + "'}._links.changeset.href"))
      .as("assert single tag changesets link")
      .isNotNull()
      .contains(String.format("%s/changesets/%s", repositoryUrl, changeset.getId()));
  }

  @Test
  @SuppressWarnings("squid:S2925")
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
      .path("_embedded.children.find{it.name=='a.txt'}._links.self.href");

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
      .path("_embedded.children.find{it.name=='subfolder'}._links.self.href");
    String selfOfSubfolderUrl = given()
      .when()
      .get(subfolderSourceUrl)
      .then()
      .statusCode(HttpStatus.SC_OK)
      .extract()
      .path("_links.self.href");
    assertThat(subfolderSourceUrl).isEqualTo(selfOfSubfolderUrl);
    String subfolderContentUrl = given()
      .when()
      .get(subfolderSourceUrl)
      .then()
      .statusCode(HttpStatus.SC_OK)
      .extract()
      .path("_embedded.children[0]._links.self.href");
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
    Changeset changeset = RepositoryUtil.createAndCommitFile(repositoryClient, ADMIN_USERNAME, "folder/subfolder/a.txt", "a");
    repositoryResponse
      .requestSources()
      .requestSelf("folder")
      .requestSelf("subfolder")
      .requestFileHistory("a.txt")
      .assertStatusCode(HttpStatus.SC_OK)
      .assertChangesets(changesets -> {
          assertThat(changesets).hasSize(1);
          assertThat(changesets.get(0)).containsEntry("id", changeset.getId());
          assertThat(changesets.get(0)).containsEntry("description", changeset.getDescription());
        }
      );
  }

  @Test
  @SuppressWarnings("unchecked")
  public void shouldFindAddedModifications() throws IOException {
    RepositoryClient repositoryClient = RepositoryUtil.createRepositoryClient(repositoryType, folder);
    String fileName = "a.txt";
    Changeset changeset = RepositoryUtil.createAndCommitFile(repositoryClient, ADMIN_USERNAME, fileName, "a");
    String revision = changeset.getId();
    repositoryResponse
      .requestChangesets()
      .assertStatusCode(HttpStatus.SC_OK)
      .requestModifications(revision)
      .assertStatusCode(HttpStatus.SC_OK)
      .assertRevision(actualRevision -> assertThat(actualRevision).isEqualTo(revision))
      .assertAdded(addedFiles -> assertThat(addedFiles)
        .hasSize(1)
        .containsExactly(fileName))
      .assertRemoved(removedFiles -> assertThat(removedFiles)
        .hasSize(0))
      .assertModified(files -> assertThat(files)
        .hasSize(0));
  }

  @Test
  @SuppressWarnings("unchecked")
  public void shouldFindRemovedModifications() throws IOException {
    RepositoryClient repositoryClient = RepositoryUtil.createRepositoryClient(repositoryType, folder);
    String fileName = "a.txt";
    RepositoryUtil.createAndCommitFile(repositoryClient, ADMIN_USERNAME, fileName, "a");
    Changeset changeset = RepositoryUtil.removeAndCommitFile(repositoryClient, ADMIN_USERNAME, fileName);

    String revision = changeset.getId();
    repositoryResponse
      .requestChangesets()
      .assertStatusCode(HttpStatus.SC_OK)
      .requestModifications(revision)
      .assertStatusCode(HttpStatus.SC_OK)
      .assertRevision(actualRevision -> assertThat(actualRevision).isEqualTo(revision))
      .assertRemoved(removedFiles -> assertThat(removedFiles)
        .hasSize(1)
        .containsExactly(fileName))
      .assertAdded(addedFiles -> assertThat(addedFiles)
        .hasSize(0))
      .assertModified(files -> assertThat(files)
        .hasSize(0));
  }

  @Test
  @SuppressWarnings("unchecked")
  public void shouldFindUpdateModifications() throws IOException {
    RepositoryClient repositoryClient = RepositoryUtil.createRepositoryClient(repositoryType, folder);
    String fileName = "a.txt";
    RepositoryUtil.createAndCommitFile(repositoryClient, ADMIN_USERNAME, fileName, "a");
    Changeset changeset = RepositoryUtil.createAndCommitFile(repositoryClient, ADMIN_USERNAME, fileName, "new Content");

    String revision = changeset.getId();
    repositoryResponse
      .requestChangesets()
      .assertStatusCode(HttpStatus.SC_OK)
      .requestModifications(revision)
      .assertStatusCode(HttpStatus.SC_OK)
      .assertRevision(actualRevision -> assertThat(actualRevision).isEqualTo(revision))
      .assertModified(modifiedFiles -> assertThat(modifiedFiles)
        .hasSize(1)
        .containsExactly(fileName))
      .assertRemoved(removedFiles -> assertThat(removedFiles)
        .hasSize(0))
      .assertAdded(addedFiles -> assertThat(addedFiles)
        .hasSize(0));
  }

  @Test
  @SuppressWarnings("unchecked")
  public void shouldFindMultipleModifications() throws IOException {
    RepositoryClient repositoryClient = RepositoryUtil.createRepositoryClient(repositoryType, folder);
    RepositoryUtil.createAndCommitFile(repositoryClient, ADMIN_USERNAME, "b.txt", "b");
    RepositoryUtil.createAndCommitFile(repositoryClient, ADMIN_USERNAME, "c.txt", "c");
    RepositoryUtil.createAndCommitFile(repositoryClient, ADMIN_USERNAME, "d.txt", "d");
    Map<String, String> addedFiles = new HashMap<String, String>() {{
      put("a.txt", "bla bla");
    }};
    Map<String, String> modifiedFiles = new HashMap<String, String>() {{
      put("b.txt", "new content");
    }};
    ArrayList<String> removedFiles = Lists.newArrayList("c.txt", "d.txt");
    Changeset changeset = RepositoryUtil.commitMultipleFileModifications(repositoryClient, ADMIN_USERNAME, addedFiles, modifiedFiles, removedFiles);

    String revision = changeset.getId();
    repositoryResponse
      .requestChangesets()
      .assertStatusCode(HttpStatus.SC_OK)
      .requestModifications(revision)
      .assertStatusCode(HttpStatus.SC_OK)
      .assertRevision(actualRevision -> assertThat(actualRevision).isEqualTo(revision))
      .assertAdded(a -> assertThat(a)
        .hasSize(1)
        .containsExactly("a.txt"))
      .assertModified(m -> assertThat(m)
        .hasSize(1)
        .containsExactly("b.txt"))
      .assertRemoved(r -> assertThat(r)
        .hasSize(2)
        .containsExactly("c.txt", "d.txt"));
  }

  @Test
  @SuppressWarnings("unchecked")
  public void svnShouldCreateOneModificationPerFolder() throws IOException {
    Assume.assumeThat(repositoryType, equalTo("svn"));
    RepositoryClient repositoryClient = RepositoryUtil.createRepositoryClient(repositoryType, folder);
    RepositoryUtil.createAndCommitFile(repositoryClient, ADMIN_USERNAME, "bbb/bb/b.txt", "b");
    RepositoryUtil.createAndCommitFile(repositoryClient, ADMIN_USERNAME, "ccc/cc/c.txt", "c");
    RepositoryUtil.createAndCommitFile(repositoryClient, ADMIN_USERNAME, "ddd/dd/d.txt", "d");
    Map<String, String> addedFiles = new HashMap<String, String>()
    {{
      put("aaa/aa/a.txt", "bla bla");
    }};
    Map<String, String> modifiedFiles = new HashMap<String, String>()
    {{
      put("bbb/bb/b.txt", "new content");
    }};
    ArrayList<String> removedFiles = Lists.newArrayList("ccc/cc/c.txt", "ddd/dd/d.txt");
    Changeset changeset = RepositoryUtil.commitMultipleFileModifications(repositoryClient, ADMIN_USERNAME, addedFiles, modifiedFiles, removedFiles);

    String revision = changeset.getId();
    repositoryResponse
      .requestChangesets()
      .assertStatusCode(HttpStatus.SC_OK)
      .requestModifications(revision)
      .assertStatusCode(HttpStatus.SC_OK)
      .assertRevision(actualRevision -> assertThat(actualRevision).isEqualTo(revision))
      .assertAdded(a -> assertThat(a)
        .hasSize(3)
        .containsExactly("aaa/aa/a.txt", "aaa", "aaa/aa"))
      .assertModified(m-> assertThat(m)
        .hasSize(1)
        .containsExactly("bbb/bb/b.txt"))
      .assertRemoved(r -> assertThat(r)
        .hasSize(2)
        .containsExactly("ccc/cc/c.txt", "ddd/dd/d.txt"));
  }
}

