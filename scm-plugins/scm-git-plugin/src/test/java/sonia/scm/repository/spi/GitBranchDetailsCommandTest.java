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

public class GitBranchDetailsCommandTest extends AbstractGitCommandTestBase {

  @Test
  public void shouldGetZerosForDefaultBranch() {
    GitBranchDetailsCommand command = new GitBranchDetailsCommand(createContext());

    BranchDetailsCommandRequest request = new BranchDetailsCommandRequest();
    request.setBranchName("master");
    BranchDetailsCommandResult result = command.execute(request);

    assertThat(result.getChangesetsAhead()).get().isEqualTo(0);
    assertThat(result.getChangesetsBehind()).get().isEqualTo(0);
  }

  @Test
  public void shouldCountSimpleAheadAndBehind() {
    GitBranchDetailsCommand command = new GitBranchDetailsCommand(createContext());

    BranchDetailsCommandRequest request = new BranchDetailsCommandRequest();
    request.setBranchName("test-branch");
    BranchDetailsCommandResult result = command.execute(request);

    assertThat(result.getChangesetsAhead()).get().isEqualTo(1);
    assertThat(result.getChangesetsBehind()).get().isEqualTo(2);
  }

  @Test
  public void shouldCountMoreComplexAheadAndBehind() {
    GitBranchDetailsCommand command = new GitBranchDetailsCommand(createContext());

    BranchDetailsCommandRequest request = new BranchDetailsCommandRequest();
    request.setBranchName("partially_merged");
    BranchDetailsCommandResult result = command.execute(request);

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
