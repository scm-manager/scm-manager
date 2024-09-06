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

package sonia.scm.repository.api;

import org.junit.jupiter.api.Test;
import sonia.scm.repository.Branch;
import sonia.scm.repository.spi.BranchStaleComputer;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static java.time.Instant.now;
import static org.assertj.core.api.Assertions.assertThat;
import static sonia.scm.repository.Branch.defaultBranch;
import static sonia.scm.repository.Branch.normalBranch;

class BranchXDaysOlderThanDefaultStaleComputerTest {

  Instant now = now();

  BranchXDaysOlderThanDefaultStaleComputer computer = new BranchXDaysOlderThanDefaultStaleComputer(30);

  @Test
  void shouldTagOldBranchAsStale() {
    long staleTime =
      now
        .minus(30, ChronoUnit.DAYS)
        .minus(1, ChronoUnit.MINUTES)
        .toEpochMilli();

    Branch branch = normalBranch("hog", "42", staleTime);
    boolean stale = computer.computeStale(branch, createStaleContext());

    assertThat(stale).isTrue();
  }

  @Test
  void shouldNotTagNotSoOldBranchAsStale() {
    long activeTime =
      now
        .minus(30, ChronoUnit.DAYS)
        .plus(1, ChronoUnit.MINUTES)
        .toEpochMilli();

    Branch branch = normalBranch("hog", "42", activeTime);
    boolean stale = computer.computeStale(branch, createStaleContext());

    assertThat(stale).isFalse();
  }

  @Test
  void shouldNotTagDefaultBranchAsStale() {
    long staleTime =
      now
        .minus(30, ChronoUnit.DAYS)
        .minus(1, ChronoUnit.MINUTES)
        .toEpochMilli();

    Branch branch = defaultBranch("hog", "42", staleTime);
    boolean stale = computer.computeStale(branch, createStaleContext());

    assertThat(stale).isFalse();
  }

  BranchStaleComputer.StaleContext createStaleContext() {
    BranchStaleComputer.StaleContext staleContext = new BranchStaleComputer.StaleContext();
    staleContext.setDefaultBranch(defaultBranch("default", "23", now.toEpochMilli()));
    return staleContext;
  }
}
