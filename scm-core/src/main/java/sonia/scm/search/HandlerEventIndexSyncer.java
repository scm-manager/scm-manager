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
import sonia.scm.HandlerEventType;
import sonia.scm.event.HandlerEvent;

/**
 * Keep index in sync with {@link HandlerEvent}.
 *
 * @param <T> type of indexed item
 * @since 2.22.0
 */
@Beta
public final class HandlerEventIndexSyncer<T> {

  private final SearchEngine searchEngine;
  private final Indexer<T> indexer;

  public HandlerEventIndexSyncer(SearchEngine searchEngine, Indexer<T> indexer) {
    this.searchEngine = searchEngine;
    this.indexer = indexer;
  }

  /**
   * Update index based on {@link HandlerEvent}.
   *
   * @param event handler event
   */
  public void handleEvent(HandlerEvent<T> event) {
    HandlerEventType type = event.getEventType();
    if (type.isPost()) {
      SerializableIndexTask<T> task = createTask(type, event.getItem());
      searchEngine.forType(indexer.getType()).update(task);
    }
  }

  private SerializableIndexTask<T> createTask(HandlerEventType type, T item) {
    if (type == HandlerEventType.DELETE) {
      return indexer.createDeleteTask(item);
    } else {
      return indexer.createStoreTask(item);
    }
  }

}
