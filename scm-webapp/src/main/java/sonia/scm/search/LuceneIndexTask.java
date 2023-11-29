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

import com.google.inject.Injector;
import jakarta.inject.Inject;

import java.io.Serializable;

public abstract class LuceneIndexTask implements Runnable, Serializable {

  private final Class<?> type;
  private final String indexName;

  private transient LuceneIndexFactory indexFactory;
  private transient SearchableTypeResolver searchableTypeResolver;
  private transient Injector injector;

  protected LuceneIndexTask(IndexParams params) {
    this.type = params.getSearchableType().getType();
    this.indexName = params.getIndex();
  }

  @Inject
  public void setIndexFactory(LuceneIndexFactory indexFactory) {
    this.indexFactory = indexFactory;
  }

  @Inject
  public void setSearchableTypeResolver(SearchableTypeResolver searchableTypeResolver) {
    this.searchableTypeResolver = searchableTypeResolver;
  }

  @Inject
  public void setInjector(Injector injector) {
    this.injector = injector;
  }

  public abstract IndexTask<?> task(Injector injector);

  @SuppressWarnings({"unchecked", "rawtypes"})
  public void run() {
    LuceneSearchableType searchableType = searchableTypeResolver.resolve(type);
    IndexTask<?> task = task(injector);
    try (LuceneIndex index = indexFactory.create(new IndexParams(indexName, searchableType))) {
      task.update(index);
    }
    task.afterUpdate();
  }


}
