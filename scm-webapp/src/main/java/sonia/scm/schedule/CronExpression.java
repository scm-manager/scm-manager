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
