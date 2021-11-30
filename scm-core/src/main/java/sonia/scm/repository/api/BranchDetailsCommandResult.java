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

import lombok.Value;

/**
 * @since 2.28.0
 */
public class BranchDetailsCommandResult {
  private final int changesetsAhead;
  private final int changesetsBehind;

  /**
   * Creates the result object
   *
   * @param changesetsAhead  The number of changesets this branch is ahead of the default branch (that is
   *                         the number of changesets on this branch that are not reachable from the default branch).
   * @param changesetsBehind The number of changesets the default branch is ahead of this branch (that is
   *                         the number of changesets on the default branch that are not reachable from this branch).
   */
  public BranchDetailsCommandResult(int changesetsAhead, int changesetsBehind) {
    this.changesetsAhead = changesetsAhead;
    this.changesetsBehind = changesetsBehind;
  }

  /**
   * The number of changesets this branch is ahead of the default branch (that is
   * the number of changesets on this branch that are not reachable from the default branch).
   */
  public int getChangesetsAhead() {
    return changesetsAhead;
  }

  /**
   * The number of changesets the default branch is ahead of this branch (that is
   * the number of changesets on the default branch that are not reachable from this branch).
   */
  public int getChangesetsBehind() {
    return changesetsBehind;
  }
}
