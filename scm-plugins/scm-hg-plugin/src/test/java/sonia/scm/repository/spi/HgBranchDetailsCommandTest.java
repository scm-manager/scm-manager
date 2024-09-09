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

import org.junit.Test;
import sonia.scm.NotFoundException;
import sonia.scm.repository.BranchDetails;

import static org.assertj.core.api.Assertions.assertThat;

public class HgBranchDetailsCommandTest extends AbstractHgCommandTestBase {

  @Test
  public void shouldGetSingleBranchDetails() {
    BranchDetailsCommandRequest branchRequest = new BranchDetailsCommandRequest();
    branchRequest.setBranchName("testbranch");

    BranchDetails result = new HgBranchDetailsCommand(cmdContext).execute(branchRequest).getDetails();

    assertThat(result.getChangesetsAhead()).get().isEqualTo(1);
    assertThat(result.getChangesetsBehind()).get().isEqualTo(3);
  }

  @Test
  public void shouldGetSingleBranchDetailsWithMerge() {
    BranchDetailsCommandRequest branchRequest = new BranchDetailsCommandRequest();
    branchRequest.setBranchName("with_merge");

    BranchDetails result = new HgBranchDetailsCommand(cmdContext).execute(branchRequest).getDetails();

    assertThat(result.getChangesetsAhead()).get().isEqualTo(5);
    assertThat(result.getChangesetsBehind()).get().isEqualTo(1);
  }

  @Test
  public void shouldGetSingleBranchDetailsWithAnotherMerge() {
    BranchDetailsCommandRequest branchRequest = new BranchDetailsCommandRequest();
    branchRequest.setBranchName("next_merge");

    BranchDetails result = new HgBranchDetailsCommand(cmdContext).execute(branchRequest).getDetails();

    assertThat(result.getChangesetsAhead()).get().isEqualTo(3);
    assertThat(result.getChangesetsBehind()).get().isEqualTo(0);
  }

  @Test(expected = NotFoundException.class)
  public void shouldThrowNotFoundExceptionForUnknownBranch() {
    BranchDetailsCommandRequest branchRequest = new BranchDetailsCommandRequest();
    branchRequest.setBranchName("no-such-branch");

    new HgBranchDetailsCommand(cmdContext).execute(branchRequest);
  }

  @Override
  protected String getZippedRepositoryResource() {
    return "sonia/scm/repository/spi/scm-hg-ahead-behind-test.zip";
  }
}
