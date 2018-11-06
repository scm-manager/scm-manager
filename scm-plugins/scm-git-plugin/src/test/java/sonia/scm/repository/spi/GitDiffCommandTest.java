package sonia.scm.repository.spi;

import org.junit.Test;

import java.io.ByteArrayOutputStream;

import static org.junit.Assert.assertEquals;

public class GitDiffCommandTest extends AbstractGitCommandTestBase {

  private static final String DIFF_LATEST_COMMIT_TEST_BRANCH = "diff --git a/a.txt b/a.txt\n" +
    "index 7898192..1dc60c7 100644\n" +
    "--- a/a.txt\n" +
    "+++ b/a.txt\n" +
    "@@ -1 +1 @@\n" +
    "-a\n" +
    "+a and b\n" +
    "diff --git a/b.txt b/b.txt\n" +
    "deleted file mode 100644\n" +
    "index 6178079..0000000\n" +
    "--- a/b.txt\n" +
    "+++ /dev/null\n" +
    "@@ -1 +0,0 @@\n" +
    "-b\n";

  @Test
  public void diffForOneRevisionShouldCreateDiff() {
    GitDiffCommand gitDiffCommand = new GitDiffCommand(createContext(), repository);
    DiffCommandRequest diffCommandRequest = new DiffCommandRequest();
    diffCommandRequest.setRevision("3f76a12f08a6ba0dc988c68b7f0b2cd190efc3c4");
    ByteArrayOutputStream output = new ByteArrayOutputStream();
    gitDiffCommand.getDiffResult(diffCommandRequest, output);
    assertEquals(DIFF_LATEST_COMMIT_TEST_BRANCH, output.toString());
  }

  @Test
  public void diffForOneBranchShouldCreateDiff() {
    GitDiffCommand gitDiffCommand = new GitDiffCommand(createContext(), repository);
    DiffCommandRequest diffCommandRequest = new DiffCommandRequest();
    diffCommandRequest.setRevision("test-branch");
    ByteArrayOutputStream output = new ByteArrayOutputStream();
    gitDiffCommand.getDiffResult(diffCommandRequest, output);
    assertEquals(DIFF_LATEST_COMMIT_TEST_BRANCH, output.toString());
  }

  @Test
  public void diffForPathShouldCreateLimitedDiff() {
    GitDiffCommand gitDiffCommand = new GitDiffCommand(createContext(), repository);
    DiffCommandRequest diffCommandRequest = new DiffCommandRequest();
    diffCommandRequest.setRevision("test-branch");
    diffCommandRequest.setPath("a.txt");
    ByteArrayOutputStream output = new ByteArrayOutputStream();
    gitDiffCommand.getDiffResult(diffCommandRequest, output);
    assertEquals("diff --git a/a.txt b/a.txt\n" +
      "index 7898192..1dc60c7 100644\n" +
      "--- a/a.txt\n" +
      "+++ b/a.txt\n" +
      "@@ -1 +1 @@\n" +
      "-a\n" +
      "+a and b\n", output.toString());
  }

  @Test
  public void diffBetweenTwoBranchesShouldCreateDiff() {
    GitDiffCommand gitDiffCommand = new GitDiffCommand(createContext(), repository);
    DiffCommandRequest diffCommandRequest = new DiffCommandRequest();
    diffCommandRequest.setRevision("master");
    diffCommandRequest.setAncestorChangeset("test-branch");
    ByteArrayOutputStream output = new ByteArrayOutputStream();
    gitDiffCommand.getDiffResult(diffCommandRequest, output);
    assertEquals("diff --git a/a.txt b/a.txt\n" +
      "index 7898192..2f8bc28 100644\n" +
      "--- a/a.txt\n" +
      "+++ b/a.txt\n" +
      "@@ -1 +1,2 @@\n" +
      " a\n" +
      "+line for blame\n" +
      "diff --git a/f.txt b/f.txt\n" +
      "new file mode 100644\n" +
      "index 0000000..6a69f92\n" +
      "--- /dev/null\n" +
      "+++ b/f.txt\n" +
      "@@ -0,0 +1 @@\n" +
      "+f\n", output.toString());
  }

  @Test
  public void diffBetweenTwoBranchesForPathShouldCreateLimitedDiff() {
    GitDiffCommand gitDiffCommand = new GitDiffCommand(createContext(), repository);
    DiffCommandRequest diffCommandRequest = new DiffCommandRequest();
    diffCommandRequest.setRevision("master");
    diffCommandRequest.setAncestorChangeset("test-branch");
    diffCommandRequest.setPath("a.txt");
    ByteArrayOutputStream output = new ByteArrayOutputStream();
    gitDiffCommand.getDiffResult(diffCommandRequest, output);
    assertEquals("diff --git a/a.txt b/a.txt\n" +
      "index 7898192..2f8bc28 100644\n" +
      "--- a/a.txt\n" +
      "+++ b/a.txt\n" +
      "@@ -1 +1,2 @@\n" +
      " a\n" +
      "+line for blame\n", output.toString());
  }
}
