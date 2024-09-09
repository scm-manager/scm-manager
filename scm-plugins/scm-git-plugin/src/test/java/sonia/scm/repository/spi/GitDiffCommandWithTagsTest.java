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

package sonia.scm.repository.spi;

import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class GitDiffCommandWithTagsTest extends AbstractGitCommandTestBase {

  @Test
  public void diffBetweenTwoTagsShouldCreateDiff() throws IOException {
    GitDiffCommand gitDiffCommand = new GitDiffCommand(createContext());
    DiffCommandRequest diffCommandRequest = new DiffCommandRequest();
    diffCommandRequest.setRevision("1.0.0");
    diffCommandRequest.setAncestorChangeset("test-tag");
    ByteArrayOutputStream output = new ByteArrayOutputStream();
    gitDiffCommand.getDiffResult(diffCommandRequest).accept(output);
    assertEquals("diff --git a/a.txt b/a.txt\n" +
      "index 7898192..2f8bc28 100644\n" +
      "--- a/a.txt\n" +
      "+++ b/a.txt\n" +
      "@@ -1 +1,2 @@\n" +
      " a\n" +
      "+line for blame\n", output.toString());
  }

  @Test
  public void diffBetweenTagAndBranchShouldCreateDiff() throws IOException {
    GitDiffCommand gitDiffCommand = new GitDiffCommand(createContext());
    DiffCommandRequest diffCommandRequest = new DiffCommandRequest();
    diffCommandRequest.setRevision("master");
    diffCommandRequest.setAncestorChangeset("test-tag");
    ByteArrayOutputStream output = new ByteArrayOutputStream();
    gitDiffCommand.getDiffResult(diffCommandRequest).accept(output);
    assertEquals("diff --git a/a.txt b/a.txt\n" +
      "index 7898192..2f8bc28 100644\n" +
      "--- a/a.txt\n" +
      "+++ b/a.txt\n" +
      "@@ -1 +1,2 @@\n" +
      " a\n" +
      "+line for blame\n", output.toString());
  }

  @Override
  protected String getZippedRepositoryResource() {
    return "sonia/scm/repository/spi/scm-git-spi-test-tags.zip";
  }
}
