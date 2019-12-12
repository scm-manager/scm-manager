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
