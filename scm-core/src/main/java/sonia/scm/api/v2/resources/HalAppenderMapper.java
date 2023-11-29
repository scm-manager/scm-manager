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

package sonia.scm.api.v2.resources;

import com.google.common.annotations.VisibleForTesting;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class HalAppenderMapper {

  @Inject
  private HalEnricherRegistry registry;

  @VisibleForTesting
  void setRegistry(HalEnricherRegistry registry) {
    this.registry = registry;
  }

  protected void applyEnrichers(HalAppender appender, Object source, Object... contextEntries) {
    // null check is only their to not break existing tests
    if (registry != null) {

      Object[] ctx = new Object[contextEntries.length + 1];
      ctx[0] = source;
      for (int i = 0; i < contextEntries.length; i++) {
        ctx[i + 1] = contextEntries[i];
      }

      HalEnricherContext context = HalEnricherContext.of(ctx);
      applyEnrichers(context, appender, source.getClass());
    }
  }

  protected void applyEnrichers(HalEnricherContext context, HalAppender appender, Class<?> type) {
    Iterable<HalEnricher> enrichers = registry.allByType(type);
    for (HalEnricher enricher : enrichers) {
      try {
        enricher.enrich(context, appender);
      } catch (Exception e) {
        log.warn("failed to enrich repository; it might be, that the repository has been deleted in the meantime", e);
      }
    }
  }

}
