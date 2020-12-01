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
