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

import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;

import java.util.Set;

/**
 * The tracer api allows the tracing of long running tasks, such as calling external systems.
 * The api is able to collect tracing points called spans.
 *
 * To use the tracer api inject the {@link Tracer} and open a span in a try with resources block e.g.:
 * <pre>
 *   try (Span span = tracer.span("jenkins").label("repository", "builds/core")) {
 *     Response response = jenkins.call("http://...");
 *     if (!response.isSuccess()) {
 *       span.label("reason", response.getFailedReason());
 *       span.failed();
 *     }
 *   }
 * </pre>
 *
 * As seen in the example we can mark span as failed and add more context to the span with labels.
 * After a span is closed it is delegated to an {@link Exporter}, which
 *
 * @since 2.9.0
 */
@Slf4j
public final class Tracer {

  private final Set<Exporter> exporters;

  @Inject
  public Tracer(Set<Exporter> exporters) {
    this.exporters = exporters;
  }

  /**
   * Creates a new span.
   * @param kind kind of span
   */
  public Span span(String kind) {
    return new Span(this, kind);
  }

  /**
   * Pass the finished span to the exporters.
   */
  void export(SpanContext span) {
    for (Exporter exporter : exporters) {
      try {
        exporter.export(span);
      } catch (Exception e) {
        log.warn("got error from trace exporter", e);
      }
    }
  }
}
