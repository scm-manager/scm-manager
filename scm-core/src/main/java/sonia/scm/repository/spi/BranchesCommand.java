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

import sonia.scm.repository.Branch;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

/**
 * @since 1.18
 */
public interface BranchesCommand {

  List<Branch> getBranches() throws IOException;

  default List<Branch> getBranchesWithStaleFlags(BranchStaleComputer computer) throws IOException {
    List<Branch> branches = getBranches();
    new StaleProcessor(computer, branches).process();
    return branches;
  }

  final class StaleProcessor {

    private final BranchStaleComputer computer;
    private final List<Branch> branches;

    private StaleProcessor(BranchStaleComputer computer, List<Branch> branches) {
      this.computer = computer;
      this.branches = branches;
    }

    private void process() {
      Optional<Branch> defaultBranch = branches.stream()
        .filter(Branch::isDefaultBranch)
        .findFirst();

      defaultBranch.ifPresent(this::process);
    }

    private void process(Branch defaultBranch) {
      BranchStaleComputer.StaleContext staleContext = new BranchStaleComputer.StaleContext();
      staleContext.setDefaultBranch(defaultBranch);

      branches.forEach(branch -> branch.setStale(computer.computeStale(branch, staleContext)));
    }
  }
}
