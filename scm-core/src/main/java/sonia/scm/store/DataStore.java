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
   * Put an item into the store. If the item has an attribute that is
   * annotated with {@link Id}, then the value from this field will
   * be taken as an id if it is not null. Otherwise, a new id will be
   * generated and used.
   *
   * @param item item to store
   *
   * @return automatically generated id of the item
   */
  String put(T item);

  /**
   * Put the item with the given id to the store.
   * If the item has an attribute that is annotated with {@link Id},
   * then this field will be set to the given id.
   *
   * @param id id of the item
   * @param item item to store
   */
  void put(String id, T item);


  /**
   * Returns a map of all stored items. The key of the map is the item id and 
   * the value is item.
   *
   * @return map of all stored items
   */
  Map<String, T> getAll();
}
