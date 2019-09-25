package sonia.scm.repository.spi;

import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

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

  @Test
  public void diffForOneRevisionShouldCreateDiff() throws IOException {
    GitDiffCommand gitDiffCommand = new GitDiffCommand(createContext(), repository);
    DiffCommandRequest diffCommandRequest = new DiffCommandRequest();
    diffCommandRequest.setRevision("3f76a12f08a6ba0dc988c68b7f0b2cd190efc3c4");
    ByteArrayOutputStream output = new ByteArrayOutputStream();
    gitDiffCommand.getDiffResult(diffCommandRequest);
    assertEquals(DIFF_FILE_A + DIFF_FILE_B, output.toString());
  }

  @Test
  public void diffForOneBranchShouldCreateDiff() throws IOException {
    GitDiffCommand gitDiffCommand = new GitDiffCommand(createContext(), repository);
    DiffCommandRequest diffCommandRequest = new DiffCommandRequest();
    diffCommandRequest.setRevision("test-branch");
    ByteArrayOutputStream output = new ByteArrayOutputStream();
    gitDiffCommand.getDiffResult(diffCommandRequest);
    assertEquals(DIFF_FILE_A + DIFF_FILE_B, output.toString());
  }

  @Test
  public void diffForPathShouldCreateLimitedDiff() throws IOException {
    GitDiffCommand gitDiffCommand = new GitDiffCommand(createContext(), repository);
    DiffCommandRequest diffCommandRequest = new DiffCommandRequest();
    diffCommandRequest.setRevision("test-branch");
    diffCommandRequest.setPath("a.txt");
    ByteArrayOutputStream output = new ByteArrayOutputStream();
    gitDiffCommand.getDiffResult(diffCommandRequest);
    assertEquals(DIFF_FILE_A, output.toString());
  }

  @Test
  public void diffBetweenTwoBranchesShouldCreateDiff() throws IOException {
    GitDiffCommand gitDiffCommand = new GitDiffCommand(createContext(), repository);
    DiffCommandRequest diffCommandRequest = new DiffCommandRequest();
    diffCommandRequest.setRevision("master");
    diffCommandRequest.setAncestorChangeset("test-branch");
    ByteArrayOutputStream output = new ByteArrayOutputStream();
    gitDiffCommand.getDiffResult(diffCommandRequest);
    assertEquals(DIFF_FILE_A_MULTIPLE_REVISIONS + DIFF_FILE_F_MULTIPLE_REVISIONS, output.toString());
  }

  @Test
  public void diffBetweenTwoBranchesForPathShouldCreateLimitedDiff() throws IOException {
    GitDiffCommand gitDiffCommand = new GitDiffCommand(createContext(), repository);
    DiffCommandRequest diffCommandRequest = new DiffCommandRequest();
    diffCommandRequest.setRevision("master");
    diffCommandRequest.setAncestorChangeset("test-branch");
    diffCommandRequest.setPath("a.txt");
    ByteArrayOutputStream output = new ByteArrayOutputStream();
    gitDiffCommand.getDiffResult(diffCommandRequest);
    assertEquals(DIFF_FILE_A_MULTIPLE_REVISIONS, output.toString());
  }
}
