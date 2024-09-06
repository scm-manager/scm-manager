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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.plugin.Extension;

import java.util.Map;
import java.util.function.Consumer;

/**
 * An {@link Exporter} which logs every collected span.
 *
 * @since 2.9.0
 */
@Extension
public final class LoggingExporter implements Exporter {

  private static final Logger LOG = LoggerFactory.getLogger(LoggingExporter.class);

  private final Consumer<String> logger;

  @Inject
  LoggingExporter() {
    this(LOG::debug);
  }

  LoggingExporter(Consumer<String> logger) {
    this.logger = logger;
  }

  @Override
  public void export(SpanContext span) {
    logger.accept(format(span));
  }

  private String format(SpanContext span) {
    StringBuilder message = new StringBuilder("received ");
    if (span.isFailed()) {
      message.append("failed ");
    }
    message.append(span.getKind()).append(" span, which took ");
    message.append(span.duration().toMillis()).append("ms");
    Map<String, String> labels = span.getLabels();
    if (!labels.isEmpty()) {
      message.append(":");
      for (Map.Entry<String, String> e : labels.entrySet()) {
        message.append("\n - ").append(e.getKey()).append(": ").append(e.getValue());
      }
    }
    return message.toString();
  }

}
