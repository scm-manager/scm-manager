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
import java.util.HashSet;

import static java.util.Collections.emptyList;
import static java.util.Collections.unmodifiableCollection;

/**
 * This class keeps the result of a merge of branches. Use {@link #isSuccess()} to check whether the merge was
 * sucessfully executed. If the result is <code>false</code> the merge could not be done without conflicts. In this
 * case you can use {@link #getFilesWithConflict()} to get a list of files with merge conflicts.
 */
public class MergeCommandResult {

  private final Collection<String> filesWithConflict;
  private final String newHeadRevision;
  private final String targetRevision;
  private final String revisionToMerge;

  private MergeCommandResult(Collection<String> filesWithConflict, String targetRevision, String revisionToMerge, String newHeadRevision) {
    this.filesWithConflict = filesWithConflict;
    this.targetRevision = targetRevision;
    this.revisionToMerge = revisionToMerge;
    this.newHeadRevision = newHeadRevision;
  }

 public static MergeCommandResult success(String targetRevision, String revisionToMerge, String newHeadRevision) {
    return new MergeCommandResult(emptyList(), targetRevision, revisionToMerge, newHeadRevision);
  }

  public static MergeCommandResult failure(String targetRevision, String revisionToMerge, Collection<String> filesWithConflict) {
    return new MergeCommandResult(new HashSet<>(filesWithConflict), targetRevision, revisionToMerge, null);
  }

  /**
   * If this returns <code>true</code>, the merge was successfull. If this returns <code>false</code> there were
   * merge conflicts. In this case you can use {@link #getFilesWithConflict()} to check what files could not be merged.
   */
  public boolean isSuccess() {
    return filesWithConflict.isEmpty() && newHeadRevision != null;
  }

  /**
   * If the merge was not successful ({@link #isSuccess()} returns <code>false</code>) this will give you a list of
   * file paths that could not be merged automatically.
   */
  public Collection<String> getFilesWithConflict() {
    return unmodifiableCollection(filesWithConflict);
  }

  /**
   * Returns the revision of the new head of the target branch, if the merge was successful ({@link #isSuccess()})
   */
  public String getNewHeadRevision() {
    return newHeadRevision;
  }

  /**
   * Returns the revision of the target branch prior to the merge.
   */
  public String getTargetRevision() {
    return targetRevision;
  }

  /**
   * Returns the revision of the branch that was merged into the target (or in case of a conflict of the revision that
   * should have been merged).
   */
  public String getRevisionToMerge() {
    return revisionToMerge;
  }
}
