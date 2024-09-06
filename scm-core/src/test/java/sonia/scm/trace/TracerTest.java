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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class TracerTest {

  @Nested
  class WithNormalExporter {
    private Tracer tracer;
    private CollectingExporter exporter;

    @BeforeEach
    void setUpTracer() {
      exporter = new CollectingExporter();
      tracer = new Tracer(Collections.singleton(exporter));
    }

    @Test
    void shouldReturnSpan() {
      tracer.span("sample").close();

      SpanContext span = exporter.spans.get(0);
      assertThat(span.getKind()).isEqualTo("sample");
      assertThat(span.getOpened()).isNotNull();
      assertThat(span.getClosed()).isNotNull();
      assertThat(span.isFailed()).isFalse();
    }

    @Test
    @SuppressWarnings("java:S2925") // it is ok, to use sleep here
    void shouldReturnPositiveDuration() throws InterruptedException {
      try (Span span = tracer.span("sample")) {
        span.label("l1", "one");
        Thread.sleep(1L);
      }

      SpanContext span = exporter.spans.get(0);
      assertThat(span.duration()).isPositive();
    }

    @Test
    void shouldConvertLabels() {
      try (Span span = tracer.span("sample")) {
        span.label("int", 21);
        span.label("long", 42L);
        span.label("float", 21.0f);
        span.label("double", 42.0d);
        span.label("boolean", true);
        span.label("object", new StringWrapper("value"));
      }

      Map<String, String> labels = exporter.spans.get(0).getLabels();
      assertThat(labels)
        .containsEntry("int", "21")
        .containsEntry("long", "42")
        .containsEntry("float", "21.0")
        .containsEntry("double", "42.0")
        .containsEntry("boolean", "true")
        .containsEntry("object", "value");
    }

    @Test
    void shouldReturnFailedSpan() {
      try (Span span = tracer.span("failing")) {
        span.failed();
      }

      SpanContext span = exporter.spans.get(0);
      assertThat(span.getKind()).isEqualTo("failing");
      assertThat(span.isFailed()).isTrue();
    }

    private static class StringWrapper {

      private final String value;

      public StringWrapper(String value) {
        this.value = value;
      }

      @Override
      public String toString() {
        return value;
      }
    }
  }

  @Test
  void shouldNotFailIfExporterFails() {
    CollectingExporter exporterThatShouldBeCalled = new CollectingExporter();
    Exporter failingExporter = span -> {
      throw new RuntimeException("this should not break everything");
    };

    Tracer tracer = new Tracer(Set.of(failingExporter, exporterThatShouldBeCalled));
    Span span = tracer.span("test");
    span.close();

    assertThat(exporterThatShouldBeCalled.spans).isNotEmpty();
  }

  public static class CollectingExporter implements Exporter {

    private final List<SpanContext> spans = new ArrayList<>();

    @Override
    public void export(SpanContext spanContext) {
      spans.add(spanContext);
    }
  }
}
