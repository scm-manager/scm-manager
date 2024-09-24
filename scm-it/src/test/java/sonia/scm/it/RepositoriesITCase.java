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


import org.apache.http.HttpStatus;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import sonia.scm.it.utils.RepositoryUtil;
import sonia.scm.it.utils.TestData;
import sonia.scm.repository.client.api.RepositoryClient;
import sonia.scm.web.VndMediaType;

import java.io.IOException;
import java.util.Collection;
import java.util.Objects;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertEquals;
import static sonia.scm.it.utils.RegExMatcher.matchesPattern;
import static sonia.scm.it.utils.RestUtil.createResourceUrl;
import static sonia.scm.it.utils.RestUtil.given;
import static sonia.scm.it.utils.ScmTypes.availableScmTypes;
import static sonia.scm.it.utils.TestData.repositoryJson;

@RunWith(Parameterized.class)
public class RepositoriesITCase {

  @Rule
  public TemporaryFolder temporaryFolder = new TemporaryFolder();

  private final String repositoryType;

  private String repositoryUrl;

  public RepositoriesITCase(String repositoryType) {
    this.repositoryType = repositoryType;
    this.repositoryUrl = TestData.getDefaultRepositoryUrl(repositoryType);
  }

  @Parameters(name = "{0}")
  public static Collection<String> createParameters() {
    return availableScmTypes();
  }

  @Before
  public void createRepository() {
    TestData.createDefault();
  }

  @Test
  public void shouldCreateSuccessfully() {
    given(VndMediaType.REPOSITORY)

      .when()
      .get(repositoryUrl)

      .then()
      .statusCode(HttpStatus.SC_OK)
      .body(
        "name", equalTo("HeartOfGold-" + repositoryType),
        "type", equalTo(repositoryType),
        "creationDate", matchesPattern("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}(?:\\.\\d+)?Z"),
        "lastModified", is(nullValue()),
        "_links.self.href", equalTo(repositoryUrl)
      );
  }

  @Test
  public void shouldDeleteSuccessfully() {
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
  public void shouldRejectMultipleCreations() {
    String repositoryJson = repositoryJson(repositoryType);
    given(VndMediaType.REPOSITORY)
      .body(repositoryJson)

      .when()
      .post(createResourceUrl("repositories"))

      .then()
      .statusCode(HttpStatus.SC_CONFLICT);
  }

  @Test
  public void shouldCloneRepository() throws IOException {
    RepositoryClient client = RepositoryUtil.createRepositoryClient(repositoryType, temporaryFolder.getRoot());
    assertEquals("expected metadata dir", 1, Objects.requireNonNull(client.getWorkingCopy().list()).length);
  }

  @Test
  public void shouldCommitFiles() throws IOException {
    RepositoryClient client = RepositoryUtil.createRepositoryClient(repositoryType, temporaryFolder.newFolder(), "scmadmin", "scmadmin");
    String name = RepositoryUtil.addAndCommitRandomFile(client, "scmadmin");
    RepositoryClient checkClient = RepositoryUtil.createRepositoryClient(repositoryType, temporaryFolder.newFolder(), "scmadmin", "scmadmin");
    Assertions.assertThat(checkClient.getWorkingCopy().list()).contains(name);
  }

}
