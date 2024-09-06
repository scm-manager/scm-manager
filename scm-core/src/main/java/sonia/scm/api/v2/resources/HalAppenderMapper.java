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
