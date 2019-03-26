package sonia.scm.repository.spi;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import sonia.scm.repository.Branch;

import java.util.List;

public class HgBranchCommandTest extends AbstractHgCommandTestBase {
  @Test
  public void x() {
    Assertions.assertThat(readBranches()).filteredOn(b -> b.getName().equals("new_branch")).isEmpty();

    new HgBranchCommand(cmdContext, repository).branch("new_branch");

    Assertions.assertThat(readBranches()).filteredOn(b -> b.getName().equals("new_branch")).isNotEmpty();
  }

  private List<Branch> readBranches() {
    return new HgBranchesCommand(cmdContext, repository).getBranches();
  }
}
