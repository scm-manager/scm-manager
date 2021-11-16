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
import sonia.scm.repository.Branch;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static sonia.scm.repository.Branch.defaultBranch;
import static sonia.scm.repository.Branch.normalBranch;

public class HgBranchesCommandTest extends AbstractHgCommandTestBase {

  @Test
  public void shouldReadBranches() {
    HgBranchesCommand command = new HgBranchesCommand(cmdContext);

    List<Branch> branches = command.getBranches();

    assertThat(branches).contains(
      defaultBranch("default", "b991188f44ae452922f658d9f109c9b8ee3a67ae", 1637058574000L),
      normalBranch("test-branch", "79b6baf49711ae675568e0698d730b97ef13e84a", 1339586299000L)
    );
  }
}
