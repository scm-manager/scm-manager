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

import sonia.scm.repository.Branch;
import sonia.scm.repository.spi.BranchStaleComputer;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static java.time.Instant.ofEpochMilli;

public class BranchXDaysOlderThanDefaultStaleComputer implements BranchStaleComputer {

  public static final int DEFAULT_AMOUNT_OF_DAYS = 30;

  private final int amountOfDays;

  public BranchXDaysOlderThanDefaultStaleComputer() {
    this(DEFAULT_AMOUNT_OF_DAYS);
  }

  public BranchXDaysOlderThanDefaultStaleComputer(int amountOfDays) {
    this.amountOfDays = amountOfDays;
  }

  @Override
  @SuppressWarnings("java:S3655") // we check "isPresent" for both dates, but due to the third check sonar does not get it
  public boolean computeStale(Branch branch, StaleContext context) {
    Branch defaultBranch = context.getDefaultBranch();
    if (shouldCompute(branch, defaultBranch)) {
      Instant defaultCommitDate = ofEpochMilli(defaultBranch.getLastCommitDate().get());
      Instant thisCommitDate = ofEpochMilli(branch.getLastCommitDate().get());
      return thisCommitDate.plus(amountOfDays, ChronoUnit.DAYS).isBefore(defaultCommitDate);
    } else {
      return false;
    }
  }

  public boolean shouldCompute(Branch branch, Branch defaultBranch) {
    return !branch.isDefaultBranch() && branch.getLastCommitDate().isPresent() && defaultBranch.getLastCommitDate().isPresent();
  }
}
