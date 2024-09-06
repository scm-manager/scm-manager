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

  private final Tracer tracer;
  private final String kind;
  private final Map<String,String> labels = new LinkedHashMap<>();
  private final Instant opened;
  private boolean failed;

  Span(Tracer tracer, String kind) {
    this.tracer = tracer;
    this.kind = kind;
    this.opened = Instant.now();
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
    tracer.export(new SpanContext(kind, Collections.unmodifiableMap(labels), opened, Instant.now(), failed));
  }

}
