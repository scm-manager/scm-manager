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

import com.google.common.annotations.VisibleForTesting;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
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
      LOG.debug("open writer, because usage increased from zero to one");
      writer = writerFactory.get();
    } else {
      LOG.debug("new task is using the writer, counter is now at {}", usageCounter);
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

  long deleteAll() throws IOException {
    return writer.deleteAll();
  }

  synchronized void close() throws IOException {
    usageCounter--;
    if (usageCounter == 0) {
      LOG.debug("no one seems to use index any longer, closing underlying writer");
      writer.close();
      writer = null;
    } else if (usageCounter > 0) {
      LOG.debug("index is still used by {} task(s), commit work but keep writer open", usageCounter);
      writer.commit();
    } else {
      LOG.warn("index is already closed");
    }
  }
}
