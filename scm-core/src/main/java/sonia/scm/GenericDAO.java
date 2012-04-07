/**
 * Copyright (c) 2010, Sebastian Sdorra
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 3. Neither the name of SCM-Manager; nor the names of its
 *    contributors may be used to endorse or promote products derived from this
 *    software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * http://bitbucket.org/sdorra/scm-manager
 *
 */


package sonia.scm;

//~--- JDK imports ------------------------------------------------------------

import java.util.Collection;

/**
 * Generic data access object. A DAO does not check the permission of the
 * current user. A DAO should only used by a {@link Manager} class.
 *
 * @author Sebastian Sdorra
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
   *
   * @param item item to persist
   */
  public void add(T item);

  /**
   * Returns true if the item already exists in the backend.
   *
   *
   * @param item item to check
   *
   * @return true if the item already exists
   */
  public boolean contains(T item);

  /**
   * Returns true if the item with the specified id
   * already exists in the backend.
   *
   *
   * @param id id of the item to check
   *
   * @return true if the item already exists
   */
  public boolean contains(String id);

  /**
   * Updates an existing item.
   *
   *
   * @param item item to update
   */
  public void modify(T item);

  /**
   * Removes the specified item from the backend.
   *
   *
   * @param item item to remove
   */
  public void delete(T item);

  //~--- get methods ----------------------------------------------------------

  /**
   * Returns the item by its id or returns null if no item with
   * the specified id exists in the backend.
   *
   *
   * @param id id of the item
   *
   * @return item with the specified id or null
   */
  public T get(String id);

  /**
   * Returns all items stored in the backend.
   *
   *
   * @return all items
   */
  public Collection<T> getAll();
}
