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

package sonia.scm.search;

import com.google.common.annotations.Beta;

/**
 * A task which updates an index.
 * @param <T> type of indexed objects
 * @since 2.23.0
 */
@Beta
@FunctionalInterface
public interface IndexTask<T> {

  /**
   * Execute operations on the index.
   */
  void update(Index<T> index);

  /**
   * This method is called after work is committed to the index.
   */
  default void afterUpdate() {
// Do nothing
  }

}
