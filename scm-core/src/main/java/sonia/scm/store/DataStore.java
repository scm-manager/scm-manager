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

import java.util.Map;

/**
 * A DataStore can be used to store any structured data. Note: the default 
 * implementation use JAXB to marshall the items.
 *
 * @since 1.23
 *
 * @param <T> type of store items
 */
public interface DataStore<T> extends MultiEntryStore<T> {

  /**
   * Put a item with automatically generated id to the store.
   *
   *
   * @param item item to store
   *
   * @return automatically generated id of the item
   */
  public String put(T item);

  /**
   * Put the item with the given id to the store.
   *
   *
   * @param id id of the item
   * @param item item to store
   */
  public void put(String id, T item);


  /**
   * Returns a map of all stored items. The key of the map is the item id and 
   * the value is item.
   *
   *
   * @return map of all stored items
   */
  public Map<String, T> getAll();
}
