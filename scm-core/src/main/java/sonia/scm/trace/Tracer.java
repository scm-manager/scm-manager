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

import jakarta.inject.Inject;

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
      exporter.export(span);
    }
  }
}
