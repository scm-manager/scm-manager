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

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Singleton
@SuppressWarnings("unchecked")
public class LuceneIndexFactory {

  private final IndexManager indexManager;
  @SuppressWarnings("rawtypes")
  private final Map<IndexKey, LuceneIndex> indexes = new ConcurrentHashMap<>();

  @Inject
  public LuceneIndexFactory(IndexManager indexManager) {
    this.indexManager = indexManager;
  }

  public <T> LuceneIndex<T> create(IndexParams indexParams) {
    return indexes.compute(keyOf(indexParams), (key, index) -> {
      if (index != null) {
        index.open();
        return index;
      }
      return new LuceneIndex<>(
        indexParams,
        () -> indexManager.openForWrite(indexParams)
      );
    });
  }

  private IndexKey keyOf(IndexParams indexParams) {
    return new IndexKey(
      indexParams.getSearchableType().getName(), indexParams.getIndex()
    );
  }

  @EqualsAndHashCode
  @AllArgsConstructor
  private static class IndexKey {
    String type;
    String name;
  }
}
