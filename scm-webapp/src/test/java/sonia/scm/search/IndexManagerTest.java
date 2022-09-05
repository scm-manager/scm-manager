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
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.assertj.core.api.AssertionsForClassTypes;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.SCMContextProvider;
import sonia.scm.plugin.PluginLoader;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IndexManagerTest {

  private Path directory;

  @Mock
  private AnalyzerFactory analyzerFactory;

  @Mock
  private LuceneSearchableType searchableType;

  @Mock
  private SCMContextProvider context;

  @Mock
  private PluginLoader pluginLoader;

  private IndexManager indexManager;

  @BeforeEach
  void createIndexWriterFactory(@TempDir Path tempDirectory) {
    this.directory = tempDirectory;
    when(context.resolve(Paths.get("index"))).thenReturn(tempDirectory.resolve("index"));
    when(analyzerFactory.create(any(LuceneSearchableType.class))).thenReturn(new SimpleAnalyzer());
    when(pluginLoader.getUberClassLoader()).thenReturn(IndexManagerTest.class.getClassLoader());
    indexManager = new IndexManager(context, pluginLoader, analyzerFactory);
  }

  @Test
  void shouldCreateNewIndex() throws IOException {
    try (IndexWriter writer = open(Songs.class, "new-index")) {
      addDoc(writer, "Trillian");
    }
    assertThat(directory.resolve("index").resolve("songs").resolve("new-index")).exists();
  }

  @Test
  void shouldCreateNewIndexForEachType() throws IOException {
    try (IndexWriter writer = open(Songs.class, "new-index")) {
      addDoc(writer, "Trillian");
    }
    try (IndexWriter writer = open(Lyrics.class, "new-index")) {
      addDoc(writer, "Trillian");
    }
    assertThat(directory.resolve("index").resolve("songs").resolve("new-index")).exists();
    assertThat(directory.resolve("index").resolve("lyrics").resolve("new-index")).exists();
  }

  @Test
  void shouldReturnAllCreatedIndices() throws IOException {
    try (IndexWriter writer = open(Songs.class, "special")) {
      addDoc(writer, "Trillian");
    }
    try (IndexWriter writer = open(Lyrics.class, "awesome")) {
      addDoc(writer, "Trillian");
    }

    assertThat(indexManager.all())
      .anySatisfy(details -> {
        assertThat(details.getType()).isEqualTo(Songs.class);
        assertThat(details.getName()).isEqualTo("special");
      })
      .anySatisfy(details -> {
        assertThat(details.getType()).isEqualTo(Lyrics.class);
        assertThat(details.getName()).isEqualTo("awesome");
      });
  }

  @Test
  void shouldRestoreIndicesOnCreation() throws IOException {
    try (IndexWriter writer = open(Songs.class, "special")) {
      addDoc(writer, "Trillian");
    }
    try (IndexWriter writer = open(Lyrics.class, "awesome")) {
      addDoc(writer, "Trillian");
    }

    assertThat(new IndexManager(context, pluginLoader, analyzerFactory).all())
      .anySatisfy(details -> {
        AssertionsForClassTypes.assertThat(details.getType()).isEqualTo(Songs.class);
        AssertionsForClassTypes.assertThat(details.getName()).isEqualTo("special");
      })
      .anySatisfy(details -> {
        AssertionsForClassTypes.assertThat(details.getType()).isEqualTo(Lyrics.class);
        AssertionsForClassTypes.assertThat(details.getName()).isEqualTo("awesome");
      });
  }

  @SuppressWarnings({"rawtypes", "unchecked"})
  private IndexWriter open(Class type, String indexName) {
    lenient().when(searchableType.getType()).thenReturn(type);
    when(searchableType.getName()).thenReturn(type.getSimpleName().toLowerCase(Locale.ENGLISH));
    return indexManager.openForWrite(new IndexParams(indexName, searchableType));
  }

  @Test
  void shouldOpenExistingIndex() throws IOException {
    try (IndexWriter writer = open(Songs.class, "reused")) {
      addDoc(writer, "Dent");
    }
    try (IndexWriter writer = open(Songs.class, "reused")) {
      assertThat(writer.getFieldNames()).contains("hitchhiker");
    }
  }

  @Test
  void shouldUseAnalyzerFromFactory() throws IOException {
    try (IndexWriter writer = open(Songs.class, "new-index")) {
      assertThat(writer.getAnalyzer()).isInstanceOf(SimpleAnalyzer.class);
    }
  }

  @Test
  void shouldOpenIndexForRead() throws IOException {
    try (IndexWriter writer = open(Songs.class, "idx-for-read")) {
      addDoc(writer, "Dent");
    }

    try (IndexReader reader = indexManager.openForRead(searchableType, "idx-for-read")) {
      assertThat(reader.numDocs()).isOne();
    }
  }

  @Test
  void shouldOpenNoOpIndexReaderForMissingIndex() throws IOException {
    open(Songs.class, "idx-for-read");
    try (IndexReader reader = indexManager.openForRead(searchableType, "idx-for-read")) {
      assertThat(reader).isInstanceOf(NoOpIndexReader.class);
    }
  }

  private void addDoc(IndexWriter writer, String name) throws IOException {
    Document doc = new Document();
    doc.add(new TextField("hitchhiker", name, Field.Store.YES));
    writer.addDocument(doc);
  }

  public static class Songs {
  }

  public static class Lyrics {
  }

}
