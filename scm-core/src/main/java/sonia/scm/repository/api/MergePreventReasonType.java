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

/**
 * @since 3.3.0
 */
public enum MergePreventReasonType {
  /**
   File conflicts are the typical reason why merges are not possible. Common examples are:
   - File added on both branches
   - File modified on one branch but deleted on the other
   - File modified on both branches
   Reference: {@link sonia.scm.repository.spi.MergeConflictResult.ConflictTypes}
   */
  FILE_CONFLICTS,
  /**
   * It is also possible that we cannot perform a merge properly because files would be affected which have to be merged by an external merge tool.
   * For git these merge tools are configured in the .gitattributes file inside the repository.
   */
  EXTERNAL_MERGE_TOOL
}
