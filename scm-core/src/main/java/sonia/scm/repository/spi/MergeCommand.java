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

package sonia.scm.repository.spi;

import sonia.scm.repository.api.MergeCommandResult;
import sonia.scm.repository.api.MergeDryRunCommandResult;
import sonia.scm.repository.api.MergeStrategy;

import java.util.Set;

public interface MergeCommand {
  /**
   * Executes the merge.
   * @param request The parameters specifying the merge.
   * @return Result holding either the new revision or a list of conflicting files.
   * @throws sonia.scm.NoChangesMadeException If the merge neither had a conflict nor made any change.
   */
  MergeCommandResult merge(MergeCommandRequest request);

  MergeDryRunCommandResult dryRun(MergeCommandRequest request);

  MergeConflictResult computeConflicts(MergeCommandRequest request);

  boolean isSupported(MergeStrategy strategy);

  Set<MergeStrategy> getSupportedMergeStrategies();
}
