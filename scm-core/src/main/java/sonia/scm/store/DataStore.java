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
