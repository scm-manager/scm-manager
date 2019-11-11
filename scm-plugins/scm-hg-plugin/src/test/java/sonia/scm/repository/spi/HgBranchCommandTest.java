package sonia.scm.repository.spi;

import com.aragost.javahg.commands.PullCommand;
import com.google.inject.util.Providers;
import org.junit.Before;
import org.junit.Test;
import sonia.scm.repository.Branch;
import sonia.scm.repository.HgTestUtil;
import sonia.scm.repository.InternalRepositoryException;
import sonia.scm.repository.api.BranchRequest;
import sonia.scm.repository.util.WorkdirProvider;
import sonia.scm.web.HgRepositoryEnvironmentBuilder;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class HgBranchCommandTest extends AbstractHgCommandTestBase {

  private SimpleHgWorkdirFactory workdirFactory;

  @Before
  public void initWorkdirFactory() {
    HgRepositoryEnvironmentBuilder hgRepositoryEnvironmentBuilder =
      new HgRepositoryEnvironmentBuilder(handler, HgTestUtil.createHookManager());

    workdirFactory = new SimpleHgWorkdirFactory(Providers.of(hgRepositoryEnvironmentBuilder), new WorkdirProvider()) {
      @Override
      public void configure(PullCommand pullCommand) {
        // we do not want to configure http hooks in this unit test
      }
    };
  }

  @Test
  public void shouldCreateBranch() {
    BranchRequest branchRequest = new BranchRequest();
    branchRequest.setNewBranch("new_branch");

    Branch newBranch = new HgBranchCommand(cmdContext, repository, workdirFactory).branch(branchRequest);

    assertThat(readBranches()).filteredOn(b -> b.getName().equals("new_branch")).isNotEmpty();
    assertThat(cmdContext.open().changeset(newBranch.getRevision()).getParent1().getBranch()).isEqualTo("default");
  }

  @Test
  public void shouldCreateBranchOnSpecificParent() {
    BranchRequest branchRequest = new BranchRequest();
    branchRequest.setParentBranch("test-branch");
    branchRequest.setNewBranch("new_branch");

    Branch newBranch = new HgBranchCommand(cmdContext, repository, workdirFactory).branch(branchRequest);

    assertThat(readBranches()).filteredOn(b -> b.getName().equals("new_branch")).isNotEmpty();
    assertThat(cmdContext.open().changeset(newBranch.getRevision()).getParent1().getBranch()).isEqualTo("test-branch");
  }

  @Test
  public void shouldCloseBranch() {
    String branchToBeClosed = "test-branch";

    new HgBranchCommand(cmdContext, repository, workdirFactory).deleteOrClose(branchToBeClosed);
    assertThat(readBranches()).filteredOn(b -> b.getName().equals(branchToBeClosed)).isEmpty();
  }

  @Test
  public void shouldThrowInternalRepositoryException() {
    String branchToBeClosed = "default";

    new HgBranchCommand(cmdContext, repository, workdirFactory).deleteOrClose(branchToBeClosed);
    assertThrows(InternalRepositoryException.class, () -> new HgBranchCommand(cmdContext, repository, workdirFactory).deleteOrClose(branchToBeClosed));
  }

  private List<Branch> readBranches() {
    return new HgBranchesCommand(cmdContext, repository).getBranches();
  }
}
