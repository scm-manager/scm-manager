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
