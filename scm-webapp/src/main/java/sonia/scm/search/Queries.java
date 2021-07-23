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

import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;

import static org.apache.lucene.search.BooleanClause.Occur.MUST;

final class Queries {

  private Queries() {
  }

  private static Query typeQuery(LuceneSearchableType type) {
    return new TermQuery(new Term(FieldNames.TYPE, type.getName()));
  }

  private static Query repositoryQuery(String repositoryId) {
    return new TermQuery(new Term(FieldNames.REPOSITORY, repositoryId));
  }

  static Query filter(Query query, LuceneSearchableType searchableType, QueryBuilder.QueryParams params) {
    BooleanQuery.Builder builder = new BooleanQuery.Builder()
      .add(query, MUST)
      .add(typeQuery(searchableType), MUST);
    params.getRepositoryId().ifPresent(repo -> builder.add(repositoryQuery(repo), MUST));
    return builder.build();
  }
}
