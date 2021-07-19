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

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;

import javax.inject.Inject;
import java.util.List;
import java.util.stream.Collectors;

public class LuceneSearchEngine implements SearchEngine {

  private final SearchableTypeResolver resolver;
  private final LuceneIndexFactory indexFactory;
  private final LuceneQueryBuilderFactory queryBuilderFactory;

  @Inject
  public LuceneSearchEngine(SearchableTypeResolver resolver, LuceneIndexFactory indexFactory, LuceneQueryBuilderFactory queryBuilderFactory) {
    this.resolver = resolver;
    this.indexFactory = indexFactory;
    this.queryBuilderFactory = queryBuilderFactory;
  }

  @Override
  public List<SearchableType> getSearchableTypes() {
    Subject subject = SecurityUtils.getSubject();
    return resolver.getSearchableTypes()
      .stream()
      .filter(type -> type.getPermission().map(subject::isPermitted).orElse(true))
      .collect(Collectors.toList());
  }

  @Override
  public Index getOrCreate(String name, IndexOptions options) {
    return indexFactory.create(name, options);
  }

  @Override
  public QueryBuilder search(String name, IndexOptions options) {
    return queryBuilderFactory.create(name, options);
  }

}
