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

import org.apache.lucene.analysis.core.SimpleAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.SCMContextProvider;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IndexWriterFactoryTest {

  private Path directory;

  @Mock
  private AnalyzerFactory analyzerFactory;

  private IndexWriterFactory indexWriterFactory;

  @BeforeEach
  void createIndexWriterFactory(@TempDir Path tempDirectory) {
    this.directory = tempDirectory;
    SCMContextProvider context = mock(SCMContextProvider.class);
    when(context.resolve(Paths.get("index"))).thenReturn(tempDirectory);
    when(analyzerFactory.create(any(IndexOptions.class))).thenReturn(new SimpleAnalyzer());
    indexWriterFactory = new IndexWriterFactory(context, analyzerFactory);
  }

  @Test
  void shouldCreateNewIndex() throws IOException {
    try (IndexWriter writer = indexWriterFactory.create("new-index", IndexOptions.defaults())) {
      addDoc(writer, "Trillian");
    }
    assertThat(directory.resolve("new-index")).exists();
  }

  @Test
  void shouldOpenExistingIndex() throws IOException {
    try (IndexWriter writer = indexWriterFactory.create("reused", IndexOptions.defaults())) {
      addDoc(writer, "Dent");
    }
    try (IndexWriter writer = indexWriterFactory.create("reused", IndexOptions.defaults())) {
      assertThat(writer.getFieldNames()).contains("hitchhiker");
    }
  }

  @Test
  void shouldUseAnalyzerFromFactory() throws IOException {
    try (IndexWriter writer = indexWriterFactory.create("new-index", IndexOptions.defaults())) {
      assertThat(writer.getAnalyzer()).isInstanceOf(SimpleAnalyzer.class);
    }
  }

  private void addDoc(IndexWriter writer, String name) throws IOException {
    Document doc = new Document();
    doc.add(new TextField("hitchhiker", name, Field.Store.YES));
    writer.addDocument(doc);
  }

}
