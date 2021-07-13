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

/**
 * Queue the work of indexing.
 * An index can't be opened in parallel, so the queue coordinates the work of indexing in an asynchronous manner.
 * {@link IndexQueue} should be used most of the time to index content.
 *
 * @since 2.21.0
 */
public interface IndexQueue {

  /**
   * Returns an index which queues every change to the content.
   *
   * @param name name of index
   * @param indexOptions options for the index
   *
   * @return index which queues changes
   */
  Index getQueuedIndex(String name, IndexOptions indexOptions);

  /**
   * Returns an index which with default options which queues every change to the content.
   * @param name name of index
   *
   * @return index with default options which queues changes
   * @see IndexOptions#defaults()
   */
  default Index getQueuedIndex(String name) {
    return getQueuedIndex(name, IndexOptions.defaults());
  }

}
