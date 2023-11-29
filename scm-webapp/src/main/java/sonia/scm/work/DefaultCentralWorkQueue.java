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

import com.google.common.annotations.VisibleForTesting;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.inject.Inject;
import com.google.inject.Injector;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.annotation.Nullable;
import jakarta.inject.Singleton;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.PrincipalCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.config.ConfigValue;
import sonia.scm.metrics.Metrics;
import sonia.scm.web.security.DefaultAdministrationContext;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.IntSupplier;

@Singleton
public class DefaultCentralWorkQueue implements CentralWorkQueue, Closeable {

  private static final Logger LOG = LoggerFactory.getLogger(DefaultCentralWorkQueue.class);

  private final List<UnitOfWork> queue = new ArrayList<>();
  private final List<Resource> lockedResources = new ArrayList<>();
  private final AtomicInteger size = new AtomicInteger();
  private final AtomicLong order = new AtomicLong();

  private final Injector injector;
  private final Persistence persistence;
  private final ExecutorService executor;
  private final MeterRegistry meterRegistry;

  @Inject
  public DefaultCentralWorkQueue(
    @ConfigValue(key = "centralWorkQueue.workers", defaultValue = "8", description = "") Integer workers,
    Injector injector,
    Persistence persistence,
    MeterRegistry meterRegistry
  ) {
    this(injector, persistence, meterRegistry, new ThreadCountProvider(workers));
  }

  @VisibleForTesting
  DefaultCentralWorkQueue(Injector injector, Persistence persistence, MeterRegistry meterRegistry, IntSupplier threadCountProvider) {
    this.injector = injector;
    this.persistence = persistence;
    this.executor = createExecutorService(meterRegistry, threadCountProvider.getAsInt());
    this.meterRegistry = meterRegistry;

    loadFromDisk();
  }

  private static ExecutorService createExecutorService(MeterRegistry registry, int threadCount) {
    ExecutorService executorService = Executors.newFixedThreadPool(
      threadCount,
      new ThreadFactoryBuilder()
        .setNameFormat("CentralWorkQueue-%d")
        .build()
    );
    Metrics.executor(registry, executorService, "CentralWorkQueue", "fixed");
    return executorService;
  }

  @Override
  public Enqueue append() {
    return new DefaultEnqueue();
  }

  @Override
  public int getSize() {
    return size.get();
  }

  @Override
  public void close() {
    executor.shutdown();
  }

  private void loadFromDisk() {
    for (UnitOfWork unitOfWork : persistence.loadAll()) {
      unitOfWork.restore(order.incrementAndGet());
      append(unitOfWork);
    }
    run();
  }

  private synchronized void append(UnitOfWork unitOfWork) {
    persistence.store(unitOfWork);
    int queueSize = size.incrementAndGet();
    queue.add(unitOfWork);
    LOG.debug("add task {} to queue, queue size is now {}", unitOfWork, queueSize);
  }

  private synchronized void run() {
    Iterator<UnitOfWork> iterator = queue.iterator();
    while (iterator.hasNext()) {
      UnitOfWork unitOfWork = iterator.next();
      if (isRunnable(unitOfWork)) {
        run(unitOfWork);
        iterator.remove();
      } else {
        unitOfWork.blocked();
      }
    }
  }

  private void run(UnitOfWork unitOfWork) {
    lockedResources.addAll(unitOfWork.getLocks());
    unitOfWork.init(injector, this::finalizeWork, meterRegistry);
    LOG.trace("pass task {} to executor", unitOfWork);
    executor.execute(unitOfWork);
  }

  private synchronized void finalizeWork(UnitOfWork unitOfWork) {
    for (Resource lock : unitOfWork.getLocks()) {
      lockedResources.remove(lock);
    }
    persistence.remove(unitOfWork);

    int queueSize = size.decrementAndGet();
    LOG.debug("finish task, queue size is now {}", queueSize);

    run();
  }

  private boolean isRunnable(UnitOfWork unitOfWork) {
    for (Resource resource : unitOfWork.getLocks()) {
      for (Resource lock : lockedResources) {
        if (resource.isBlockedBy(lock)) {
          LOG.trace("skip {}, because resource {} is locked by {}", unitOfWork, resource, lock);
          return false;
        }
      }
    }
    return true;
  }

  private class DefaultEnqueue implements Enqueue {

    private final Set<Resource> locks = new HashSet<>();
    private boolean runAsAdmin = false;

    @Override
    public Enqueue locks(String resourceType) {
      locks.add(new Resource(resourceType));
      return this;
    }

    @Override
    public Enqueue locks(String resource, @Nullable String id) {
      locks.add(new Resource(resource, id));
      return this;
    }

    @Override
    public Enqueue runAsAdmin() {
      this.runAsAdmin = true;
      return this;
    }

    @Override
    public void enqueue(Task task) {
      appendAndRun(new SimpleUnitOfWork(order.incrementAndGet(), principal(), locks, task));
    }

    @Override
    public void enqueue(Class<? extends Runnable> task) {
      appendAndRun(new InjectingUnitOfWork(order.incrementAndGet(), principal(), locks, task));
    }

    private PrincipalCollection principal() {
      if (runAsAdmin) {
        return DefaultAdministrationContext.createAdminPrincipal();
      }
      return SecurityUtils.getSubject().getPrincipals();
    }

    private synchronized void appendAndRun(UnitOfWork unitOfWork) {
      append(unitOfWork);
      run();
    }
  }
}
