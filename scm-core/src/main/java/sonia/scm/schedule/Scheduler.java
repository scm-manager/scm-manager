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
