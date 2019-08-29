package sonia.scm.repository.spi;

import org.assertj.core.api.Assertions;
import org.junit.Rule;
import org.junit.Test;
import sonia.scm.repository.Branch;
import sonia.scm.repository.api.BranchRequest;
import sonia.scm.repository.util.WorkdirProvider;

import java.io.IOException;
import java.util.List;


public class GitBranchCommandTest extends AbstractGitCommandTestBase {

  @Rule
  public BindTransportProtocolRule transportProtocolRule = new BindTransportProtocolRule();

  @Test
  public void shouldCreateBranchWithDefinedSourceBranch() throws IOException {
    GitContext context = createContext();

    Branch source = findBranch(context, "mergeable");

    BranchRequest branchRequest = new BranchRequest();
    branchRequest.setParentBranch(source.getName());
    branchRequest.setNewBranch("new_branch");

    new GitBranchCommand(context, repository, new SimpleGitWorkdirFactory(new WorkdirProvider())).branch(branchRequest);

    Branch newBranch = findBranch(context, "new_branch");
    Assertions.assertThat(newBranch.getRevision()).isEqualTo(source.getRevision());
  }

  private Branch findBranch(GitContext context, String name) throws IOException {
    List<Branch> branches = readBranches(context);
    return branches.stream().filter(b -> name.equals(b.getName())).findFirst().get();
  }

  @Test
  public void shouldCreateBranch() throws IOException {
    GitContext context = createContext();

    Assertions.assertThat(readBranches(context)).filteredOn(b -> b.getName().equals("new_branch")).isEmpty();

    BranchRequest branchRequest = new BranchRequest();
    branchRequest.setNewBranch("new_branch");

    new GitBranchCommand(context, repository, new SimpleGitWorkdirFactory(new WorkdirProvider())).branch(branchRequest);

    Assertions.assertThat(readBranches(context)).filteredOn(b -> b.getName().equals("new_branch")).isNotEmpty();
  }

  private List<Branch> readBranches(GitContext context) throws IOException {
    return new GitBranchesCommand(context, repository).getBranches();
  }
}
