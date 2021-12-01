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
import sonia.scm.repository.Person;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static sonia.scm.repository.Branch.defaultBranch;
import static sonia.scm.repository.Branch.normalBranch;

public class GitBranchesCommandTest extends AbstractGitCommandTestBase {

  @Test
  public void shouldReadBranches() throws IOException {
    GitBranchesCommand branchesCommand = new GitBranchesCommand(createContext());

    List<Branch> branches = branchesCommand.getBranches();

    assertThat(findBranch(branches, "master")).isEqualTo(
      defaultBranch(
        "master",
        "fcd0ef1831e4002ac43ea539f4094334c79ea9ec",
        1339428655000L,
        new Person("Zaphod Beeblebrox", "zaphod.beeblebrox@hitchhiker.com")
      )
    );
    assertThat(findBranch(branches, "mergeable")).isEqualTo(
      normalBranch(
        "mergeable",
        "91b99de908fcd04772798a31c308a64aea1a5523",
        1541586052000L,
        new Person("Douglas Adams",
          "douglas.adams@hitchhiker.com")
      )
    );
    assertThat(findBranch(branches, "rename")).isEqualTo(
      normalBranch(
        "rename",
        "383b954b27e052db6880d57f1c860dc208795247",
        1589203061000L,
        new Person("scmadmin",
          "scm@admin.com")
      )
    );
  }

  private Branch findBranch(List<Branch> branches, String mergeable) {
    return branches.stream().filter(b -> b.getName().equals(mergeable)).findFirst().get();
  }
}
