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
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.github.sdorra.jse.ShiroExtension;
import org.github.sdorra.jse.SubjectAware;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith({MockitoExtension.class, ShiroExtension.class})
@SubjectAware(value = "trillian", permissions = {"bench:1", "bench:2", "bench:3", "bench:*"})
class PermissionAwareCollectorTest {

  private static final int NUMBER_OF_DOCUMENTS = 10000;

  private static final boolean COUNT_ONLY = false;

  @Mock
  private IndexManager indexManager;

  private Path path;

  @BeforeEach
  void createLargeIndex(@TempDir Path path) throws IOException {
    this.path = path;
    IndexWriterConfig config = new IndexWriterConfig(new StandardAnalyzer());
    config.setOpenMode(IndexWriterConfig.OpenMode.CREATE);

    try (Directory directory = FSDirectory.open(path); IndexWriter writer = new IndexWriter(directory, config)) {
      for (int i = 0; i < NUMBER_OF_DOCUMENTS; i++) {
        Document document = document(i);
        writer.addDocument(document);
      }
    }
  }

  @Test
  void shouldReturnLimitedResults() throws IOException {
    QueryCountResult result = query("awesome");

    assertThat(result.getTotalHits()).isEqualTo(500);
  }

  private QueryCountResult query(String queryString) throws IOException {
    SearchableTypeResolver resolver = new SearchableTypeResolver(Sample.class);
    LuceneSearchableType searchableType = resolver.resolve(Sample.class);

    try (Directory directory = FSDirectory.open(path); IndexReader reader = DirectoryReader.open(directory)) {
      when(indexManager.openForRead(any(), any())).thenReturn(reader);

      LuceneQueryBuilder<Sample> queryBuilder = new LuceneQueryBuilder<>(
        indexManager, "...", searchableType, new StandardAnalyzer()
      );

      QueryCountResult result;
      if (COUNT_ONLY) {
        result = queryBuilder.count(queryString);
      } else {
        result = queryBuilder.execute(queryString);
      }
      return result;
    }
  }

  private Document document(int counter) {
    Document document = new Document();
    document.add(new StringField("_id", "" + counter, Field.Store.YES));

    String text;
    if (counter % 2 == 0) {
      text = "Awesome content number" + counter;
    } else {
      text = "Incredible value number" + counter;
    }

    document.add(new TextField("content", text, Field.Store.YES));
    document.add(new StringField("_permission", "bench:" + counter, Field.Store.YES));

    return document;
  }

  @Value
  @IndexedType
  public static class Sample {
    @Indexed(defaultQuery = true)
    String content;
  }
}
