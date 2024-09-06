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

import jakarta.inject.Inject;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.plugin.Extension;

import java.util.Set;

/**
 * Registers every {@link HalEnricher} which is annotated with an {@link Enrich} annotation.
 */
@Extension
public class LinkEnricherAutoRegistration implements ServletContextListener {

  private static final Logger LOG = LoggerFactory.getLogger(LinkEnricherAutoRegistration.class);

  private final HalEnricherRegistry registry;
  private final Set<HalEnricher> enrichers;

  @Inject
  public LinkEnricherAutoRegistration(HalEnricherRegistry registry, Set<HalEnricher> enrichers) {
    this.registry = registry;
    this.enrichers = enrichers;
  }

  @Override
  public void contextInitialized(ServletContextEvent sce) {
    for (HalEnricher enricher : enrichers) {
      Enrich annotation = enricher.getClass().getAnnotation(Enrich.class);
      if (annotation != null) {
        registry.register(annotation.value(), enricher);
      } else {
        LOG.warn("found HalEnricher extension {} without Enrich annotation", enricher.getClass());
      }
    }
  }

  @Override
  public void contextDestroyed(ServletContextEvent sce) {
    // nothing todo
  }
}
