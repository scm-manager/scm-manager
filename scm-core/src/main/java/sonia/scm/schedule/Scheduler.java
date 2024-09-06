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

package sonia.scm.schedule;

import java.io.Closeable;

/**
 * Scheduler is able to run tasks on the future in a background thread. Task can be scheduled with cron like expression.
 * <strong>Note:</strong> Task are always executed in an administrative context.
 * @since 1.47
 */
public interface Scheduler extends Closeable {

  /**
   * Schedule a new task for future execution.
   * 
   * @param expression cron expression
   * @param runnable action
   * 
   * @return cancelable task
   */
  public Task schedule(String expression, Runnable runnable);

  /**
   * Schedule a new task for future execution. The method will create a new instance of the runnable for every 
   * execution. The runnable can use injection.
   * 
   * @param expression cron expression
   * @param runnable action class
   * 
   * @return cancelable task
   */  
  public Task schedule(String expression, Class<? extends Runnable> runnable);
  
}
