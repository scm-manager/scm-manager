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
