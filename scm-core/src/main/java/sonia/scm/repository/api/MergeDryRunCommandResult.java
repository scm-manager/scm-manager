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

import java.util.Collection;
import java.util.List;

/**
 * This class keeps the result of a merge dry run. Use {@link #isMergeable()} to check whether an automatic merge is
 * possible or not.
 */
public class MergeDryRunCommandResult {

  private final boolean mergeable;
  private final Collection<MergePreventReason> reasons;

  /**
   * Creates a result and sets the reason to FILE_CONFLICTS.
   *
   * @deprecated Please use {@link MergeDryRunCommandResult#MergeDryRunCommandResult(boolean, Collection)}
   * instead and specify a concrete reason.
   */
  @Deprecated
  public MergeDryRunCommandResult(boolean mergeable) {
    this(mergeable, List.of(new MergePreventReason(MergePreventReasonType.FILE_CONFLICTS)));
  }

  /**
   * @since 3.3.0
   */
  public MergeDryRunCommandResult(boolean mergeable, Collection<MergePreventReason> reasons) {
    this.mergeable = mergeable;
    this.reasons = reasons;
  }

  /**
   * This will return <code>true</code>, when an automatic merge is possible <em>at the moment</em>; <code>false</code>
   * otherwise.
   */
  public boolean isMergeable() {
    return mergeable;
  }

  /**
   * This will return the reasons why the merge via the internal merge command is not possible.
   */
  public Collection<MergePreventReason> getReasons() {
    return reasons;
  }
}
