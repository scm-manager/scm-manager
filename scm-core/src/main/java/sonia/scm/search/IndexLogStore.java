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

import java.util.Optional;

/**
 * Can be used to mark when a type of object was last indexed and with which version.
 * This is useful to detect and mark if a bootstrap index was created for the kind of object
 * or if the way how an object is indexed has changed.
 *
 * @since 2.21.0
 */
@Beta
public interface IndexLogStore {

  /**
   * Returns an index log store for the given index.
   * @param index name of index
   * @return index log store for given index
   * @since 2.23.0
   */
  ForIndex forIndex(String index);

  /**
   * Returns the index log store for the default index.
   * @return index log store for default index
   * @since 2.23.0
   */
  ForIndex defaultIndex();

  /**
   * Index log store for a specific index.
   * @since 2.23.0
   */
  interface ForIndex {

    /**
     * Log index and version of a type which is now indexed.
     *
     * @param type type which was indexed
     * @param version model version
     */
    void log(Class<?> type, int version);

    /**
     * Returns version and date of the indexed type or an empty object,
     * if the object was not indexed at all.
     *
     * @param type type of object
     *
     * @return log entry or empty
     */
    Optional<IndexLog> get(Class<?> type);
  }
}
