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
   */
  public Duration duration() {
    return Duration.between(opened, closed);
  }
}
