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

import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;

/**
 * The changeset paging result is used to do a paging over the
 * {@link Changeset}s of a {@link Repository}.
 *
 */
@EqualsAndHashCode
@ToString
public class ChangesetPagingResult implements Iterable<Changeset>, Serializable {

  private static final long serialVersionUID = -8678755403658841733L;

  private List<Changeset> changesets;
  private int total;
  private String branchName;

  /**
   * Constructs a new changeset paging result.
   *
   * @param total      total number of changesets
   * @param changesets current list of fetched changesets
   */
  public ChangesetPagingResult(int total, List<Changeset> changesets) {
    this.total = total;
    this.changesets = changesets;
    this.branchName = null;
  }

  /**
   * Constructs a new changeset paging result for a specific branch.
   *
   * @param total      total number of changesets
   * @param changesets current list of fetched changesets
   * @param branchName branch name this result was created for
   */
  public ChangesetPagingResult(int total, List<Changeset> changesets, String branchName) {
    this.total = total;
    this.changesets = changesets;
    this.branchName = branchName;
  }

  /**
   * Returns an iterator which can iterate over the current list of changesets.
   *
   * @since 1.8
   */
  @Override
  public Iterator<Changeset> iterator() {
    Iterator<Changeset> it = null;

    if (changesets != null) {
      it = changesets.iterator();
    }

    return it;
  }

  /**
   * Returns the current list of changesets.
   */
  public List<Changeset> getChangesets() {
    return changesets;
  }

  /**
   * Returns the total number of changesets.
   */
  public int getTotal() {
    return total;
  }

  /**
   * Returns the branch name this result was created for. This can either be an explicit branch ("give me all
   * changesets for branch xyz") or an implicit one ("give me the changesets for the default").
   */
  public String getBranchName() {
    return branchName;
  }

  void setChangesets(List<Changeset> changesets)
  {
    this.changesets = changesets;
  }

  void setTotal(int total)
  {
    this.total = total;
  }

  void setBranchName(String branchName) {
    this.branchName = branchName;
  }

}
