/*
 * Copyright (c) 2020 - present Cloudogu GmbH
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

package sonia.scm.store;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

/** Runs competing operations at a controlled backing-store or auditor boundary. */
public final class ConcurrentStoreOperations {

  private final CountDownLatch paused = new CountDownLatch(1);
  private final CountDownLatch resume = new CountDownLatch(1);
  private Thread firstThread;

  public void pauseFirstOperation() {
    if (Thread.currentThread() != firstThread) {
      return;
    }
    paused.countDown();
    try {
      assertThat(resume.await(5, TimeUnit.SECONDS)).as("first operation released").isTrue();
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new AssertionError(e);
    }
  }

  public void run(Runnable first, Runnable second) throws Exception {
    FutureTask<Void> firstTask = new FutureTask<>(first, null);
    FutureTask<Void> secondTask = new FutureTask<>(second, null);
    firstThread = createThread(firstTask);
    Thread secondThread = createThread(secondTask);
    try {
      firstThread.start();
      assertThat(paused.await(5, TimeUnit.SECONDS)).as("first operation reached boundary").isTrue();
      secondThread.start();
      try {
        awaitCompletionOrContention(secondThread);
      } finally {
        resume.countDown();
      }
      firstTask.get(5, TimeUnit.SECONDS);
      secondTask.get(5, TimeUnit.SECONDS);
    } finally {
      resume.countDown();
      firstTask.cancel(true);
      secondTask.cancel(true);
      firstThread.join(5000);
      secondThread.join(5000);
    }
  }

  private void awaitCompletionOrContention(Thread secondThread) throws InterruptedException {
    long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
    while (secondThread.isAlive()) {
      ThreadInfo info = ManagementFactory.getThreadMXBean().getThreadInfo(secondThread.threadId());
      if (info != null && info.getThreadState() == Thread.State.BLOCKED
        && info.getLockOwnerId() == firstThread.threadId()) {
        return;
      }
      assertThat(System.nanoTime()).as("competing operation completed or blocked on first operation").isLessThan(deadline);
      Thread.sleep(1);
    }
  }

  private Thread createThread(Runnable task) {
    Thread thread = new Thread(task);
    thread.setDaemon(true);
    return thread;
  }
}
