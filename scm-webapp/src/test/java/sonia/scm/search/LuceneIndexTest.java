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
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.ByteBuffersDirectory;
import org.apache.lucene.store.Directory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.group.Group;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryTestData;
import sonia.scm.user.User;

import java.io.IOException;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static sonia.scm.search.FieldNames.ID;
import static sonia.scm.search.FieldNames.PERMISSION;
import static sonia.scm.search.FieldNames.REPOSITORY;

class LuceneIndexTest {

  private static final Id<Storable> ONE = Id.of(Storable.class, "one");
  private static final Id<Storable> TWO = Id.of(Storable.class, "two");

  private Directory directory;

  @BeforeEach
  void createDirectory() {
    directory = new ByteBuffersDirectory();
  }

  @Test
  void shouldStoreObject() throws IOException {
    try (LuceneIndex<Storable> index = createIndex(Storable.class)) {
      index.store(ONE, null, new Storable("Awesome content which should be indexed"));
    }

    assertHits("value", "content", 1);
  }

  @Test
  void shouldUpdateObject() throws IOException {
    try (LuceneIndex<Storable> index = createIndex(Storable.class)) {
      index.store(ONE, null, new Storable("Awesome content which should be indexed"));
      index.store(ONE, null, new Storable("Awesome content"));
    }

    assertHits("value", "content", 1);
  }

  @Test
  void shouldStoreIdOfObject() throws IOException {
    try (LuceneIndex<Storable> index = createIndex(Storable.class)) {
      index.store(ONE, null, new Storable("Some text"));
    }

    assertHits(ID, "one", 1);
  }

  @Test
  void shouldStoreRepositoryOfId() throws IOException {
    try (LuceneIndex<Storable> index = createIndex(Storable.class)) {
      index.store(ONE.and(Repository.class, "4211"), null, new Storable("Some text"));
    }

    assertHits(REPOSITORY, "4211", 1);
  }

  @Test
  void shouldDeleteById() throws IOException {
    try (LuceneIndex<Storable> index = createIndex(Storable.class)) {
      index.store(ONE, null, new Storable("Some other text"));
    }

    try (LuceneIndex<Storable> index = createIndex(Storable.class)) {
      index.delete().byId(ONE);
    }

    assertHits(ID, "one", 0);
  }

  @Test
  void shouldDeleteByIdAndRepository() throws IOException {
    Repository heartOfGold = RepositoryTestData.createHeartOfGold();
    Repository puzzle42 = RepositoryTestData.createHeartOfGold();
    try (LuceneIndex<Storable> index = createIndex(Storable.class)) {
      index.store(ONE.and(Repository.class, heartOfGold), null, new Storable("content"));
      index.store(ONE.and(Repository.class, puzzle42), null, new Storable("content"));
    }

    try (LuceneIndex<Storable> index = createIndex(Storable.class)) {
      index.delete().byId(ONE.and(Repository.class, heartOfGold));
    }

    assertHits("value", "content", 1);
  }

  @Test
  void shouldDeleteAll() throws IOException {
    try (LuceneIndex<Storable> index = createIndex(Storable.class)) {
      index.store(ONE, null, new Storable("content"));
      index.store(TWO, null, new Storable("content"));
    }

    try (LuceneIndex<Storable> index = createIndex(Storable.class)) {
      index.delete().all();
    }

    assertHits("value", "content", 0);
  }

  @Test
  void shouldDeleteByRepository() throws IOException {
    try (LuceneIndex<Storable> index = createIndex(Storable.class)) {
      index.store(ONE.and(Repository.class, "4211"), null, new Storable("content"));
      index.store(TWO.and(Repository.class, "4212"), null, new Storable("content"));
    }

    try (LuceneIndex<Storable> index = createIndex(Storable.class)) {
      index.delete().by(Repository.class, "4212").execute();
    }

    assertHits("value", "content", 1);
  }

  @Test
  void shouldDeleteByMultipleFields() throws IOException {
    Id<Storable> base = ONE.and(Repository.class, "4211");

    try (LuceneIndex<Storable> index = createIndex(Storable.class)) {
      index.store(base.and(String.class, "1"), null, new Storable("content"));
      index.store(base.and(String.class, "2"), null, new Storable("content"));
      index.store(base.and(String.class, "2"), null, new Storable("content"));
    }

    try (LuceneIndex<Storable> index = createIndex(Storable.class)) {
      index.delete().by(Repository.class, "4211").and(String.class, "2").execute();
    }

    assertHits("value", "content", 1);
  }

  @Test
  void shouldStorePermission() throws IOException {
    try (LuceneIndex<Storable> index = createIndex(Storable.class)) {
      index.store(ONE.and(Repository.class, "4211"), "repo:4211:read", new Storable("Some other text"));
    }

    assertHits(PERMISSION, "repo:4211:read", 1);
  }

  @Test
  void shouldReturnDetails() {
    try (LuceneIndex<Storable> index = createIndex(Storable.class)) {
      IndexDetails details = index.getDetails();
      assertThat(details.getType()).isEqualTo(Storable.class);
      assertThat(details.getName()).isEqualTo("default");
    }
  }

  @Test
  void shouldStoreIdFields() throws IOException {
    Id<Storable> id = ONE.and(User.class, "trillian").and(Group.class, "heart-of-gold");
    try (LuceneIndex<Storable> index = createIndex(Storable.class)) {
      index.store(id, "repo:4211:read", new Storable("Some other text"));
    }

    assertHits("_id", "one;heart-of-gold;trillian", 1);
    assertHits("_user", "trillian", 1);
    assertHits("_group", "heart-of-gold", 1);
  }

  @Nested
  @ExtendWith(MockitoExtension.class)
  class ExceptionTests {

    @Mock
    private IndexWriter writer;

    private LuceneIndex<Storable> index;

    @BeforeEach
    void setUpIndex() {
      index = createIndex(Storable.class, () -> writer);
    }

    @Test
    void shouldThrowSearchEngineExceptionOnStore() throws IOException {
      when(writer.updateDocument(any(), any())).thenThrow(new IOException("failed to store"));

      Storable storable = new Storable("Some other text");
      assertThrows(SearchEngineException.class, () -> index.store(ONE, null, storable));
    }

    @Test
    void shouldThrowSearchEngineExceptionOnDeleteById() throws IOException {
      when(writer.deleteDocuments(any(Term.class))).thenThrow(new IOException("failed to delete"));

      Index.Deleter<Storable> deleter = index.delete();
      assertThrows(SearchEngineException.class, () -> deleter.byId(ONE));
    }

    @Test
    void shouldThrowSearchEngineExceptionOnDeleteAll() throws IOException {
      when(writer.deleteAll()).thenThrow(new IOException("failed to delete"));

      Index.Deleter<Storable> deleter = index.delete();
      assertThrows(SearchEngineException.class, deleter::all);
    }

    @Test
    void shouldThrowSearchEngineExceptionOnDeleteBy() throws IOException {
      when(writer.deleteDocuments(any(Query.class))).thenThrow(new IOException("failed to delete"));

      Index.DeleteBy deleter = index.delete().by(Repository.class, "42");
      assertThrows(SearchEngineException.class, deleter::execute);
    }

    @Test
    void shouldThrowSearchEngineExceptionOnClose() throws IOException {
      doThrow(new IOException("failed to delete")).when(writer).close();
      assertThrows(SearchEngineException.class, () -> index.close());
    }

  }

  private void assertHits(String field, String value, int expectedHits) throws IOException {
    try (DirectoryReader reader = DirectoryReader.open(directory)) {
      IndexSearcher searcher = new IndexSearcher(reader);
      TopDocs docs = searcher.search(new TermQuery(new Term(field, value)), 10);
      assertThat(docs.totalHits.value).isEqualTo(expectedHits);
    }
  }

  private <T> LuceneIndex<T> createIndex(Class<T> type) {
    return createIndex(type, this::createWriter);
  }

  private <T> LuceneIndex<T> createIndex(Class<T> type, Supplier<IndexWriter> writerFactor) {
    SearchableTypeResolver resolver = new SearchableTypeResolver(type);
    return new LuceneIndex<>(
      new IndexParams("default", resolver.resolve(type).get()), writerFactor
    );
  }

  private IndexWriter createWriter() {
    IndexWriterConfig config = new IndexWriterConfig(new StandardAnalyzer());
    config.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
    try {
      return new IndexWriter(directory, config);
    } catch (IOException ex) {
      throw new SearchEngineException("failed to open index writer", ex);
    }
  }

  @Value
  @IndexedType
  private static class Storable {
    @Indexed
    String value;
  }

}
