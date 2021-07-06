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
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DefaultIndexQueueTest {

  private Directory directory;

  private DefaultIndexQueue queue;

  @BeforeEach
  void createQueue() throws IOException {
    directory = new ByteBuffersDirectory();
    IndexOpener factory = mock(IndexOpener.class);
    when(factory.openForWrite(any(String.class), any(IndexOptions.class))).thenAnswer(ic -> {
      IndexWriterConfig config = new IndexWriterConfig(new StandardAnalyzer());
      config.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
      return new IndexWriter(directory, config);
    });
    SearchEngine engine = new LuceneSearchEngine(factory, new DocumentConverter());
    queue = new DefaultIndexQueue(engine);
  }

  @AfterEach
  void closeQueue() throws IOException {
    queue.close();
    directory.close();
  }

  @Test
  void shouldWriteToIndex() throws Exception {
    try (Index index = queue.getQueuedIndex("default")) {
      index.store(Id.of("tricia"), null, new Account("tricia", "Trillian", "McMillan"));
      index.store(Id.of("dent"), null, new Account("dent", "Arthur", "Dent"));
    }
    assertDocCount(2);
  }

  @Test
  void shouldWriteMultiThreaded() throws Exception {
    ExecutorService executorService = Executors.newFixedThreadPool(4);
    for (int i = 0; i < 20; i++) {
      executorService.execute(new IndexNumberTask(i));
    }
    executorService.execute(() -> {
      try (Index index = queue.getQueuedIndex("default")) {
        index.delete(Id.of(String.valueOf(12)), IndexedNumber.class);
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
  public static class Account {
    @Indexed
    String username;
    @Indexed
    String firstName;
    @Indexed
    String lastName;
  }

  @Value
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
      try (Index index = queue.getQueuedIndex("default")) {
        index.store(Id.of(String.valueOf(number)), null, new IndexedNumber(number));
      }
    }
  }

}
