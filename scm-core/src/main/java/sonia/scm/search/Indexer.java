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
import sonia.scm.plugin.ExtensionPoint;

/**
 * Indexer for a specific type.
 * Note: An implementation of the indexer creates only a bootstrap index.
 * To keep the index in sync you have to implement a subscriber, see {@link HandlerEventIndexSyncer}.
 *
 * @param <T> type to index
 * @since 2.22.0
 * @see HandlerEventIndexSyncer
 */
@Beta
@ExtensionPoint
public interface Indexer<T> {

  /**
   * Returns class of type.
   */
  Class<T> getType();

  /**
   * Returns version of index type.
   * The version should be increased if the type changes and should be re indexed.
   *
   * @return current index type version
   */
  int getVersion();

  /**
   * Returns task which re index all items.
   * @since 2.23.0
   */
  Class<? extends ReIndexAllTask<T>> getReIndexAllTask();

  /**
   * Creates a task which stores the given item in the index.
   * @param item item to store
   * @return task which stores the item
   * @since 2.23.0
   */
  SerializableIndexTask<T> createStoreTask(T item);

  /**
   * Creates a task which deletes the given item from index.
   * @param item item to delete
   * @return task which deletes the item
   * @since 2.23.0
   */
  SerializableIndexTask<T> createDeleteTask(T item);

  /**
   * Abstract class which builds the foundation for tasks which re-index all items.
   *
   * @since 2.23.0
   */
  abstract class ReIndexAllTask<T> implements IndexTask<T> {

    private final IndexLogStore logStore;
    private final Class<T> type;
    private final int version;

    protected ReIndexAllTask(IndexLogStore logStore, Class<T> type, int version) {
      this.logStore = logStore;
      this.type = type;
      this.version = version;
    }

    @Override
    public void afterUpdate() {
      logStore.defaultIndex().log(type, version);
    }
  }
}
