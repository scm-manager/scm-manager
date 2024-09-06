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

package sonia.scm.repository.spi;

/**
 * Use this provider to get {@link SyncAsyncExecutor} instances to execute a number of normally short-lived tasks, that
 * should be run asynchronously (or even be skipped) whenever they take too long in summary.
 * <p>
 * The goal of this is a "best effort" approach: The submitted tasks are run immediately when they are submitted, unless
 * a given timespan (<code>switchToAsyncInSeconds</code>) has passed. From this moment on the tasks are put into a queue to be
 * processed asynchronously. If even then they take too long and their accumulated asynchronous runtime exceeds another
 * limit (<code>maxAsyncAbortSeconds</code>), the tasks are skipped.
 * <p>
 * Note that whenever a task has been started either synchronously or asynchronously it will neither be terminated nor
 * switched from foreground to background execution, so this will only work well for short-living tasks. A long running
 * task can still block this for longer than the configured amount of seconds.
 */
public interface SyncAsyncExecutorProvider {

  int DEFAULT_SWITCH_TO_ASYNC_IN_SECONDS = 2;

  /**
   * Creates an {@link SyncAsyncExecutor} that will run tasks synchronously for
   * {@link #DEFAULT_SWITCH_TO_ASYNC_IN_SECONDS} seconds. The limit of asynchronous runtime is implementation dependant.
   *
   * @return The executor.
   */
  default SyncAsyncExecutor createExecutorWithDefaultTimeout() {
    return createExecutorWithSecondsToTimeout(DEFAULT_SWITCH_TO_ASYNC_IN_SECONDS);
  }

  /**
   * Creates an {@link SyncAsyncExecutor} that will run tasks synchronously for
   * <code>switchToAsyncInSeconds</code> seconds. The limit of asynchronous runtime is implementation dependant.
   *
   * @param switchToAsyncInSeconds The amount of seconds submitted tasks will be run synchronously. After this time,
   *                               further tasks will be run asynchronously. To run all tasks asynchronously no matter
   *                               what, set this to <code>0</code>.
   * @return The executor.
   */
  SyncAsyncExecutor createExecutorWithSecondsToTimeout(int switchToAsyncInSeconds);

  /**
   * Creates an {@link SyncAsyncExecutor} that will run tasks synchronously for
   * <code>switchToAsyncInSeconds</code> seconds and will abort tasks after they ran
   * <code>maxAsyncAbortSeconds</code> asynchronously.
   *
   * @param switchToAsyncInSeconds The amount of seconds submitted tasks will be run synchronously. After this time,
   *                               further tasks will be run asynchronously. To run all tasks asynchronously no matter
   *                               what, set this to <code>0</code>.
   * @param maxAsyncAbortSeconds   The amount of seconds, tasks that were started asynchronously may run in summary
   *                               before remaining tasks will not be executed at all anymore. To abort all tasks that
   *                               are submitted after <code>switchToAsyncInSeconds</code> immediately, set this to
   *                               <code>0</code>.
   * @return The executor.
   */
  SyncAsyncExecutor createExecutorWithSecondsToTimeout(int switchToAsyncInSeconds, int maxAsyncAbortSeconds);
}
