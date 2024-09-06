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
import jakarta.inject.Provider;
import org.apache.shiro.SecurityUtils;
import sonia.scm.metrics.MonitoringSystem;
import sonia.scm.plugin.Extension;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Extension
@Enrich(Index.class)
public class MetricsIndexEnricher implements HalEnricher {

  private final Provider<ResourceLinks> resourceLinks;
  private final List<String> scrapeTargets;

  @Inject
  public MetricsIndexEnricher(Provider<ResourceLinks> resourceLinks, Set<MonitoringSystem> monitoringSystems) {
    this.resourceLinks = resourceLinks;
    this.scrapeTargets = scrapeTargets(monitoringSystems);
  }

  private List<String> scrapeTargets(Set<MonitoringSystem> monitoringSystems) {
    return monitoringSystems.stream()
      .filter(sys -> sys.getScrapeTarget().isPresent())
      .map(MonitoringSystem::getName)
      .collect(Collectors.toList());
  }

  @Override
  public void enrich(HalEnricherContext context, HalAppender appender) {
    if (!isPermitted() || scrapeTargets.isEmpty()) {
      return;
    }
    HalAppender.LinkArrayBuilder links = appender.linkArrayBuilder("metrics");
    for (String type : scrapeTargets) {
      links.append(type, resourceLinks.get().metrics().forType(type));
    }
    links.build();
  }

  private boolean isPermitted() {
    return SecurityUtils.getSubject().isPermitted("metrics:read");
  }
}
