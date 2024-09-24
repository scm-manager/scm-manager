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

package sonia.scm.cache;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.cache.GuavaCacheMetrics;
import jakarta.inject.Inject;

import java.util.Collections;

public class GuavaCacheFactory {

  private final MeterRegistry meterRegistry;

  @Inject
  public GuavaCacheFactory(MeterRegistry meterRegistry) {
    this.meterRegistry = meterRegistry;
  }

  <K, V> GuavaCache<K, V> create(GuavaCacheConfiguration configuration, String name) {
    com.google.common.cache.Cache<K, V> cache = GuavaCaches.create(configuration, name);

    new GuavaCacheMetrics(cache, name, Collections.emptySet()).bindTo(meterRegistry);

    return new GuavaCache<>(cache, configuration.getCopyStrategy(), name);
  }
}
