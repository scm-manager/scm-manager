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

public class QueuedIndex<T> implements Index<T> {

  private final IndexQueue queue;
  private final IndexParams indexParams;
  private final List<IndexQueueTask<T>> tasks = new ArrayList<>();

   QueuedIndex(IndexQueue queue, IndexParams indexParams) {
    this.queue = queue;
     this.indexParams = indexParams;
   }

  @Override
  public void store(Id id, String permission, T object) {
    tasks.add(index -> index.store(id, permission, object));
  }

  @Override
  public Deleter delete() {
    return new QueueDeleter();
  }

  @Override
  public void close() {
    IndexQueueTaskWrapper<T> wrappedTask = new IndexQueueTaskWrapper<>(
      queue.getIndexFactory(), indexParams, tasks
    );
    queue.enqueue(wrappedTask);
  }

  class QueueDeleter implements Deleter {

    @Override
    public ByTypeDeleter byType() {
      return new QueueByTypeDeleter();
    }

    @Override
    public AllTypesDeleter allTypes() {
      return new QueueAllTypesDeleter();
    }
  }

  class QueueByTypeDeleter implements ByTypeDeleter {

    @Override
    public void byId(Id id) {
      tasks.add(index -> index.delete().byType().byId(id));
    }

    @Override
    public void all() {
      tasks.add(index -> index.delete().byType().all());
    }

    @Override
    public void byRepository(String repositoryId) {
      tasks.add(index -> index.delete().byType().byRepository(repositoryId));
    }
  }

  class QueueAllTypesDeleter implements AllTypesDeleter {

    @Override
    public void byRepository(String repositoryId) {
      tasks.add(index -> index.delete().allTypes().byRepository(repositoryId));
    }

    @Override
    public void byTypeName(String typeName) {
      tasks.add(index -> index.delete().allTypes().byTypeName(typeName));
    }
  }
}
