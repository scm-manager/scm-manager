package sonia.scm.repository.spi;

import org.junit.Assert;
import org.junit.Test;

public class GitMergeCommandTest extends AbstractGitCommandTestBase {
  @Test
  public void shouldDetectNotMergeableBranches() {
    GitMergeCommand command = createCommand();
    MergeCommandRequest request = new MergeCommandRequest();
    request.setBranchToMerge("test-branch");
    request.setTargetBranch("master");

    boolean mergeable = command.dryRun(request).isMergeable();

    Assert.assertFalse(mergeable);
  }

  private GitMergeCommand createCommand() {
    return new GitMergeCommand(createContext(), repository, null);
  }
}
