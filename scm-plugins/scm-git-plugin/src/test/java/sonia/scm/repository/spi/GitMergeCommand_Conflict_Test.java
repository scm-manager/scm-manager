package sonia.scm.repository.spi;

import org.junit.Rule;
import org.junit.Test;
import sonia.scm.repository.spi.MergeConflictResult.SingleMergeConflict;
import sonia.scm.repository.util.WorkdirProvider;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static sonia.scm.repository.spi.MergeConflictResult.ConflictTypes.BOTH_MODIFIED;
import static sonia.scm.repository.spi.MergeConflictResult.ConflictTypes.DELETED_BY_THEM;
import static sonia.scm.repository.spi.MergeConflictResult.ConflictTypes.DELETED_BY_US;

public class GitMergeCommand_Conflict_Test extends AbstractGitCommandTestBase {

  static final String DIFF_HEADER = "diff --git a/Main.java b/Main.java";
  static final String DIFF_FILE_CONFLICT = "--- a/Main.java\n" +
    "+++ b/Main.java\n" +
    "@@ -1,6 +1,13 @@\n" +
    "+import java.util.Arrays;\n" +
    "+\n" +
    " class Main {\n" +
    "     public static void main(String[] args) {\n" +
    "         System.out.println(\"Expect nothing more to happen.\");\n" +
    "+<<<<<<< HEAD\n" +
    "         System.out.println(\"This is for demonstration, only.\");\n" +
    "+=======\n" +
    "+        System.out.println(\"Parameters:\");\n" +
    "+        Arrays.stream(args).map(arg -> \"- \" + arg).forEach(System.out::println);\n" +
    "+>>>>>>> feature/print_args\n" +
    "     }\n" +
    " }\n";

  @Rule
  public BindTransportProtocolRule transportProtocolRule = new BindTransportProtocolRule();

  @Test
  public void diffBetweenTwoBranchesWithoutConflict() throws IOException {
    MergeConflictResult result = computeMergeConflictResult("feature/rename_variable", "integration");
    assertThat(result.getConflicts()).isEmpty();
  }

  @Test
  public void diffBetweenTwoBranchesWithSimpleConflict() throws IOException {
    MergeConflictResult result = computeMergeConflictResult("feature/print_args", "integration");
    SingleMergeConflict conflict = result.getConflicts().get(0);
    assertThat(conflict.getType()).isEqualTo(BOTH_MODIFIED);
    assertThat(conflict.getPath()).isEqualTo("Main.java");
    assertThat(conflict.getDiff()).contains(DIFF_HEADER, DIFF_FILE_CONFLICT);
  }

  @Test
  public void diffBetweenTwoBranchesWithDeletedByThem() throws IOException {
    MergeConflictResult result = computeMergeConflictResult("feature/remove_class", "integration");
    SingleMergeConflict conflict = result.getConflicts().get(0);
    assertThat(conflict.getType()).isEqualTo(DELETED_BY_THEM);
    assertThat(conflict.getPath()).isEqualTo("Main.java");
  }

  @Test
  public void diffBetweenTwoBranchesWithDeletedByUs() throws IOException {
    MergeConflictResult result = computeMergeConflictResult("integration", "feature/remove_class");
    SingleMergeConflict conflict = result.getConflicts().get(0);
    assertThat(conflict.getType()).isEqualTo(DELETED_BY_US);
    assertThat(conflict.getPath()).isEqualTo("Main.java");
  }

  private MergeConflictResult computeMergeConflictResult(String branchToMerge, String targetBranch) {
    GitMergeCommand gitMergeCommand = new GitMergeCommand(createContext(), repository, new SimpleGitWorkdirFactory(new WorkdirProvider()));
    MergeCommandRequest mergeCommandRequest = new MergeCommandRequest();
    mergeCommandRequest.setBranchToMerge(branchToMerge);
    mergeCommandRequest.setTargetBranch(targetBranch);
    return gitMergeCommand.computeConflicts(mergeCommandRequest);
  }

  @Override
  protected String getZippedRepositoryResource() {
    return "sonia/scm/repository/spi/scm-git-spi-merge-diff-test.zip";
  }
}
