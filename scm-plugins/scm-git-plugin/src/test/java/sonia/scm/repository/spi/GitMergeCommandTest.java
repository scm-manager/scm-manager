package sonia.scm.repository.spi;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import sonia.scm.repository.api.MergeCommandResult;

import static org.assertj.core.api.Assertions.assertThat;


public class GitMergeCommandTest extends AbstractGitCommandTestBase {

  @Test
  public void shouldDetectMergeableBranches() {
    GitMergeCommand command = createCommand();
    MergeCommandRequest request = new MergeCommandRequest();
    request.setBranchToMerge("mergeable");
    request.setTargetBranch("master");

    boolean mergeable = command.dryRun(request).isMergeable();

    assertThat(mergeable).isTrue();
  }

  @Test
  public void shouldDetectNotMergeableBranches() {
    GitMergeCommand command = createCommand();
    MergeCommandRequest request = new MergeCommandRequest();
    request.setBranchToMerge("test-branch");
    request.setTargetBranch("master");

    boolean mergeable = command.dryRun(request).isMergeable();

    assertThat(mergeable).isFalse();
  }

  @Test
  public void shouldMergeMergeableBranches() {
    GitMergeCommand command = createCommand();
    MergeCommandRequest request = new MergeCommandRequest();
    request.setTargetBranch("master");
    request.setBranchToMerge("mergeable");

    MergeCommandResult mergeCommandResult = command.merge(request);

    assertThat(mergeCommandResult.isSuccess()).isTrue();
  }

  @Test
  public void shouldNotMergeConflictingBranches() {
    GitMergeCommand command = createCommand();
    MergeCommandRequest request = new MergeCommandRequest();
    request.setBranchToMerge("test-branch");
    request.setTargetBranch("master");

    MergeCommandResult mergeCommandResult = command.merge(request);

    assertThat(mergeCommandResult.isSuccess()).isFalse();
    assertThat(mergeCommandResult.getFilesWithConflict()).containsExactly("a.txt");
  }

  private GitMergeCommand createCommand() {
    return new GitMergeCommand(createContext(), repository, new SimpleGitWorkdirFactory());
  }
}
