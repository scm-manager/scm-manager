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

package sonia.scm.api.v2.resources;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static sonia.scm.repository.Branch.VALID_BRANCH_NAMES;

class BranchDtoTest {

  @ParameterizedTest
  @ValueSource(strings = {
    "v",
    "1234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890",
    "val#x",
    "val&x",
    "val+",
    "val,kill",
    "val.kill",
    "val;kill",
    "val<kill",
    "val=",
    "val>kill",
    "val@",
    "val]id",
    "val`id",
    "valid#",
    "valid.t",
    "val{",
    "val{d",
    "val{}d",
    "val|kill",
    "val}",
    "va/li/d"
  })
  void shouldAcceptValidBranchName(String branchName) {
    assertTrue(branchName.matches(VALID_BRANCH_NAMES));
  }

  @ParameterizedTest
  @ValueSource(strings = {
    "",
    ".val",
    "val.",
    "/val",
    "val/",
    "val id"
  })
  void shouldRejectInvalidBranchName(String branchName) {
    assertFalse(branchName.matches(VALID_BRANCH_NAMES));
  }
}
