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

package sonia.scm.store;

import java.util.Optional;

import static java.util.Optional.ofNullable;

/**
 * Base class for {@link BlobStore} and {@link DataStore}.
 *
 * @since 1.23
 *
 * @param <T> Type of the stored objects
 */
public interface MultiEntryStore<T> {

  /**
   * Remove all items from the store.
   *
   */
  public void clear();

  /**
   * Remove the item with the given id.
   *
   *
   * @param id id of the item to remove
   */
  public void remove(String id);


  /**
   * Returns the item with the given id from the store.
   *
   *
   * @param id id of the item to return
   *
   * @return item with the given id
   */
  public T get(String id);

  /**
   * Returns the item with the given id from the store.
   *
   *
   * @param id id of the item to return
   *
   * @return item with the given id
   */
  default Optional<T> getOptional(String id) {
    return ofNullable(get(id));
  }
}
