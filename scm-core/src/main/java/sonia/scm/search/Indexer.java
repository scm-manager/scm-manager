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

package sonia.scm.search;

import sonia.scm.plugin.ExtensionPoint;

/**
 * Indexer for a specific type.
 * Note: An implementation of the indexer creates only a bootstrap index.
 * To keep the index in sync you have to implement a subscriber, see {@link HandlerEvents}.
 *
 * @param <T> type to index
 * @since 2.22.0
 * @see HandlerEvents
 */
@ExtensionPoint
public interface Indexer<T> {

  /**
   * Returns class of type.
   *
   * @return class of type
   */
  Class<T> getType();

  /**
   * Returns name of index.
   *
   * @return name of index
   */
  String getIndex();

  /**
   * Returns version of index type.
   * The version should be increased if the type changes and should be re indexed.
   *
   * @return current index type version
   */
  int getVersion();

  /**
   * Opens the index and return an updater for the given type.
   *
   * @return updater with open index
   */
  Updater<T> open();

  /**
   * Updater for index.
   *
   * @param <T> type to index
   */
  interface Updater<T> extends AutoCloseable {

    /**
     * Stores the given item in the index.
     *
     * @param item item to index
     */
    void store(T item);

    /**
     * Delete the given item from the index
     *
     * @param item item to delete
     */
    void delete(T item);

    /**
     * Re index all existing items.
     */
    void reIndexAll();

    /**
     * Close the given index.
     */
    void close();
  }

}
