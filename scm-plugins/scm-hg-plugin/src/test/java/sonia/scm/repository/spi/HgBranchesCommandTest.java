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

    assertThat(branches).contains(
      defaultBranch(eq("default"), eq("2baab8e80280ef05a9aa76c49c76feca2872afb7"), eq(1339586381000L), argThat(person -> {
        assertThat(person.getName()).isEqualTo("");
        assertThat(person.getMail()).isEqualTo("");
        return true;
      })),
      normalBranch(eq("test-branch"), ("79b6baf49711ae675568e0698d730b97ef13e84a"), eq(1339586299000L), argThat(person -> {
        assertThat(person.getName()).isEqualTo("");
        assertThat(person.getMail()).isEqualTo("");

        return true;
      }))
    );
  }
}
