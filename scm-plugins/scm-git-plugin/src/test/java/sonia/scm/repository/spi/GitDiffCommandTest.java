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
import sonia.scm.repository.api.IgnoreWhitespaceLevel;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

public class GitDiffCommandTest extends AbstractGitCommandTestBase {

  public static final String DIFF_FILE_A = "diff --git a/a.txt b/a.txt\n" +
    "index 7898192..1dc60c7 100644\n" +
    "--- a/a.txt\n" +
    "+++ b/a.txt\n" +
    "@@ -1 +1 @@\n" +
    "-a\n" +
    "+a and b\n";
  public static final String DIFF_FILE_B = "diff --git a/b.txt b/b.txt\n" +
    "deleted file mode 100644\n" +
    "index 6178079..0000000\n" +
    "--- a/b.txt\n" +
    "+++ /dev/null\n" +
    "@@ -1 +0,0 @@\n" +
    "-b\n";
  public static final String DIFF_FILE_A_MULTIPLE_REVISIONS = "diff --git a/a.txt b/a.txt\n" +
    "index 7898192..2f8bc28 100644\n" +
    "--- a/a.txt\n" +
    "+++ b/a.txt\n" +
    "@@ -1 +1,2 @@\n" +
    " a\n" +
    "+line for blame\n";
  public static final String DIFF_FILE_F_MULTIPLE_REVISIONS = "diff --git a/f.txt b/f.txt\n" +
    "new file mode 100644\n" +
    "index 0000000..6a69f92\n" +
    "--- /dev/null\n" +
    "+++ b/f.txt\n" +
    "@@ -0,0 +1 @@\n" +
    "+f\n";
  public static final String DIFF_FILE_PARTIAL_MERGE = "diff --git a/a.txt b/a.txt\n" +
    "index 7898192..8cd63ec 100644\n" +
    "--- a/a.txt\n" +
    "+++ b/a.txt\n" +
    "@@ -1 +1,2 @@\n" +
    " a\n" +
    "+change\n" +
    "diff --git a/b.txt b/b.txt\n" +
    "index 6178079..09ccdf0 100644\n" +
    "--- a/b.txt\n" +
    "+++ b/b.txt\n" +
    "@@ -1 +1,2 @@\n" +
    " b\n" +
    "+change\n";

  public static final String DIFF_IGNORE_WHITESPACE = "diff --git a/a.txt b/a.txt\n" +
    "index 2f8bc28..fc3f0ba 100644\n" +
    "--- a/a.txt\n" +
    "+++ b/a.txt\n";

  public static final String DIFF_WITH_WHITESPACE = "diff --git a/a.txt b/a.txt\n" +
    "index 2f8bc28..fc3f0ba 100644\n" +
    "--- a/a.txt\n" +
    "+++ b/a.txt\n" +
    "@@ -1,2 +1,2 @@\n" +
    " a\n" +
    "-line for blame\n" +
    "+line                          for blame\n";

  @Test
  public void diffForOneRevisionShouldCreateDiff() throws IOException {
    GitDiffCommand gitDiffCommand = new GitDiffCommand(createContext());
    DiffCommandRequest diffCommandRequest = new DiffCommandRequest();
    diffCommandRequest.setRevision("3f76a12f08a6ba0dc988c68b7f0b2cd190efc3c4");
    ByteArrayOutputStream output = new ByteArrayOutputStream();
    gitDiffCommand.getDiffResult(diffCommandRequest).accept(output);
    assertEquals(DIFF_FILE_A + DIFF_FILE_B, output.toString());
  }

  @Test
  public void diffForOneBranchShouldCreateDiff() throws IOException {
    GitDiffCommand gitDiffCommand = new GitDiffCommand(createContext());
    DiffCommandRequest diffCommandRequest = new DiffCommandRequest();
    diffCommandRequest.setRevision("test-branch");
    ByteArrayOutputStream output = new ByteArrayOutputStream();
    gitDiffCommand.getDiffResult(diffCommandRequest).accept(output);
    assertEquals(DIFF_FILE_A + DIFF_FILE_B, output.toString());
  }

  @Test
  public void shouldIgnoreWhiteSpace() throws IOException {
    GitDiffCommand gitDiffCommand = new GitDiffCommand(createContext());
    DiffCommandRequest diffCommandRequest = new DiffCommandRequest();
    diffCommandRequest.setIgnoreWhitespaceLevel(IgnoreWhitespaceLevel.ALL);
    diffCommandRequest.setRevision("whitespace");
    ByteArrayOutputStream output = new ByteArrayOutputStream();
    gitDiffCommand.getDiffResult(diffCommandRequest).accept(output);
    assertEquals(DIFF_IGNORE_WHITESPACE, output.toString());
  }

  @Test
  public void shouldNotIgnoreWhiteSpace() throws IOException {
    GitDiffCommand gitDiffCommand = new GitDiffCommand(createContext());
    DiffCommandRequest diffCommandRequest = new DiffCommandRequest();
    diffCommandRequest.setIgnoreWhitespaceLevel(IgnoreWhitespaceLevel.NONE);
    diffCommandRequest.setRevision("whitespace");
    ByteArrayOutputStream output = new ByteArrayOutputStream();
    gitDiffCommand.getDiffResult(diffCommandRequest).accept(output);
    assertEquals(DIFF_WITH_WHITESPACE, output.toString());
  }

  @Test
  public void diffForPathShouldCreateLimitedDiff() throws IOException {
    GitDiffCommand gitDiffCommand = new GitDiffCommand(createContext());
    DiffCommandRequest diffCommandRequest = new DiffCommandRequest();
    diffCommandRequest.setRevision("test-branch");
    diffCommandRequest.setPath("a.txt");
    ByteArrayOutputStream output = new ByteArrayOutputStream();
    gitDiffCommand.getDiffResult(diffCommandRequest).accept(output);
    assertEquals(DIFF_FILE_A, output.toString());
  }

  @Test
  public void diffBetweenTwoBranchesShouldCreateDiff() throws IOException {
    GitDiffCommand gitDiffCommand = new GitDiffCommand(createContext());
    DiffCommandRequest diffCommandRequest = new DiffCommandRequest();
    diffCommandRequest.setRevision("master");
    diffCommandRequest.setAncestorChangeset("test-branch");
    ByteArrayOutputStream output = new ByteArrayOutputStream();
    gitDiffCommand.getDiffResult(diffCommandRequest).accept(output);
    assertEquals(DIFF_FILE_A_MULTIPLE_REVISIONS + DIFF_FILE_F_MULTIPLE_REVISIONS, output.toString());
  }

  @Test
  public void diffBetweenTwoBranchesForPathShouldCreateLimitedDiff() throws IOException {
    GitDiffCommand gitDiffCommand = new GitDiffCommand(createContext());
    DiffCommandRequest diffCommandRequest = new DiffCommandRequest();
    diffCommandRequest.setRevision("master");
    diffCommandRequest.setAncestorChangeset("test-branch");
    diffCommandRequest.setPath("a.txt");
    ByteArrayOutputStream output = new ByteArrayOutputStream();
    gitDiffCommand.getDiffResult(diffCommandRequest).accept(output);
    assertEquals(DIFF_FILE_A_MULTIPLE_REVISIONS, output.toString());
  }

  @Test
  public void diffBetweenTwoBranchesWithMergedIntegrationBranchShouldCreateDiffOfAllIncomingChanges() throws IOException {
    GitDiffCommand gitDiffCommand = new GitDiffCommand(createContext());
    DiffCommandRequest diffCommandRequest = new DiffCommandRequest();
    diffCommandRequest.setRevision("partially_merged");
    diffCommandRequest.setAncestorChangeset("master");
    ByteArrayOutputStream output = new ByteArrayOutputStream();
    gitDiffCommand.getDiffResult(diffCommandRequest).accept(output);
    assertEquals(DIFF_FILE_PARTIAL_MERGE, output.toString());
  }

  @Test
  public void diffBetweenTwoBranchesWithMovedFiles() throws IOException {
    GitDiffCommand gitDiffCommand = new GitDiffCommand(createContext());
    DiffCommandRequest diffCommandRequest = new DiffCommandRequest();
    diffCommandRequest.setRevision("rename");
    ByteArrayOutputStream output = new ByteArrayOutputStream();
    gitDiffCommand.getDiffResult(diffCommandRequest).accept(output);
    assertThat(output.toString())
      .contains("similarity index 100%")
      .contains("rename from a.txt")
      .contains("rename to a-copy.txt")
      .contains("rename from b.txt")
      .contains("rename to b-copy.txt");
  }

  @Override
  protected String getZippedRepositoryResource() {
    return "sonia/scm/repository/spi/scm-git-spi-whitespace-test.zip";
  }
}
