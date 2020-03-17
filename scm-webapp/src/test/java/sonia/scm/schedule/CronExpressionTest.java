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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CronExpressionTest {

  @Mock
  private Clock clock;

  @BeforeEach
  void setUpClockMock() {
    when(clock.getZone()).thenReturn(ZoneId.systemDefault());
    when(clock.instant()).thenReturn(Instant.parse("2007-12-03T10:15:00.00Z"));
  }

  @Test
  void shouldCalculateTheNextRunIn30Seconds() {
    assertNextRun("30 * * * * ?", 30);
  }

  @Test
  void shouldCalculateTheNextRunIn10Seconds() {
    assertNextRun("0/10 * * * * ?", 10);
  }

  @Test
  void shouldReturnEmptyOptional() {
    CronExpression expression = new CronExpression(clock, "30 12 12 12 * ? 1985");

    Optional<ZonedDateTime> optionalNextRun = expression.calculateNextRun();
    assertThat(optionalNextRun).isNotPresent();
  }

  @Test
  void shouldReturnTrue() {
    ZonedDateTime time = ZonedDateTime.now(clock).minusSeconds(1L);

    CronExpression expression = new CronExpression(clock, "30 * * * * ?");
    assertThat(expression.shouldRun(time)).isTrue();
  }

  @Test
  void shouldReturnFalse() {
    ZonedDateTime time = ZonedDateTime.now(clock).plusSeconds(1L);

    CronExpression expression = new CronExpression(clock, "30 * * * * ?");
    assertThat(expression.shouldRun(time)).isFalse();
  }

  private void assertNextRun(String expressionAsString, long expectedSecondsToNextRun) {
    CronExpression expression = new CronExpression(clock, expressionAsString);

    Optional<ZonedDateTime> optionalNextRun = expression.calculateNextRun();
    assertThat(optionalNextRun).isPresent();

    ZonedDateTime nextRun = optionalNextRun.get();
    long nextRunInSeconds = Duration.between(ZonedDateTime.now(clock), nextRun).getSeconds();
    assertThat(nextRunInSeconds).isEqualTo(expectedSecondsToNextRun);
  }
}
