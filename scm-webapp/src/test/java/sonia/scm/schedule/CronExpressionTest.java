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
