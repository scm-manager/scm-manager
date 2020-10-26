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

import java.time.Clock;
import java.time.Instant;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A span represents a single unit of work e.g. a request to an external system.
 *
 * @since 2.9.0
 */
public final class Span implements AutoCloseable {

  private final Clock clock;
  private final Tracer tracer;
  private final String kind;
  private final Map<String,String> labels = new LinkedHashMap<>();
  private final Instant opened;
  private boolean failed;

  Span(Tracer tracer, String kind) {
    this(tracer, kind, Clock.systemUTC());
  }

  Span(Tracer tracer, String kind, Clock clock) {
    this.clock = clock;
    this.tracer = tracer;
    this.kind = kind;
    this.opened = clock.instant();
  }

  /**
   * Adds a label to the span.
   *
   * @param key key of label
   * @param value label value
   * @return {@code this}
   */
  public Span label(String key, String value) {
    labels.put(key, value);
    return this;
  }

  /**
   * Adds a label to the span.
   * @param key key of label
   * @param value label value
   * @return {@code this}
   */
  public Span label(String key, int value) {
    return label(key, String.valueOf(value));
  }

  /**
   * Adds a label to the span.
   * @param key key of label
   * @param value label value
   * @return {@code this}
   */
  public Span label(String key, long value) {
    return label(key, String.valueOf(value));
  }

  /**
   * Adds a label to the span.
   * @param key key of label
   * @param value label value
   * @return {@code this}
   */
  public Span label(String key, float value) {
    return label(key, String.valueOf(value));
  }

  /**
   * Adds a label to the span.
   * @param key key of label
   * @param value label value
   * @return {@code this}
   */
  public Span label(String key, double value) {
    return label(key, String.valueOf(value));
  }

  /**
   * Adds a label to the span.
   * @param key key of label
   * @param value label value
   * @return {@code this}
   */
  public Span label(String key, boolean value) {
    return label(key, String.valueOf(value));
  }

  /**
   * Adds a label to the span.
   * @param key key of label
   * @param value label value
   * @return {@code this}
   */
  public Span label(String key, Object value) {
    return label(key, String.valueOf(value));
  }

  /**
   * Marks the span as failed.
   *
   * @return {@code this}
   */
  public Span failed() {
    failed = true;
    return this;
  }

  /**
   * Closes the span a reports the context to the {@link Tracer}.
   */
  @Override
  public void close() {
    tracer.export(new SpanContext(kind, Collections.unmodifiableMap(labels), opened, clock.instant(), failed));
  }

}
