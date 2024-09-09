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

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static sonia.scm.repository.Branch.defaultBranch;
import static sonia.scm.repository.Branch.normalBranch;

public class HgBranchesCommandTest extends AbstractHgCommandTestBase {

  @Test
  public void shouldReadBranches() {
    HgBranchesCommand command = new HgBranchesCommand(cmdContext);

    List<Branch> branches = command.getBranches();

    assertThat(branches).hasSize(2);
    assertThat(branches.get(0)).isEqualTo(
      defaultBranch(
        "default",
        "2baab8e80280ef05a9aa76c49c76feca2872afb7",
        1339586381000L,
        new Person("Zaphod Beeblebrox", "zaphod.beeblebrox@hitchhiker.com")
      )
    );
    assertThat(branches.get(1)).isEqualTo(
      normalBranch(
        "test-branch",
        "79b6baf49711ae675568e0698d730b97ef13e84a",
        1339586299000L,
        new Person("Ford Prefect",
          "ford.perfect@hitchhiker.com")
      )
    );
  }
}
