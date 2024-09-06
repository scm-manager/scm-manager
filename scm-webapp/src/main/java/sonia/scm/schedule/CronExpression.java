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

import com.cronutils.model.Cron;
import com.cronutils.model.CronType;
import com.cronutils.model.definition.CronDefinition;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.model.time.ExecutionTime;
import com.cronutils.parser.CronParser;

import java.time.Clock;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.Optional;

final class CronExpression {

  private final Clock clock;
  private final String expression;
  private final ExecutionTime executionTime;

  CronExpression(String expression) {
    this(Clock.systemUTC(), expression);
  }

  CronExpression(Clock clock, String expression) {
    this.clock = clock;
    this.expression = expression;
    executionTime = createExecutionTime(expression);
  }

  boolean shouldRun(ZonedDateTime time) {
    ZonedDateTime now = ZonedDateTime.now(clock);
    return time.isBefore(now) || time.isEqual(now);
  }

  Optional<ZonedDateTime> calculateNextRun() {
    ZonedDateTime now = ZonedDateTime.now(clock);
    Optional<ZonedDateTime> nextExecution = executionTime.nextExecution(now);
    if (nextExecution.isPresent()) {
      ZonedDateTime next = nextExecution.get();
      if (Duration.between(now, next).toMillis() < 1000) {
        return executionTime.nextExecution(now.plusSeconds(1L));
      }
    }
    return nextExecution;
  }

  private ExecutionTime createExecutionTime(String expression) {
    CronDefinition cronDefinition = CronDefinitionBuilder.instanceDefinitionFor(CronType.QUARTZ);
    CronParser parser = new CronParser(cronDefinition);
    Cron cron = parser.parse(expression);
    return ExecutionTime.forCron(cron);
  }

  @Override
  public String toString() {
    return expression;
  }
}
