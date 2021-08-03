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
import org.apache.lucene.store.ByteBuffersDirectory;
import org.apache.lucene.store.Directory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.in;
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IndexQueueTest {

  private Directory directory;

  private IndexQueue queue;

  @BeforeEach
  void createQueue() throws IOException {
    directory = new ByteBuffersDirectory();
    IndexOpener opener = mock(IndexOpener.class);
    when(opener.openForWrite(any(IndexParams.class))).thenAnswer(ic -> {
      IndexWriterConfig config = new IndexWriterConfig(new StandardAnalyzer());
      config.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
      return new IndexWriter(directory, config);
    });

    LuceneIndexFactory indexFactory = new LuceneIndexFactory(opener);
    queue = new IndexQueue(indexFactory);
  }

  @AfterEach
  void closeQueue() throws IOException {
    queue.close();
    directory.close();
  }

  @Test
  void shouldWriteToIndex() throws Exception {
    try (Index<Account> index = getIndex(Account.class)) {
      index.store(Id.of("tricia"), null, new Account("tricia", "Trillian", "McMillan"));
      index.store(Id.of("dent"), null, new Account("dent", "Arthur", "Dent"));
    }
    assertDocCount(2);
  }

  private <T> Index<T> getIndex(Class<T> type) {
    SearchableTypeResolver resolver = new SearchableTypeResolver(type);
    LuceneSearchableType searchableType = resolver.resolve(type);
    IndexParams indexParams = new IndexParams("default", searchableType, IndexOptions.defaults());
    return queue.getQueuedIndex(indexParams);
  }

  @Test
  void shouldWriteMultiThreaded() throws Exception {
    ExecutorService executorService = Executors.newFixedThreadPool(4);
    for (int i = 0; i < 20; i++) {
      executorService.execute(new IndexNumberTask(i));
    }
    executorService.execute(() -> {
      try (Index<IndexedNumber> index = getIndex(IndexedNumber.class)) {
        index.delete().byType().byId(Id.of(String.valueOf(12)));
      }
    });
    executorService.shutdown();

    assertDocCount(19);
  }

  private void assertDocCount(int expectedCount) throws IOException {
    // wait until all tasks are finished
    await().until(() -> queue.getSize() == 0);
    try (DirectoryReader reader = DirectoryReader.open(directory)) {
      assertThat(reader.numDocs()).isEqualTo(expectedCount);
    }
  }

  @Value
  @IndexedType
  public static class Account {
    @Indexed
    String username;
    @Indexed
    String firstName;
    @Indexed
    String lastName;
  }

  @Value
  @IndexedType
  public static class IndexedNumber {
    @Indexed
    int value;
  }

  public class IndexNumberTask implements Runnable {

    private final int number;

    public IndexNumberTask(int number) {
      this.number = number;
    }

    @Override
    public void run() {
      try (Index<IndexedNumber> index = getIndex(IndexedNumber.class)) {
        index.store(Id.of(String.valueOf(number)), null, new IndexedNumber(number));
      }
    }
  }

}
