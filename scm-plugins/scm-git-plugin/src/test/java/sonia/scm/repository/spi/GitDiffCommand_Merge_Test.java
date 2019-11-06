package sonia.scm.repository.spi;

import org.junit.Rule;
import org.junit.Test;
import sonia.scm.repository.util.WorkdirProvider;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

public class GitDiffCommand_Merge_Test extends AbstractGitCommandTestBase {

  static final String DIFF_HEADER = "diff --git a/Main.java b/Main.java";
  static final String DIFF_FILE_A_MULTIPLE_REVISIONS = "--- a/Main.java\n" +
    "+++ b/Main.java\n" +
    "@@ -1,5 +1,5 @@\n" +
    " class Main {\n" +
    "-    public static void main(String[] args) {\n" +
    "+    public static void main(String[] arguments) {\n" +
    "         System.out.println(\"Expect nothing more to happen.\");\n" +
    "         System.out.println(\"This is for demonstration, only.\");\n" +
    "     }\n";

  @Rule
  public BindTransportProtocolRule transportProtocolRule = new BindTransportProtocolRule();

  @Test
  public void diffBetweenTwoBranchesWithoutConflict() throws IOException {
    GitDiffCommand gitDiffCommand = new GitDiffCommand(createContext(), repository, new SimpleGitWorkdirFactory(new WorkdirProvider()));
    DiffCommandRequest diffCommandRequest = new DiffCommandRequest();
    diffCommandRequest.setRevision("feature/rename_variable");
    diffCommandRequest.setMergeChangeset("integration");
    ByteArrayOutputStream output = new ByteArrayOutputStream();
    gitDiffCommand.getDiffResult(diffCommandRequest).accept(output);
    assertThat(output.toString())
      .contains(DIFF_HEADER)
      .contains(DIFF_FILE_A_MULTIPLE_REVISIONS);
  }

  @Test
  public void diffBetweenTwoBranchesWithSimpleConflict() throws IOException {
    GitDiffCommand gitDiffCommand = new GitDiffCommand(createContext(), repository, new SimpleGitWorkdirFactory(new WorkdirProvider()));
    DiffCommandRequest diffCommandRequest = new DiffCommandRequest();
    diffCommandRequest.setRevision("feature/print_args");
    diffCommandRequest.setMergeChangeset("integration");
    ByteArrayOutputStream output = new ByteArrayOutputStream();
    gitDiffCommand.getDiffResult(diffCommandRequest).accept(output);
    assertThat(output.toString())
      .contains(DIFF_HEADER)
      .contains(DIFF_FILE_A_MULTIPLE_REVISIONS);
  }

  @Override
  protected String getZippedRepositoryResource() {
    return "sonia/scm/repository/spi/scm-git-spi-merge-diff-test.zip";
  }
}
