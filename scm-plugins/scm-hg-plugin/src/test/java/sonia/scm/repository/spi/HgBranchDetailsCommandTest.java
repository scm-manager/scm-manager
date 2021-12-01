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

import org.junit.Test;
import sonia.scm.NotFoundException;
import sonia.scm.repository.api.BranchDetailsCommandResult;

import static org.assertj.core.api.Assertions.assertThat;

public class HgBranchDetailsCommandTest extends AbstractHgCommandTestBase {

  @Test
  public void shouldGetSingleBranchDetails() {
    BranchDetailsCommandRequest branchRequest = new BranchDetailsCommandRequest();
    branchRequest.setBranchName("testbranch");

    BranchDetailsCommandResult result = new HgBranchDetailsCommand(cmdContext).execute(branchRequest);

    assertThat(result.getChangesetsAhead()).get().isEqualTo(1);
    assertThat(result.getChangesetsBehind()).get().isEqualTo(3);
  }

  @Test
  public void shouldGetSingleBranchDetailsWithMerge() {
    BranchDetailsCommandRequest branchRequest = new BranchDetailsCommandRequest();
    branchRequest.setBranchName("with_merge");

    BranchDetailsCommandResult result = new HgBranchDetailsCommand(cmdContext).execute(branchRequest);

    assertThat(result.getChangesetsAhead()).get().isEqualTo(5);
    assertThat(result.getChangesetsBehind()).get().isEqualTo(1);
  }

  @Test
  public void shouldGetSingleBranchDetailsWithAnotherMerge() {
    BranchDetailsCommandRequest branchRequest = new BranchDetailsCommandRequest();
    branchRequest.setBranchName("next_merge");

    BranchDetailsCommandResult result = new HgBranchDetailsCommand(cmdContext).execute(branchRequest);

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
