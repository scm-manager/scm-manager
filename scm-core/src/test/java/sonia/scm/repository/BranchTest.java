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
