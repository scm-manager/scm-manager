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

import java.util.ArrayList;
import java.util.List;

public class QueuedIndex implements Index {

  private final DefaultIndexQueue queue;
  private final String indexName;
  private final IndexOptions indexOptions;

  private final List<IndexQueueTask> tasks = new ArrayList<>();

  QueuedIndex(DefaultIndexQueue queue, String indexName, IndexOptions indexOptions) {
    this.queue = queue;
    this.indexName = indexName;
    this.indexOptions = indexOptions;
  }

  @Override
  public void store(Id id, Object object) {
    tasks.add(index -> index.store(id, object));
  }

  @Override
  public void delete(Id id, Class<?> type) {
    tasks.add(index -> index.delete(id, type));
  }

  @Override
  public void deleteByRepository(String repository) {
    tasks.add(index -> index.deleteByRepository(repository));
  }

  @Override
  public void close() {
    IndexQueueTaskWrapper wrappedTask = new IndexQueueTaskWrapper(
      queue.getSearchEngine(), indexName, indexOptions, tasks
    );
    queue.enqueue(wrappedTask);
  }
}
