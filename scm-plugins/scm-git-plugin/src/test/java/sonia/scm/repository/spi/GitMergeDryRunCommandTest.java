package sonia.scm.repository.spi;

import org.junit.Assert;
import org.junit.Test;

public class GitMergeDryRunCommandTest extends AbstractGitCommandTestBase {
  @Test
  public void shouldDetectNotMergeableBranches() {
    GitMergeDryRunCommand command = createCommand();
    MergeDryRunCommandRequest request = new MergeDryRunCommandRequest();
    request.setBranchToMerge("test-branch");
    request.setTargetBranch("master");

    boolean mergeable = command.isMergeable(request);

    Assert.assertFalse(mergeable);
  }

  private GitMergeDryRunCommand createCommand()
  {
    return new GitMergeDryRunCommand(createContext(), repository);
  }
}
