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
}
