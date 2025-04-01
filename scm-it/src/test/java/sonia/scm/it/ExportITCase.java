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

import org.junit.Before;
import org.junit.Test;
import sonia.scm.it.utils.ScmRequests;
import sonia.scm.it.utils.TestData;

import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static sonia.scm.it.utils.RestUtil.ADMIN_PASSWORD;
import static sonia.scm.it.utils.RestUtil.ADMIN_USERNAME;

public class ExportITCase {

  @Before
  public void init() {
    TestData.cleanup();
  }

  @Test
  public void shouldExportAndImportRepository() {
    String namespace = ADMIN_USERNAME;
    TestData.createDefault();
    String repo = TestData.getDefaultRepoName("git");

    ScmRequests.start()
      .requestIndexResource(ADMIN_USERNAME, ADMIN_PASSWORD)
      .requestRepository(namespace, repo)
      .writeTestData("value");

    Path exportFile = ScmRequests.start()
      .requestIndexResource(ADMIN_USERNAME, ADMIN_PASSWORD)
      .requestRepository(namespace, repo)
      .requestFullExport()
      .exportFile();

    ScmRequests.start()
      .requestIndexResource(ADMIN_USERNAME, ADMIN_PASSWORD)
      .requestRepositoryType("git")
      .requestImport("fullImport", exportFile, "imported");

    List<String> importedTestData = ScmRequests.start()
      .requestIndexResource(ADMIN_USERNAME, ADMIN_PASSWORD)
      .requestRepository(namespace, "imported")
      .requestTestData()
      .getTestData();

    assertThat(importedTestData).containsExactly("value");
  }
}
