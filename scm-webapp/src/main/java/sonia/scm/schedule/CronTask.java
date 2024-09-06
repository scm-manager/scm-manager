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
