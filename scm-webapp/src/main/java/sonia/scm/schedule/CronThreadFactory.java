/**
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
package sonia.scm.schedule;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.apache.shiro.util.ThreadContext;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicLong;

/**
 * This thread factory creates threads without a shiro context.
 * This is to avoid classloader leaks, because the {@link ThreadContext} of shiro uses {@link InheritableThreadLocal},
 * which could bind a class with a reference to a {@link sonia.scm.plugin.PluginClassLoader}.
 */
class CronThreadFactory implements ThreadFactory, AutoCloseable {

  private static final String NAME_TEMPLATE = "CronScheduler-%d-%d";

  private static final AtomicLong FACTORY_COUNTER = new AtomicLong();

  private final ExecutorService executorService = Executors.newSingleThreadExecutor(
    new ThreadFactoryBuilder().setNameFormat("CronThreadFactory-%d").build()
  );

  private final long factoryId = FACTORY_COUNTER.incrementAndGet();
  private final AtomicLong threadCounter = new AtomicLong();

  @Override
  public Thread newThread(final Runnable r) {
    try {
      return executorService.submit(() -> {
        ThreadContext.remove();
        return new Thread(r, createName());
      }).get();
    } catch (InterruptedException ex) {
      Thread.currentThread().interrupt();
      throw new IllegalStateException("failed to schedule runnable");
    } catch (ExecutionException ex) {
      throw new IllegalStateException("failed to schedule runnable");
    }
  }

  private String createName() {
    long threadId = threadCounter.incrementAndGet();
    return String.format(NAME_TEMPLATE, factoryId, threadId);
  }

  @Override
  public void close()  {
    executorService.shutdown();
  }
}
