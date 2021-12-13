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

package sonia.scm.repository;

import java.util.Optional;

import static java.util.Optional.ofNullable;

/**
 * Represents details of a branch that are not computed by default for a {@link Branch}.
 *
 * @since 2.28.0
 */
public class BranchDetails {

  private final String branchName;
  private final Integer changesetsAhead;
  private final Integer changesetsBehind;

  /**
   * Create the details object without further details.
   *
   * @param branchName The name of the branch these details are for.
   */
  public BranchDetails(String branchName) {
    this(branchName, null, null);
  }

  /**
   * Creates the details object with ahead/behind counts.
   *
   * @param branchName             The name of the branch these details are for.
   * @param changesetsAhead  The number of changesets this branch is ahead of the default branch (that is
   *                         the number of changesets on this branch that are not reachable from the default branch).
   * @param changesetsBehind The number of changesets the default branch is ahead of this branch (that is
   */
  public BranchDetails(String branchName, Integer changesetsAhead, Integer changesetsBehind) {
    this.branchName = branchName;
    this.changesetsAhead = changesetsAhead;
    this.changesetsBehind = changesetsBehind;
  }

  /**
   * The name of the branch these details are for.
   */
  public String getBranchName() {
    return branchName;
  }

  /**
   * The number of changesets this branch is ahead of the default branch (that is
   * the number of changesets on this branch that are not reachable from the default branch).
   */
  public Optional<Integer> getChangesetsAhead() {
    return ofNullable(changesetsAhead);
  }

  /**
   * The number of changesets the default branch is ahead of this branch (that is
   * the number of changesets on the default branch that are not reachable from this branch).
   */
  public Optional<Integer> getChangesetsBehind() {
    return ofNullable(changesetsBehind);
  }
}
