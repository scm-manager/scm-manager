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
