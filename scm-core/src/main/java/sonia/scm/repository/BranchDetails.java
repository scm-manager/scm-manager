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
