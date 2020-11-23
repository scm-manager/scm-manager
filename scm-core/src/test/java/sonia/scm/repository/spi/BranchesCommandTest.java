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

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import sonia.scm.repository.Branch;

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
    }.getBranchesWithStaleFlags();

    Assertions.assertThat(branchesWithStaleFlags)
      .extracting("stale")
      .containsExactly(true, true, false, false);
  }
}
