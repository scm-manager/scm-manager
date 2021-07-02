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

import lombok.Value;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.ByteBuffersDirectory;
import org.apache.lucene.store.Directory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static sonia.scm.search.LuceneIndex.FIELD_ID;
import static sonia.scm.search.LuceneIndex.FIELD_TYPE;

class LuceneIndexTest {

  private Directory directory;

  @BeforeEach
  void createDirectory() {
    directory = new ByteBuffersDirectory();
  }

  @Test
  void shouldStoreObject() throws IOException {
    try (LuceneIndex index = createIndex()) {
      index.store("one", new Storable("Awesome content which should be indexed"));
    }

    assertHits("value", "content", 1);
  }

  @Test
  void shouldStoreIdOfObject() throws IOException {
    try (LuceneIndex index = createIndex()) {
      index.store("one", new Storable("Some text"));
    }

    assertHits(FIELD_ID, "one", 1);
  }

  @Test
  void shouldStoreTypeOfObject() throws IOException {
    try (LuceneIndex index = createIndex()) {
      index.store("one", new Storable("Some other text"));
    }

    assertHits(FIELD_TYPE, Storable.class.getName(), 1);
  }

  @Test
  void shouldDeleteById() throws IOException {
    try (LuceneIndex index = createIndex()) {
      index.store("one", new Storable("Some other text"));
    }

    try (LuceneIndex index = createIndex()) {
      index.delete("one");
    }

    assertHits(FIELD_ID, "one", 0);
  }

  private void assertHits(String field, String value, int expectedHits) throws IOException {
    try (DirectoryReader reader = DirectoryReader.open(directory)) {
      IndexSearcher searcher = new IndexSearcher(reader);
      TopDocs docs = searcher.search(new TermQuery(new Term(field, value)), 10);
      assertThat(docs.totalHits.value).isEqualTo(expectedHits);
    }
  }

  private LuceneIndex createIndex() throws IOException {
    return new LuceneIndex(new DocumentConverter(), createWriter());
  }

  private IndexWriter createWriter() throws IOException {
    IndexWriterConfig config = new IndexWriterConfig(new StandardAnalyzer());
    config.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
    return new IndexWriter(directory, config);
  }

  @Value
  private static class Storable {
    @Indexed
    String value;
  }

}
