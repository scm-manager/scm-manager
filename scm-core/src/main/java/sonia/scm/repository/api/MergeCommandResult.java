/**
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
