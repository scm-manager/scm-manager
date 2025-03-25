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

import java.util.List;

/**
 * The blob store can be used store unstructured data in form of a {@link Blob}.
 *
 * @since 1.23
 */
public interface BlobStore extends MultiEntryStore<Blob>
{

  /**
   * Create a new blob object with automatically generated id.
   *
   *
   * @return new blob
   */
  public Blob create();

  /**
   * Create a new blob object with the given id.
   *
   *
   * @param id id of the new blob
   *
   * @return new blob
   *
   * @throws sonia.scm.store.EntryAlreadyExistsStoreException if a blob with given id already
   *   exists
   */
  public Blob create(String id);

  /**
   * Remove the given blob object-
   *
   *
   * @param blob blob object to remove
   */
  public void remove(Blob blob);


  /**
   * Return all blob object which are stored in this BlobStore.
   *
   *
   * @return a list of all blob object
   */
  public List<Blob> getAll();
}
