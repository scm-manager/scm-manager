/*
 * Copyright (c) 2020 - present Cloudogu GmbH
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

package sonia.scm.repository.spi;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.javahg.commands.PullCommand;
import org.junit.Before;
import org.junit.Test;
import sonia.scm.repository.Branch;
import sonia.scm.repository.InternalRepositoryException;
import sonia.scm.repository.api.BranchRequest;
import sonia.scm.repository.work.NoneCachingWorkingCopyPool;
import sonia.scm.repository.work.WorkdirProvider;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class HgBranchCommandTest extends AbstractHgCommandTestBase {

  private SimpleHgWorkingCopyFactory workingCopyFactory;

  @Before
  public void initWorkingCopyFactory() {

    workingCopyFactory = new SimpleHgWorkingCopyFactory(new NoneCachingWorkingCopyPool(new WorkdirProvider(null, repositoryLocationResolver)), new SimpleMeterRegistry()) {
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

    Branch newBranch = new HgBranchCommand(cmdContext, workingCopyFactory).branch(branchRequest);

    assertThat(readBranches()).filteredOn(b -> b.getName().equals("new_branch")).isNotEmpty();
    assertThat(cmdContext.open().changeset(newBranch.getRevision()).getParent1().getBranch()).isEqualTo("default");
  }

  @Test
  public void shouldCreateBranchOnSpecificParent() {
    BranchRequest branchRequest = new BranchRequest();
    branchRequest.setParentBranch("test-branch");
    branchRequest.setNewBranch("new_branch");

    Branch newBranch = new HgBranchCommand(cmdContext, workingCopyFactory).branch(branchRequest);

    assertThat(readBranches()).filteredOn(b -> b.getName().equals("new_branch")).isNotEmpty();
    assertThat(cmdContext.open().changeset(newBranch.getRevision()).getParent1().getBranch()).isEqualTo("test-branch");
  }

  @Test
  public void shouldCloseBranch() {
    String branchToBeClosed = "test-branch";

    new HgBranchCommand(cmdContext, workingCopyFactory).deleteOrClose(branchToBeClosed);
    assertThat(readBranches()).filteredOn(b -> b.getName().equals(branchToBeClosed)).isEmpty();
  }

  @Test
  public void shouldThrowInternalRepositoryException() {
    String branchToBeClosed = "default";

    HgBranchCommand hgBranchCommand = new HgBranchCommand(cmdContext, workingCopyFactory);
    hgBranchCommand.deleteOrClose(branchToBeClosed);
    assertThrows(InternalRepositoryException.class, () -> hgBranchCommand.deleteOrClose(branchToBeClosed));
  }

  private List<Branch> readBranches() {
    return new HgBranchesCommand(cmdContext).getBranches();
  }
}
