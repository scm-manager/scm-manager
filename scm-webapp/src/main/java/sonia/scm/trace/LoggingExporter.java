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
