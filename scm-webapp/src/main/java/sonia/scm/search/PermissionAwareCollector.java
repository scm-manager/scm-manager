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

import com.google.common.base.Strings;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.search.CollectionTerminatedException;
import org.apache.lucene.search.Collector;
import org.apache.lucene.search.LeafCollector;
import org.apache.lucene.search.Scorable;
import org.apache.lucene.search.ScoreMode;
import org.apache.shiro.SecurityUtils;

import java.io.IOException;
import java.util.Collections;
import java.util.Set;

public class PermissionAwareCollector implements Collector {

  private static final String FIELD_PERMISSION = "_permission";
  private static final Set<String> FIELDS = Collections.singleton(FIELD_PERMISSION);

  private final IndexReader reader;
  private final Collector delegate;
  private int totalHits = 0;
  private static final int SEARCH_RESULT_LIMIT = 500;


  public PermissionAwareCollector(IndexReader reader, Collector delegate) {
    this.reader = reader;
    this.delegate = delegate;
  }

  @Override
  public LeafCollector getLeafCollector(LeafReaderContext context) throws IOException {
    return new PermissionAwareLeafCollector(delegate.getLeafCollector(context), context.docBase);
  }

  @Override
  public ScoreMode scoreMode() {
    return delegate.scoreMode();
  }

  private class PermissionAwareLeafCollector implements LeafCollector {

    private final LeafCollector delegate;
    private final int docBase;

    private PermissionAwareLeafCollector(LeafCollector delegate, int docBase) {
      this.delegate = delegate;
      this.docBase = docBase;
    }

    @Override
    public void setScorer(Scorable scorer) throws IOException {
      this.delegate.setScorer(scorer);
    }

    @Override
    public void collect(int doc) throws IOException {
      Document document = reader.document(docBase + doc, FIELDS);
      String permission = document.get(FIELD_PERMISSION);
      if (Strings.isNullOrEmpty(permission) || SecurityUtils.getSubject().isPermitted(permission)) {
        ensureSearchResultLimit();
        this.delegate.collect(doc);
      }
    }

    private void ensureSearchResultLimit() {
      totalHits++;
      if (totalHits > SEARCH_RESULT_LIMIT) {
        throw new CollectionTerminatedException();
      }
    }
  }
}
