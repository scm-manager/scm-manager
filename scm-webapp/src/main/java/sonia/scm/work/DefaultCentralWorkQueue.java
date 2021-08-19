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

import com.google.inject.Inject;
import com.google.inject.Injector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.ModelObject;

import javax.annotation.Nonnull;
import java.io.Closeable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.IntSupplier;

public class DefaultCentralWorkQueue implements CentralWorkQueue, Closeable {

  private static final Logger LOG = LoggerFactory.getLogger(DefaultCentralWorkQueue.class);

  private final List<UnitOfWork> queue = new ArrayList<>();
  private final List<String> currentlyBlocked = new CopyOnWriteArrayList<>();
  private final AtomicInteger size = new AtomicInteger();
  private final AtomicLong order = new AtomicLong();

  private final Injector injector;
  private final Persistence persistence;
  private final ExecutorService executor;

  @Inject
  public DefaultCentralWorkQueue(Injector injector, Persistence persistence) {
    this(injector, persistence, () -> Runtime.getRuntime().availableProcessors());
  }

  DefaultCentralWorkQueue(Injector injector, Persistence persistence, IntSupplier availableProcessorResolver) {
    this.injector = injector;
    this.persistence = persistence;
    // TODO metrics
    this.executor = Executors.newFixedThreadPool(poolSize(availableProcessorResolver));

    loadFromDisk();
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
      unitOfWork.setOrder(order.incrementAndGet());
      append(unitOfWork);
    }
    run();
  }

  private int poolSize(IntSupplier availableProcessorResolver) {
    if (availableProcessorResolver.getAsInt() > 1) {
      return 4;
    } else {
      return 2;
    }
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
      }
    }
  }

  private void run(UnitOfWork unitOfWork) {
    currentlyBlocked.addAll(unitOfWork.getBlocks());
    unitOfWork.init(injector, this::finalizeWork);
    LOG.trace("pass task {} to executor", unitOfWork);
    executor.execute(unitOfWork);
  }

  private synchronized void finalizeWork(UnitOfWork unitOfWork) {
    for (String block : unitOfWork.getBlocks()) {
      currentlyBlocked.remove(block);
    }
    persistence.remove(unitOfWork);
    run();
    int queueSize = size.decrementAndGet();
    LOG.debug("finish task, queue size is now {}", queueSize);
  }

  private boolean isRunnable(UnitOfWork unitOfWork) {
    for (String block : unitOfWork.getBlocks()) {
      if (currentlyBlocked.contains(block)) {
        LOG.trace("skip {}, because it is blocked by {}", unitOfWork, block);
        return false;
      }
    }
    for (String block : unitOfWork.getBlockedBy()) {
      LOG.trace("skip {}, because it is blocked by {}", unitOfWork, block);
      if (currentlyBlocked.contains(block)) {
        return false;
      }
    }
    return true;
  }

  private class DefaultEnqueue implements Enqueue {

    private final Set<String> blocks = new HashSet<>();
    private final Set<String> blockedBy = new HashSet<>();

    @Override
    public Enqueue blocks(String... tags) {
      blocks.addAll(Arrays.asList(tags));
      return this;
    }

    @Override
    public Enqueue blocks(ModelObject... objects) {
      for (ModelObject object : objects) {
        blocks.add(tag(object));
      }
      return this;
    }

    @Override
    public Enqueue blockedBy(String... tags) {
      blockedBy.addAll(Arrays.asList(tags));
      return this;
    }

    @Override
    public Enqueue blockedBy(ModelObject... objects) {
      for (ModelObject object : objects) {
        blockedBy.add(tag(object));
      }
      return this;
    }

    @Override
    public void enqueue(Task task) {
      appendAndRun(new SimpleUnitOfWork(order.incrementAndGet(), blocks, blockedBy, task));
    }

    @Override
    public void enqueue(Class<? extends Task> task) {
      appendAndRun(new InjectingUnitOfWork(order.incrementAndGet(), blocks, blockedBy, task));
    }

    @Nonnull
    private String tag(ModelObject object) {
      return object.getClass() + ":" + object.getId();
    }

    private synchronized void appendAndRun(UnitOfWork unitOfWork) {
      append(unitOfWork);
      run();
    }
  }
}
