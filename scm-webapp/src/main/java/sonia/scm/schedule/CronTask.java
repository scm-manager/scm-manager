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

import com.cronutils.utils.VisibleForTesting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.concurrent.Future;

class CronTask implements Task, Runnable {

  private static final Logger LOG = LoggerFactory.getLogger(CronTask.class);

  private final String name;
  private final CronExpression expression;
  private final Runnable runnable;

  private ZonedDateTime nextRun;
  private Future<?> future;

  CronTask(String name, CronExpression expression, Runnable runnable) {
    this.name = name;
    this.expression = expression;
    this.runnable = runnable;
    this.nextRun = expression.calculateNextRun().orElse(null);
  }

  void setFuture(Future<?> future) {
    this.future = future;
  }

  @Override
  public synchronized void run() {
    if (hasNextRun() && expression.shouldRun(nextRun)) {
      LOG.debug("execute task {}, because of matching expression {}", name, expression);
      runnable.run();
      Optional<ZonedDateTime> next = expression.calculateNextRun();
      if (next.isPresent()) {
        nextRun = next.get();
      } else {
        LOG.debug("cancel task {}, because expression {} has no next execution", name, expression);
        nextRun = null;
        cancel();
      }
    } else {
      LOG.trace("skip execution of task {}, because expression {} does not match", name, expression);
    }
  }

  boolean hasNextRun() {
    return nextRun != null;
  }

  @VisibleForTesting
  String getName() {
    return name;
  }

  @VisibleForTesting
  CronExpression getExpression() {
    return expression;
  }

  @Override
  public synchronized void cancel() {
    LOG.debug("cancel task {} with expression {}", name, expression);
    future.cancel(false);
  }

  @Override
  public String toString() {
    return name + "(" + expression + ")";
  }
}
