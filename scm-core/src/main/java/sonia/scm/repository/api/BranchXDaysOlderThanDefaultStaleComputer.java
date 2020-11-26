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
