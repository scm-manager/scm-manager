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

import java.util.function.Consumer;

/**
 * Tasks submitted to this executor will be run synchronously up to a given time, after which they will be queued and
 * processed asynchronously. After a maximum amount of time consumed by these tasks, they will be skipped. Note that
 * this only works for short-living tasks.
 * <p>
 * Get instances of this using a {@link SyncAsyncExecutorProvider}.
 */
public interface SyncAsyncExecutor {

  /**
   * Execute the given task (either synchronously or asynchronously). If this task is skipped due to
   * timeouts, nothing will be done.
   *
   * @param task The {@link Runnable} to be executed.
   * @return Either {@link ExecutionType#SYNCHRONOUS} when the given {@link Runnable} has been executed immediately or
   * {@link ExecutionType#ASYNCHRONOUS}, when the task was queued to be executed asynchronously in the future.
   */
  default ExecutionType execute(Runnable task) {
    return execute(
      ignored -> task.run(),
      () -> {}
    );
  }

  /**
   * Execute the given <code>task</code> (either synchronously or asynchronously). If this task is
   * skipped due to timeouts, the <code>abortionFallback</code> will be called.
   *
   * @param task             The {@link Runnable} to be executed.
   * @param abortionFallback This will only be run, when this and all remaining tasks are aborted. This task should
   *                         only consume a negligible amount of time.
   * @return Either {@link ExecutionType#SYNCHRONOUS} when the given {@link Runnable} has been executed immediately or
   * {@link ExecutionType#ASYNCHRONOUS}, when the task was queued to be executed asynchronously in the future.
   */
  default ExecutionType execute(Runnable task, Runnable abortionFallback) {
    return execute(ignored -> task.run(), abortionFallback);
  }

  /**
   * Execute the given <code>task</code> (either synchronously or asynchronously). If this task is skipped due to
   * timeouts, nothing will be done.
   *
   * @param task The {@link Consumer} to be executed. The parameter given to this is either
   *             {@link ExecutionType#SYNCHRONOUS} when the given {@link Consumer} is executed immediately
   *             or {@link ExecutionType#ASYNCHRONOUS}, when the task had been queued and now is executed
   *             asynchronously.
   * @return Either {@link ExecutionType#SYNCHRONOUS} when the given {@link Runnable} has been executed immediately or
   * {@link ExecutionType#ASYNCHRONOUS}, when the task was queued to be executed asynchronously in the future.
   */
  default ExecutionType execute(Consumer<ExecutionType> task) {
    return execute(task, () -> {});
  }

  /**
   * Execute the given <code>task</code> (either synchronously or asynchronously). If this task is
   * skipped due to timeouts, the <code>abortionFallback</code> will be called.
   *
   * @param task             The {@link Consumer} to be executed. The parameter given to this is either
   *                         {@link ExecutionType#SYNCHRONOUS} when the given {@link Consumer} is executed immediately
   *                         or {@link ExecutionType#ASYNCHRONOUS}, when the task had been queued and now is executed
   *                         asynchronously.
   * @param abortionFallback This will only be run, when this and all remaining tasks are aborted. This task should
   *                         only consume a negligible amount of time.
   * @return Either {@link ExecutionType#SYNCHRONOUS} when the given {@link Runnable} has been executed immediately or
   * {@link ExecutionType#ASYNCHRONOUS}, when the task was queued to be executed asynchronously in the future.
   */
  ExecutionType execute(Consumer<ExecutionType> task, Runnable abortionFallback);

  /**
   * When all submitted tasks have been executed synchronously, this will return <code>true</code>. If at least one task
   * has been enqueued to be executed asynchronously, this returns <code>false</code> (even when none of the enqueued
   * tasks have been run, yet).
   */
  boolean hasExecutedAllSynchronously();

  enum ExecutionType {
    SYNCHRONOUS, ASYNCHRONOUS
  }
}
