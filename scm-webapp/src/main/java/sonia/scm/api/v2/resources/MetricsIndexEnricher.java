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
