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

import com.google.common.annotations.VisibleForTesting;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.function.Supplier;

class SharableIndexWriter {

  private static final Logger LOG = LoggerFactory.getLogger(SharableIndexWriter.class);

  private int usageCounter = 0;

  private final Supplier<IndexWriter> writerFactory;
  private IndexWriter writer;

  SharableIndexWriter(Supplier<IndexWriter> writerFactory) {
    this.writerFactory = writerFactory;
  }

  synchronized void open() {
    usageCounter++;
    if (usageCounter == 1) {
      LOG.trace("open writer, because usage increased from zero to one");
      writer = writerFactory.get();
    } else {
      LOG.trace("new task is using the writer, counter is now at {}", usageCounter);
    }
  }

  @VisibleForTesting
  int getUsageCounter() {
    return usageCounter;
  }

  void updateDocument(Term term, Document document) throws IOException {
    writer.updateDocument(term, document);
  }

  long deleteDocuments(Term term) throws IOException {
    return writer.deleteDocuments(term);
  }

  long deleteDocuments(Query query) throws IOException {
    return writer.deleteDocuments(query);
  }

  long deleteAll() throws IOException {
    return writer.deleteAll();
  }

  synchronized void close() throws IOException {
    usageCounter--;
    if (usageCounter == 0) {
      LOG.trace("no one seems to use index any longer, closing underlying writer");
      writer.close();
      writer = null;
    } else if (usageCounter > 0) {
      LOG.trace("index is still used by {} task(s), commit work but keep writer open", usageCounter);
      writer.commit();
    } else {
      LOG.warn("index is already closed");
    }
  }
}
