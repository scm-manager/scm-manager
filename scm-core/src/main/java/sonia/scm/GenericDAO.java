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
