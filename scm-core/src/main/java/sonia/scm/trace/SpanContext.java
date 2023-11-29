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

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import sonia.scm.xml.XmlInstantAdapter;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;

/**
 * The {@link SpanContext} represents a finished span which could be processed by an {@link Exporter}.
 *
 * @since 2.9.0
 */
@Getter
@XmlRootElement
@EqualsAndHashCode
@AllArgsConstructor
@XmlAccessorType(XmlAccessType.FIELD)
@NoArgsConstructor(access = AccessLevel.PACKAGE)
public class SpanContext {

  private String kind;
  private Map<String, String> labels;
  @XmlJavaTypeAdapter(XmlInstantAdapter.class)
  private Instant opened;
  @XmlJavaTypeAdapter(XmlInstantAdapter.class)
  private Instant closed;
  private boolean failed;

  /**
   * Returns the label with the given key or {@code null}.
   * @param key key of label
   * @return label or {@code null}
   */
  public String label(String key) {
    return labels.get(key);
  }

  /**
   * Calculates the duration of the span.
   *
   * @return duration of the span
   */
  public Duration duration() {
    return Duration.between(opened, closed);
  }
}
