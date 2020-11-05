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
