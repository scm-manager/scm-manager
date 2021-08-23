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
   *
   * @return class of type
   */
  Class<T> getType();

  /**
   * Returns version of index type.
   * The version should be increased if the type changes and should be re indexed.
   *
   * @return current index type version
   */
  int getVersion();

  Class<? extends ReIndexAllTask<T>> getReIndexAllTask();

  IndexTask<T> createStoreTask(T item);

  IndexTask<T> createDeleteTask(T item);

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
