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

import sonia.scm.plugin.ExtensionPoint;

/**
 * A {@link HalEnricher} can be used to append hal specific attributes, such as links, to the json response.
 * To register an enricher use the {@link Enrich} annotation or the {@link HalEnricherRegistry} which is available
 * via injection.
 *
 * <b>Warning:</b> enrichers are always registered as singletons.
 *
 * @since 2.0.0
 */
@ExtensionPoint
@FunctionalInterface
public interface HalEnricher {

  /**
   * Enriches the response with hal specific attributes.
   *
   * @param context contains the source for the json mapping and related objects
   * @param appender can be used to append links or embedded objects to the json response
   */
  void enrich(HalEnricherContext context, HalAppender appender);
}
