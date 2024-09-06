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

package sonia.scm;

import java.util.Collection;

/**
 * Generic data access object. A DAO does not check the permission of the
 * current user. A DAO should only be used by a {@link Manager} class.
 *
 * @since 1.14
 *
 * @param <T> type of object
 */
public interface GenericDAO<T>
        extends LastModifiedAware, CreationTimeAware, TypedObject
{

  /**
   * Persists a new item.
   *
   * @param item item to persist
   */
  public void add(T item);

  /**
   * Returns true if the item already exists in the backend.
   *
   * @param item item to check
   */
  public boolean contains(T item);

  /**
   * Returns true if the item with the specified id
   * already exists in the backend.
   *
   * @param id id of the item to check
   */
  public boolean contains(String id);

  /**
   * Updates an existing item.
   *
   * @param item item to update
   */
  public void modify(T item);

  /**
   * Removes the specified item from the backend.
   *
   * @param item item to remove
   */
  public void delete(T item);


  /**
   * Returns the item by its id or returns null if no item with
   * the specified id exists in the backend.
   *
   */
  public T get(String id);

  /**
   * Returns all items stored in the backend.
   */
  public Collection<T> getAll();

}
