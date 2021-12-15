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

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.highlight.InvalidTokenOffsetsException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.Optional.empty;
import static java.util.Optional.of;

public class QueryResultFactory {

  private final LuceneHighlighter highlighter;
  private final IndexSearcher searcher;
  private final LuceneSearchableType searchableType;

  public QueryResultFactory(Analyzer analyzer, IndexSearcher searcher, LuceneSearchableType searchableType, Query query) {
    this.searcher = searcher;
    this.searchableType = searchableType;
    this.highlighter = new LuceneHighlighter(analyzer, query);
  }

  public QueryResult create(TopDocs topDocs) throws IOException, InvalidTokenOffsetsException {
    List<Hit> hits = new ArrayList<>();
    for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
      hits.add(createHit(scoreDoc));
    }
    return new QueryResult(topDocs.totalHits.value, searchableType.getType(), hits);
  }

  private Hit createHit(ScoreDoc scoreDoc) throws IOException, InvalidTokenOffsetsException {
    Document document = searcher.doc(scoreDoc.doc);
    Map<String, Hit.Field> fields = new HashMap<>();
    for (LuceneSearchableField field : searchableType.getAllFields()) {
      field(document, field).ifPresent(f -> fields.put(field.getName(), f));
    }
    return new Hit(document.get(FieldNames.ID), document.get(FieldNames.REPOSITORY), scoreDoc.score, fields);
  }

  private Optional<Hit.Field> field(Document document, LuceneSearchableField field) throws IOException, InvalidTokenOffsetsException {
    Object value = field.value(document);
    if (value != null) {
      if (highlighter.isHighlightable(field)) {
        ContentFragment[] fragments = createFragments(field, value.toString());
        if (fragments.length > 0) {
          boolean firstFragmentMatchesContentStart = fragments[0].isMatchesContentStart();
          boolean lastFragmentMatchesContentEnd = fragments[fragments.length - 1].isMatchesContentEnd();
          return of(new Hit.HighlightedField(Arrays.stream(fragments).map(ContentFragment::getFragment).toArray(String[]::new), firstFragmentMatchesContentStart, lastFragmentMatchesContentEnd));
        }
      }
      return of(new Hit.ValueField(value));
    }
    return empty();
  }

  private ContentFragment[] createFragments(LuceneSearchableField field, String value) throws InvalidTokenOffsetsException, IOException {
    return highlighter.highlight(field.getName(), field.getAnalyzer(), value);
  }

}
