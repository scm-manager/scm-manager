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
import sonia.scm.repository.api.BranchDetailsCommandResult;

import static org.assertj.core.api.Assertions.assertThat;

public class GitBranchDetailsCommandTest extends AbstractGitCommandTestBase {

  @Test
  public void shouldGetZerosForDefaultBranch() {
    GitBranchDetailsCommand command = new GitBranchDetailsCommand(createContext());

    BranchDetailsCommandRequest request = new BranchDetailsCommandRequest();
    request.setBranchName("master");
    BranchDetails result = command.execute(request).getDetails();

    assertThat(result.getChangesetsAhead()).get().isEqualTo(0);
    assertThat(result.getChangesetsBehind()).get().isEqualTo(0);
  }

  @Test
  public void shouldCountSimpleAheadAndBehind() {
    GitBranchDetailsCommand command = new GitBranchDetailsCommand(createContext());

    BranchDetailsCommandRequest request = new BranchDetailsCommandRequest();
    request.setBranchName("test-branch");
    BranchDetails result = command.execute(request).getDetails();

    assertThat(result.getChangesetsAhead()).get().isEqualTo(1);
    assertThat(result.getChangesetsBehind()).get().isEqualTo(2);
  }

  @Test
  public void shouldCountMoreComplexAheadAndBehind() {
    GitBranchDetailsCommand command = new GitBranchDetailsCommand(createContext());

    BranchDetailsCommandRequest request = new BranchDetailsCommandRequest();
    request.setBranchName("partially_merged");
    BranchDetails result = command.execute(request).getDetails();

    assertThat(result.getChangesetsAhead()).get().isEqualTo(3);
    assertThat(result.getChangesetsBehind()).get().isEqualTo(1);
  }

  @Test(expected = NotFoundException.class)
  public void shouldThrowNotFoundExceptionForUnknownBranch() {
    GitBranchDetailsCommand command = new GitBranchDetailsCommand(createContext());

    BranchDetailsCommandRequest request = new BranchDetailsCommandRequest();
    request.setBranchName("no-such-branch");
    command.execute(request);
  }
}
