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

import com.google.common.base.Stopwatch;
import com.google.inject.Injector;
import lombok.EqualsAndHashCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.Optional;
import java.util.Set;

@EqualsAndHashCode
abstract class UnitOfWork implements Runnable, Serializable, Comparable<UnitOfWork> {

  private static final Logger LOG = LoggerFactory.getLogger(UnitOfWork.class);

  private long order;
  private final Set<String> blocks;
  private final Set<String> blockedBy;
  private transient Finalizer finalizer;
  private transient Task task;
  private transient String storageId;

  protected UnitOfWork(long order, Set<String> blocks, Set<String> blockedBy) {
    this.order = order;
    this.blocks = blocks;
    this.blockedBy = blockedBy;
  }

  public long getOrder() {
    return order;
  }

  public void setOrder(long order) {
    this.order = order;
  }

  public void setStorageId(String storageId) {
    this.storageId = storageId;
  }

  public Optional<String> getStorageId() {
    return Optional.ofNullable(storageId);
  }

  public Set<String> getBlockedBy() {
    return blockedBy;
  }

  public Set<String> getBlocks() {
    return blocks;
  }

  void init(Injector injector, Finalizer finalizer) {
    this.task = task(injector);
    this.finalizer = finalizer;
  }

  protected abstract Task task(Injector injector);

  @Override
  public void run() {
    Stopwatch sw = Stopwatch.createStarted();
    try {
      task.run();
      LOG.debug("task {} finished successful after {}", task, sw.stop());
    } catch (Exception ex) {
      LOG.error("task {} failed after {}", task, sw.stop(), ex);
    } finally {
      finalizer.finalizeWork(this);
    }
  }

  @Override
  public int compareTo(UnitOfWork o) {
    return Long.compare(order, o.order);
  }
}
