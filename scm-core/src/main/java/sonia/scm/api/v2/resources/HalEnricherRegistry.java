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

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import jakarta.inject.Singleton;
import sonia.scm.plugin.Extension;

/**
 * The {@link HalEnricherRegistry} is responsible for binding {@link HalEnricher} instances to their source types.
 *
 * @since 2.0.0
 */
@Extension
@Singleton
public final class HalEnricherRegistry {

  private final Multimap<Class, HalEnricher> enrichers = HashMultimap.create();

  /**
   * Registers a new {@link HalEnricher} for the given source type.
   *
   * @param sourceType type of json mapping source
   * @param enricher link enricher instance
   */
  public void register(Class sourceType, HalEnricher enricher) {
    enrichers.put(sourceType, enricher);
  }

  /**
   * Returns all registered {@link HalEnricher} for the given type.
   *
   * @param sourceType type of json mapping source
   */
  public Iterable<HalEnricher> allByType(Class sourceType) {
    return enrichers.get(sourceType);
  }
}
