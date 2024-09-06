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

package sonia.scm.trace;

import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

class LoggingExporterTest {

  private String message;

  private LoggingExporter exporter;

  @BeforeEach
  void setUpLogger() {
    exporter = new LoggingExporter((message) -> this.message = message);
  }

  @Test
  void shouldLogTheSpanKind() {
    exporter.export(new SpanContext(
      "AwesomeSpanKind", Collections.emptyMap(), Instant.now(), Instant.now(), false
    ));

    assertThat(message).contains("AwesomeSpanKind");
  }

  @Test
  void shouldLogFailed() {
    exporter.export(new SpanContext(
      "sample", Collections.emptyMap(), Instant.now(), Instant.now(), true
    ));

    assertThat(message).contains("failed");
  }

  @Test
  void shouldLogDuration() {
    Instant opened = Instant.now();
    exporter.export(new SpanContext(
      "sample", ImmutableMap.of(), opened, opened.plusMillis(42L), false
    ));

    assertThat(message).contains("42ms");
  }

  @Test
  void shouldLogLabels() {
    exporter.export(new SpanContext(
      "sample", ImmutableMap.of("l1", "v1", "l2", "v2"), Instant.now(), Instant.now(), false
    ));

    assertThat(message)
      .contains("l1")
      .contains("v1")
      .contains("l2")
      .contains("v2");
  }

}
