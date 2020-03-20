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
    
package sonia.scm.repository.spi;

import java.io.Closeable;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.function.Consumer;

import static sonia.scm.repository.spi.SyncAsyncExecutor.ExecutionType.ASYNCHRONOUS;
import static sonia.scm.repository.spi.SyncAsyncExecutor.ExecutionType.SYNCHRONOUS;

public final class SyncAsyncExecutors {

  public static SyncAsyncExecutor synchronousExecutor() {
    return new SyncAsyncExecutor() {
      @Override
      public ExecutionType execute(Consumer<ExecutionType> runnable, Runnable abortionFallback) {
        runnable.accept(SYNCHRONOUS);
        return SYNCHRONOUS;
      }

      @Override
      public boolean hasExecutedAllSynchronously() {
        return true;
      }
    };
  }

  public static SyncAsyncExecutor asynchronousExecutor() {

    Executor executor = Executors.newSingleThreadExecutor();

    return new SyncAsyncExecutor() {
      @Override
      public ExecutionType execute(Consumer<ExecutionType> runnable, Runnable abortionFallback) {
        executor.execute(() -> runnable.accept(ASYNCHRONOUS));
        return ASYNCHRONOUS;
      }

      @Override
      public boolean hasExecutedAllSynchronously() {
        return true;
      }
    };
  }

  public static AsyncExecutorStepper stepperAsynchronousExecutor() {
    return new AsyncExecutorStepper() {

      Executor executor = Executors.newSingleThreadExecutor();
      Semaphore enterSemaphore = new Semaphore(0);
      Semaphore exitSemaphore = new Semaphore(0);
      boolean timedOut = false;

      @Override
      public void close() {
        enterSemaphore.release(Integer.MAX_VALUE/2);
        exitSemaphore.release(Integer.MAX_VALUE/2);
      }

      @Override
      public ExecutionType execute(Consumer<ExecutionType> runnable, Runnable abortionFallback) {
        executor.execute(() -> {
          try {
            enterSemaphore.acquire();
          } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
          }
          if (timedOut) {
            abortionFallback.run();
          } else {
            runnable.accept(ASYNCHRONOUS);
            exitSemaphore.release();
          }
        });
        return ASYNCHRONOUS;
      }

      @Override
      public void next() {
        enterSemaphore.release();
        try {
          exitSemaphore.acquire();
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
        }
      }

      @Override
      public void timeout() {
        timedOut = true;
        close();
      }

      @Override
      public boolean hasExecutedAllSynchronously() {
        return true;
      }
    };
  }

  public interface AsyncExecutorStepper extends SyncAsyncExecutor, Closeable {
    void next();

    void timeout();
  }
}
