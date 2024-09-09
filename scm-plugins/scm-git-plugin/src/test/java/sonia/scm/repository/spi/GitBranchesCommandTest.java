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
