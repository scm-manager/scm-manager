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
import java.util.OptionalInt;

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
  private Integer total;
  private String branchName;
  private boolean hasMore;

  /**
   * Constructs a new changeset paging result.
   *
   * @param changesets current list of fetched changesets
   */
  public ChangesetPagingResult(List<Changeset> changesets) {
    this(null, changesets, null, false);
  }

  /**
   * Constructs a new changeset paging result.
   *
   * @param total      total number of changesets
   * @param changesets current list of fetched changesets
   */
  public ChangesetPagingResult(int total, List<Changeset> changesets) {
    this(total, changesets, null, false);
  }

  /**
   * Constructs a new changeset paging result for a specific branch.
   *
   * @param total      total number of changesets
   * @param changesets current list of fetched changesets
   * @param branchName branch name this result was created for
   */
  public ChangesetPagingResult(int total, List<Changeset> changesets, String branchName) {
    this(total, changesets, branchName, false);
  }

  public ChangesetPagingResult(Integer total, List<Changeset> changesets, String branchName, boolean hasMore) {
    this.total = total;
    this.changesets = changesets;
    this.branchName = branchName;
    this.hasMore = hasMore;
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

  void setChangesets(List<Changeset> changesets) {
    this.changesets = changesets;
  }

  /**
   * Returns the total number of changesets, if information is available.
   */
  public OptionalInt getTotal() {
    return total == null ? OptionalInt.empty() : OptionalInt.of(total);
  }

  /**
   * Returns the total number of changesets, value might be null.
   */
  public Integer getRawTotal() {
    return total;
  }

  public void setTotal(int total) {
    this.total = total;
  }

  /**
   * Returns whether there is another page of changesets available.
   */
  public boolean isHasMore() {
    return hasMore;
  }

  public void setHasMore(boolean hasMore) {
    this.hasMore = hasMore;
  }

  /**
   * Returns the branch name this result was created for. This can either be an explicit branch ("give me all
   * changesets for branch xyz") or an implicit one ("give me the changesets for the default").
   */
  public String getBranchName() {
    return branchName;
  }

  public void setBranchName(String branchName) {
    this.branchName = branchName;
  }
}
