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

import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SharableIndexWriterTest {

  @Mock
  private IndexWriter underlyingWriter;

  @Test
  @SuppressWarnings("unchecked")
  void shouldCreateIndexOnOpen() {
    Supplier<IndexWriter> supplier = mock(Supplier.class);

    SharableIndexWriter writer = new SharableIndexWriter(supplier);
    verifyNoInteractions(supplier);

    writer.open();
    verify(supplier).get();
  }

  @Test
  @SuppressWarnings("unchecked")
  void shouldOpenWriterOnlyOnce() {
    Supplier<IndexWriter> supplier = mock(Supplier.class);

    SharableIndexWriter writer = new SharableIndexWriter(supplier);
    writer.open();
    writer.open();
    writer.open();

    verify(supplier).get();
  }

  @Test
  void shouldIncreaseUsageCounter() {
    SharableIndexWriter writer = new SharableIndexWriter(() -> underlyingWriter);
    writer.open();
    writer.open();
    writer.open();

    assertThat(writer.getUsageCounter()).isEqualTo(3);
  }

  @Test
  void shouldDecreaseUsageCounter() throws IOException {
    SharableIndexWriter writer = new SharableIndexWriter(() -> underlyingWriter);
    writer.open();
    writer.open();
    writer.open();

    assertThat(writer.getUsageCounter()).isEqualTo(3);

    writer.close();
    writer.close();

    assertThat(writer.getUsageCounter()).isOne();
  }

  @Test
  void shouldNotCloseWriterIfUsageCounterIsGreaterZero() throws IOException {
    SharableIndexWriter writer = new SharableIndexWriter(() -> underlyingWriter);
    writer.open();
    writer.open();
    writer.open();

    writer.close();
    writer.close();

    verify(underlyingWriter, never()).close();
  }

  @Test
  void shouldCloseIfUsageCounterIsZero() throws IOException {
    SharableIndexWriter writer = new SharableIndexWriter(() -> underlyingWriter);
    writer.open();
    writer.open();

    writer.close();
    writer.close();

    verify(underlyingWriter).close();
  }

  @Test
  @SuppressWarnings("unchecked")
  void shouldReOpen() throws IOException {
    Supplier<IndexWriter> supplier = mock(Supplier.class);
    when(supplier.get()).thenReturn(underlyingWriter);

    SharableIndexWriter writer = new SharableIndexWriter(supplier);
    writer.open();

    writer.close();
    writer.open();

    verify(supplier, times(2)).get();
    verify(underlyingWriter).close();
  }

  @Test
  void shouldDelegateUpdates() throws IOException {
    SharableIndexWriter writer = new SharableIndexWriter(() -> underlyingWriter);
    writer.open();

    Term term = new Term("field", "value");
    Document document = new Document();
    writer.updateDocument(term, document);

    verify(underlyingWriter).updateDocument(term, document);
  }

  @Test
  void shouldDelegateDeleteAll() throws IOException {
    SharableIndexWriter writer = new SharableIndexWriter(() -> underlyingWriter);
    writer.open();

    writer.deleteAll();

    verify(underlyingWriter).deleteAll();
  }

  @Test
  void shouldDelegateDeletes() throws IOException {
    SharableIndexWriter writer = new SharableIndexWriter(() -> underlyingWriter);
    writer.open();

    Term term = new Term("field", "value");
    writer.deleteDocuments(term);

    verify(underlyingWriter).deleteDocuments(term);
  }

  @Nested
  class ConcurrencyTests {

    private ExecutorService executorService;

    private final AtomicInteger openCounter = new AtomicInteger();
    private final AtomicInteger commitCounter = new AtomicInteger();
    private final AtomicInteger closeCounter = new AtomicInteger();

    private final AtomicInteger invocations = new AtomicInteger();

    private SharableIndexWriter writer;

    @BeforeEach
    void setUp() throws IOException {
      executorService = Executors.newFixedThreadPool(4);
      writer = new SharableIndexWriter(() -> {
        openCounter.incrementAndGet();
        return underlyingWriter;
      });

      doAnswer(ic -> commitCounter.incrementAndGet()).when(underlyingWriter).commit();
      doAnswer(ic -> closeCounter.incrementAndGet()).when(underlyingWriter).close();
    }

    @Test
    @SuppressWarnings("java:S2925") // sleep is ok to simulate some work
    void shouldKeepIndexOpen() {
      AtomicBoolean fail = new AtomicBoolean(false);
      for (int i = 0; i < 50; i++) {
        executorService.submit(() -> {
          writer.open();
          try {
            Thread.sleep(25);
            writer.deleteAll();
            writer.close();
          } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            fail.set(true);
          } catch (IOException e) {
            fail.set(true);
          } finally {
            invocations.incrementAndGet();
          }
        });
      }

      executorService.shutdown();

      await().atMost(2, TimeUnit.SECONDS).until(() -> invocations.get() == 50);

      assertThat(fail.get()).isFalse();

      // It should be one, but it is possible that tasks finish before new added to the queue.
      // This behaviour depends heavily on the cpu's of the machine which executes this test.
      assertThat(openCounter.get()).isPositive().isLessThan(10);
      // should be 49, but see comment above
      assertThat(commitCounter.get()).isGreaterThan(40);
      // should be 1, but see comment above
      assertThat(closeCounter.get()).isPositive().isLessThan(10);
    }

  }

}
