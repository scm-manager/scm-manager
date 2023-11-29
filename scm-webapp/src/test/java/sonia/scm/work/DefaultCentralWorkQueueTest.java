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

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Inject;
import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import jakarta.annotation.Nonnull;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.SimplePrincipalCollection;
import org.github.sdorra.jse.ShiroExtension;
import org.github.sdorra.jse.SubjectAware;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.repository.Repository;
import sonia.scm.security.Authentications;

import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.Mockito.when;

@SubjectAware("trillian")
@ExtendWith({MockitoExtension.class, ShiroExtension.class})
class DefaultCentralWorkQueueTest {

  private final PrincipalCollection principal = new SimplePrincipalCollection("trillian", "test");

  private static final int ITERATIONS = 50;
  private static final int TIMEOUT = 1; // seconds

  @Mock
  private Persistence persistence;

  @Nested
  class WithDefaultInjector {

    private MeterRegistry meterRegistry;
    private DefaultCentralWorkQueue queue;

    @BeforeEach
    void setUp() {
      meterRegistry = new SimpleMeterRegistry();
      queue = new DefaultCentralWorkQueue(4, Guice.createInjector(new SecurityModule()), persistence, meterRegistry);
    }

    private final AtomicInteger runs = new AtomicInteger();
    private int counter = 0;
    private int copy = -1;

    @Test
    void shouldRunInSequenceWithBlock() {
      for (int i = 0; i < ITERATIONS; i++) {
        queue.append().locks("counter").enqueue(new Increase());
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
        queue.append().locks("counter").enqueue(new Increase());
      }
      queue.append().enqueue(() -> copy = counter);
      waitForTasks();

      assertThat(counter).isEqualTo(ITERATIONS);
      assertThat(copy).isNotNegative().isLessThan(ITERATIONS);
    }

    @Test
    void shouldNotBlockedByDifferentResource() {
      for (int i = 0; i < ITERATIONS; i++) {
        queue.append().locks("counter").enqueue(new Increase());
      }
      queue.append().locks("copy").enqueue(() -> copy = counter);
      waitForTasks();

      assertThat(counter).isEqualTo(ITERATIONS);
      assertThat(copy).isNotNegative().isLessThan(ITERATIONS);
    }

    @Test
    void shouldBeBlockedByParentResource() {
      for (int i = 0; i < ITERATIONS; i++) {
        queue.append().locks("counter").enqueue(new Increase());
      }
      queue.append().locks("counter", "one").enqueue(() -> copy = counter);
      waitForTasks();

      assertThat(counter).isEqualTo(ITERATIONS);
      assertThat(copy).isEqualTo(ITERATIONS);
    }

    @Test
    void shouldBeBlockedByParentAndExactResource() {
      for (int i = 0; i < ITERATIONS; i++) {
        if (i % 2 == 0) {
          queue.append().locks("counter", "c").enqueue(new Increase());
        } else {
          queue.append().locks("counter").enqueue(new Increase());
        }
      }
      waitForTasks();
      assertThat(counter).isEqualTo(ITERATIONS);
    }

    @Test
    void shouldBeBlockedByParentResourceWithModelObject() {
      Repository one = repository("one");
      for (int i = 0; i < ITERATIONS; i++) {
        queue.append().locks("counter").enqueue(new Increase());
      }
      queue.append().locks("counter", one).enqueue(() -> copy = counter);
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

    @Test
    void shouldSetThreadName() {
      AtomicReference<String> threadName = new AtomicReference<>();
      queue.append().enqueue(() -> threadName.set(Thread.currentThread().getName()));
      await().atMost(TIMEOUT, TimeUnit.SECONDS).until(() -> threadName.get() != null);

      assertThat(threadName.get()).startsWith("CentralWorkQueue");
    }

    @Test
    void shouldCaptureExecutorMetrics() {
      for (int i = 0; i < ITERATIONS; i++) {
        queue.append().enqueue(new Increase());
      }
      waitForTasks();

      double count = meterRegistry.get("executor.completed").functionCounter().count();
      assertThat(count).isEqualTo(ITERATIONS);
    }

    @Test
    void shouldCaptureExecutionDuration() {
      queue.append().enqueue(new Increase());
      await().atMost(TIMEOUT, TimeUnit.SECONDS).until(() -> queue.getSize() == 0);

      Timer timer = meterRegistry.get(UnitOfWork.METRIC_EXECUTION).timer();
      assertThat(timer.count()).isEqualTo(1);
    }

    @Test
    void shouldCaptureWaitDuration() {
      queue.append().enqueue(new Increase());
      await().atMost(TIMEOUT, TimeUnit.SECONDS).until(() -> queue.getSize() == 0);

      Timer timer = meterRegistry.get(UnitOfWork.METRIC_WAIT).timer();
      assertThat(timer.count()).isEqualTo(1);
    }

    @Test
    void shouldIncreaseBlockCount() {
      for (int i = 0; i < ITERATIONS; i++) {
        queue.append().locks("counter").enqueue(new Increase());
      }
      waitForTasks();

      int blockCount = 0;
      for (Meter meter : meterRegistry.getMeters()) {
        Meter.Id id = meter.getId();
        if ("cwq.task.wait.duration".equals(id.getName())) {
          String blocked = id.getTag("blocked");
          if (blocked != null) {
            blockCount += Integer.parseInt(blocked);
          }
        }
      }

      assertThat(blockCount).isPositive();
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
      Guice.createInjector(new SecurityModule(), binder -> binder.bind(Context.class).toInstance(ctx)),
      persistence,
      new SimpleMeterRegistry(),
      () -> 2
    );

    queue.append().enqueue(InjectingTask.class);
    await().atMost(TIMEOUT, TimeUnit.SECONDS).until(() -> ctx.value != null);
    assertThat(ctx.value).isEqualTo("Hello");
  }

  @Test
  void shouldLoadFromPersistence() {
    Context context = new Context();
    SimpleUnitOfWork one = new SimpleUnitOfWork(
      21L, principal, Collections.singleton(new Resource("a")), new InjectingTask(context, "one")
    );
    SimpleUnitOfWork two = new SimpleUnitOfWork(
      42L, principal, Collections.singleton(new Resource("a")), new InjectingTask(context, "two")
    );
    two.restore(42L);
    when(persistence.loadAll()).thenReturn(Arrays.asList(one, two));

    new DefaultCentralWorkQueue(4, Guice.createInjector(new SecurityModule()), persistence, new SimpleMeterRegistry());

    await().atMost(TIMEOUT, TimeUnit.SECONDS).until(() -> context.value != null);
    assertThat(context.value).isEqualTo("two");
    assertThat(one.getOrder()).isEqualTo(1L);
    assertThat(one.getRestoreCount()).isEqualTo(1);
    assertThat(two.getOrder()).isEqualTo(2L);
    assertThat(two.getRestoreCount()).isEqualTo(2);
  }

  @Test
  void shouldRunAsUser() {
    DefaultCentralWorkQueue workQueue = new DefaultCentralWorkQueue(
      4, Guice.createInjector(new SecurityModule()), persistence, new SimpleMeterRegistry()
    );

    AtomicReference<Object> ref = new AtomicReference<>();
    workQueue.append().enqueue(() -> ref.set(SecurityUtils.getSubject().getPrincipal()));
    await().atMost(1, TimeUnit.SECONDS).until(() -> "trillian".equals(ref.get()));
  }

  @Test
  void shouldRunAsAdminUser() {
    DefaultCentralWorkQueue workQueue = new DefaultCentralWorkQueue(
      4, Guice.createInjector(new SecurityModule()), persistence, new SimpleMeterRegistry()
    );

    AtomicReference<Object> ref = new AtomicReference<>();
    workQueue.append().runAsAdmin().enqueue(() -> ref.set(SecurityUtils.getSubject().getPrincipal()));
    await().atMost(1, TimeUnit.SECONDS).until(() -> Authentications.PRINCIPAL_SYSTEM.equals(ref.get()));
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

  public static class SecurityModule extends AbstractModule {

    @Override
    protected void configure() {
      bind(SecurityManager.class).toInstance(SecurityUtils.getSecurityManager());
    }
  }

}
