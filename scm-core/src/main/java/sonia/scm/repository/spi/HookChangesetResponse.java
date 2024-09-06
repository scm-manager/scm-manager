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

import sonia.scm.repository.Changeset;

import static java.util.Collections.emptyList;

/**
 * Response object to retrieve {@link Changeset}s during a hook.
 *
 * @since 1.33
 */
public final class HookChangesetResponse {
  private final Iterable<Changeset> addedChangesets;
  private final Iterable<Changeset> removedChangesets;

  public HookChangesetResponse(Iterable<Changeset> addedChangesets, Iterable<Changeset> removedChangesets) {
    this.addedChangesets = addedChangesets;
    this.removedChangesets = removedChangesets;
  }

  public HookChangesetResponse(Iterable<Changeset> changesets) {
    this(changesets, emptyList());
  }

  /**
   * Return added changesets.
   */
  public Iterable<Changeset> getChangesets() {
    return addedChangesets;
  }

  /**
   * Return removed changesets.
   *
   *  @since 2.39.0
   */
  public Iterable<Changeset> getRemovedChangesets() {
    return removedChangesets;
  }

}
