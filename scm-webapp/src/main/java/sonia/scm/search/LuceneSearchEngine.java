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

import javax.inject.Inject;
import java.io.IOException;

public class LuceneSearchEngine implements SearchEngine {

  private final IndexOpener indexOpener;
  private final DocumentConverter converter;
  private final LuceneQueryBuilderFactory queryBuilderFactory;

  @Inject
  public LuceneSearchEngine(IndexOpener indexOpener, DocumentConverter converter, LuceneQueryBuilderFactory queryBuilderFactory) {
    this.indexOpener = indexOpener;
    this.converter = converter;
    this.queryBuilderFactory = queryBuilderFactory;
  }

  @Override
  public Index getOrCreate(String name, IndexOptions options) {
    try {
      return new LuceneIndex(converter, indexOpener.openForWrite(name, options));
    } catch (IOException ex) {
      throw new SearchEngineException("failed to open index", ex);
    }
  }

  @Override
  public QueryBuilder search(String name, IndexOptions options) {
    return queryBuilderFactory.create(name, options);
  }

}
