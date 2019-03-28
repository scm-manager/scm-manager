package sonia.scm.repository.spi;

import com.google.inject.util.Providers;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import sonia.scm.repository.Branch;
import sonia.scm.repository.HgTestUtil;
import sonia.scm.repository.api.BranchRequest;
import sonia.scm.web.HgRepositoryEnvironmentBuilder;

import java.io.IOException;
import java.util.List;

public class HgBranchCommandTest extends AbstractHgCommandTestBase {
  @Test
  public void shouldCreateBranch() throws IOException {
    Assertions.assertThat(readBranches()).filteredOn(b -> b.getName().equals("new_branch")).isEmpty();

    HgRepositoryEnvironmentBuilder hgRepositoryEnvironmentBuilder =
      new HgRepositoryEnvironmentBuilder(handler, HgTestUtil.createHookManager());

    SimpleHgWorkdirFactory workdirFactory = new SimpleHgWorkdirFactory(Providers.of(hgRepositoryEnvironmentBuilder), pc -> {});

    BranchRequest branchRequest = new BranchRequest();
    branchRequest.setNewBranch("new_branch");

    new HgBranchCommand(cmdContext, repository, workdirFactory).branch(branchRequest);

    Assertions.assertThat(readBranches()).filteredOn(b -> b.getName().equals("new_branch")).isNotEmpty();
  }

  private List<Branch> readBranches() {
    return new HgBranchesCommand(cmdContext, repository).getBranches();
  }
}
