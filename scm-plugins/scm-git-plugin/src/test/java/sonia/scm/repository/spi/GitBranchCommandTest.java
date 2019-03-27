package sonia.scm.repository.spi;

import org.assertj.core.api.Assertions;
import org.junit.Rule;
import org.junit.Test;
import sonia.scm.repository.Branch;

import java.io.IOException;
import java.util.List;


public class GitBranchCommandTest extends AbstractGitCommandTestBase {

  @Rule
  public BindTransportProtocolRule transportProtocolRule = new BindTransportProtocolRule();

  @Test
  public void shouldCreateBranch() throws IOException {
    GitContext context = createContext();

    Assertions.assertThat(readBranches(context)).filteredOn(b -> b.getName().equals("new_branch")).isEmpty();

    new GitBranchCommand(context, repository, new SimpleGitWorkdirFactory()).branch("new_branch");

    Assertions.assertThat(readBranches(context)).filteredOn(b -> b.getName().equals("new_branch")).isNotEmpty();
  }

  private List<Branch> readBranches(GitContext context) throws IOException {
    return new GitBranchesCommand(context, repository).getBranches();
  }
}
