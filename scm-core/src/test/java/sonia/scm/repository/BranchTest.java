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

package sonia.scm.repository;

import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class BranchTest {

  @ParameterizedTest
  @ValueSource(strings = {
    "a",
    "test",
    "feature/nöice",
    "😄",
    "very_long/and/complex%branch+name"
  })
  void shouldAcceptValidBranchName(String branchName) {
    assertThat(branchName).matches(Branch.VALID_BRANCH_NAME_PATTERN);
  }

  @ParameterizedTest
  @ValueSource(strings = {
    "/",
    "/feature/ugly",
    "./start",
    ".hidden",
    "full_stop.",
    "very/.hidden",
    "some\\place",
    "some//place",
    "some space",
    "home/~",
    "some_:",
    "2^8",
    "real?",
    "find*all",
    "some[set"
  })
  void shouldRejectInvalidBranchName(String branchName) {
    assertThat(branchName).doesNotMatch(Branch.VALID_BRANCH_NAME_PATTERN);
  }
}
