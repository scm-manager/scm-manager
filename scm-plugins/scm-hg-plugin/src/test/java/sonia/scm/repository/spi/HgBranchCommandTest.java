/*
 * MIT License
 *
 * Copyright (c) 2020-present Cloudogu GmbH and Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
    
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
