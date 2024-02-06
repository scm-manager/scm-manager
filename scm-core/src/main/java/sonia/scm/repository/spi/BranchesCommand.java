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
