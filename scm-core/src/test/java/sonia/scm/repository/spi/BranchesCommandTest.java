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

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import sonia.scm.repository.Branch;
import sonia.scm.repository.api.BranchXDaysOlderThanDefaultStaleComputer;

import java.io.IOException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static java.time.Instant.now;
import static java.util.Arrays.asList;

class BranchesCommandTest {

  @Test
  void shouldMarkEachBranchDependingOnDefaultBranch() throws IOException {
    Instant now = now();
    long staleTime =
      now
        .minus(30, ChronoUnit.DAYS)
        .minus(1, ChronoUnit.MINUTES)
        .toEpochMilli();
    long activeTime =
      now
        .minus(30, ChronoUnit.DAYS)
        .plus(1, ChronoUnit.MINUTES)
        .toEpochMilli();

    List<Branch> branches = asList(
      Branch.normalBranch("arthur", "42", staleTime),
      Branch.normalBranch("marvin", "42", staleTime),
      Branch.defaultBranch("hog", "42", now.toEpochMilli()),
      Branch.normalBranch("trillian", "42", activeTime)
    );

    List<Branch> branchesWithStaleFlags = new BranchesCommand() {
      @Override
      public List<Branch> getBranches() {
        return branches;
      }
    }.getBranchesWithStaleFlags(new BranchXDaysOlderThanDefaultStaleComputer());

    Assertions.assertThat(branchesWithStaleFlags)
      .extracting("stale")
      .containsExactly(true, true, false, false);
  }
}
