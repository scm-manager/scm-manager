/*
 * Copyright (c) 2020 - present Cloudogu GmbH
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

package sonia.scm.search;

import com.google.common.base.Strings;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.LeafReaderContext;
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
        this.delegate.collect(doc);
      }
    }
  }
}
