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

package sonia.scm.work;

import com.google.inject.Guice;
import com.google.inject.Inject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.repository.Repository;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DefaultCentralWorkQueueTest {

  private static final int ITERATIONS = 50;
  private static final int TIMEOUT = 1; // seconds

  @Mock
  private Persistence persistence;

  @Nested
  class WithDefaultInjector {

    private DefaultCentralWorkQueue queue;

    @BeforeEach
    void setUp() {
      queue = new DefaultCentralWorkQueue(Guice.createInjector(), persistence);
    }

    private final AtomicInteger runs = new AtomicInteger();
    private int counter = 0;
    private int copy = -1;

    @Test
    void shouldRunInSequenceWithBlock() {
      for (int i = 0; i < ITERATIONS; i++) {
        queue.append().blocks("counter").enqueue(new Increase());
      }
      waitForTasks();

      assertThat(counter).isEqualTo(ITERATIONS);
    }

    private void waitForTasks() {
      await().atMost(TIMEOUT, TimeUnit.SECONDS).until(() -> queue.getSize() == 0);
      assertThat(runs.get()).isEqualTo(ITERATIONS);
    }

    @Test
    void shouldRunInParallel() {
      for (int i = 0; i < ITERATIONS; i++) {
        queue.append().enqueue(new Increase());
      }
      waitForTasks();

      // we test if the resulting counter is less than the iteration,
      // because it is extremely likely that we miss a counter update
      // when we run in parallel
      assertThat(counter)
        .isPositive()
        .isLessThan(ITERATIONS);
    }

    @Test
    void shouldNotBlocked() {
      for (int i = 0; i < ITERATIONS; i++) {
        queue.append().blocks("counter").enqueue(new Increase());
      }
      queue.append().enqueue(() -> copy = counter);
      waitForTasks();

      assertThat(counter).isEqualTo(ITERATIONS);
      assertThat(copy).isNotNegative().isLessThan(ITERATIONS);
    }

    @Test
    void shouldBeBlockedWithBlockedBy() {
      for (int i = 0; i < ITERATIONS; i++) {
        queue.append().blocks("counter").enqueue(new Increase());
      }
      queue.append().blockedBy("counter").enqueue(() -> copy = counter);
      waitForTasks();

      assertThat(counter).isEqualTo(ITERATIONS);
      assertThat(copy).isEqualTo(ITERATIONS);
    }

    @Test
    void shouldBlockedByModelObject() {
      Repository one = repository("one");
      for (int i = 0; i < ITERATIONS; i++) {
        queue.append().blocks(one).enqueue(new Increase());
      }
      queue.append().blockedBy(one).enqueue(() -> copy = counter);
      waitForTasks();

      assertThat(counter).isEqualTo(ITERATIONS);
      assertThat(copy).isEqualTo(ITERATIONS);
    }

    @Test
    void shouldFinalizeOnError() {
      queue.append().enqueue(() -> {
        throw new IllegalStateException("failed");
      });

      await().atMost(TIMEOUT, TimeUnit.SECONDS).until(() -> queue.getSize() == 0);
    }

    @Nonnull
    private Repository repository(String id) {
      Repository one = new Repository();
      one.setId(id);
      return one;
    }

    @AfterEach
    void tearDown() {
      queue.close();
    }

    private class Increase implements Task {

      @Override
      @SuppressWarnings("java:S2925")
      public void run() {
        int currentCounter = counter;
        runs.incrementAndGet();
        try {
          Thread.sleep(5);
          counter = currentCounter + 1;
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      }
    }
  }

  @Test
  void shouldInjectDependencies() {
    Context ctx = new Context();
    DefaultCentralWorkQueue queue = new DefaultCentralWorkQueue(
      Guice.createInjector(binder -> binder.bind(Context.class).toInstance(ctx)),
      persistence,
      () -> 1
    );

    queue.append().enqueue(InjectingTask.class);
    await().atMost(TIMEOUT, TimeUnit.SECONDS).until(() -> ctx.value != null);
    assertThat(ctx.value).isEqualTo("Hello");
  }

  @Test
  void shouldLoadFromPersistence() {
    Context context = new Context();
    SimpleChunkOfWork one = new SimpleChunkOfWork(
      21L, Collections.singleton("a"), Collections.emptySet(), new InjectingTask(context, "one")
    );
    SimpleChunkOfWork two =new SimpleChunkOfWork(
      42L, Collections.singleton("a"), Collections.emptySet(), new InjectingTask(context, "two")
    );
    when(persistence.loadAll()).thenReturn(Arrays.asList(one, two));

    new DefaultCentralWorkQueue(Guice.createInjector(), persistence);

    await().atMost(TIMEOUT, TimeUnit.SECONDS).until(() -> context.value != null);
    assertThat(context.value).isEqualTo("two");
    assertThat(one.getOrder()).isEqualTo(1L);
    assertThat(two.getOrder()).isEqualTo(2L);
  }

  public static class Context {

    private String value;

    public void setValue(String value) {
      this.value = value;
    }
  }

  public static class InjectingTask implements Task {

    private final Context context;
    private final String value;

    @Inject
    public InjectingTask(Context context) {
      this(context, "Hello");
    }

    public InjectingTask(Context context, String value) {
      this.context = context;
      this.value = value;
    }

    @Override
    public void run() {
      context.setValue(value);
    }
  }

}
