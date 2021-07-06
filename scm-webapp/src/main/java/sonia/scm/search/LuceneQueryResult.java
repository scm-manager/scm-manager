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

import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.lucene.document.Document;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class LuceneQueryResult implements QueryResult {

  @JsonProperty
  private final long totalHits;
  @JsonProperty
  private final List<Hit> hits;

  private LuceneQueryResult(long totalHits, List<Hit> hits) {
    this.totalHits = totalHits;
    this.hits = hits;
  }

  static LuceneQueryResult of(IndexSearcher searcher, TopDocs topDocs) throws IOException {
    List<Hit> hits = new ArrayList<>();
    for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
      Document document = searcher.doc(scoreDoc.doc);
      hits.add(new LuceneHit(document, scoreDoc.score));
    }
    return new LuceneQueryResult(topDocs.totalHits.value, hits);
  }

  @Override
  public long totalHits() {
    return totalHits;
  }

  @Override
  public List<Hit> hits() {
    return hits;
  }

  public static class LuceneHit implements Hit {

    @JsonProperty
    private final Document document;
    @JsonProperty
    private final float score;

    public LuceneHit(Document document, float score) {
      this.document = document;
      this.score = score;
    }

    @Override
    public String get(String name) {
      return document.get(name);
    }

    @Override
    public float getScore() {
      return score;
    }
  }

}
