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

import com.google.common.collect.ImmutableSet;
import com.google.inject.util.Providers;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import jakarta.inject.Provider;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.ThreadContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.metrics.MonitoringSystem;
import sonia.scm.metrics.ScrapeTarget;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MetricsIndexEnricherTest {

  @Mock
  private HalEnricherContext context;

  @Mock
  private HalAppender appender;

  @Mock
  private Subject subject;

  private Provider<ResourceLinks> resourceLinks;

  @BeforeEach
  void setUpResourceLinks() {
    ScmPathInfoStore scmPathInfoStore = new ScmPathInfoStore();
    scmPathInfoStore.set(() -> URI.create("/"));
    resourceLinks = () -> new ResourceLinks(Providers.of(scmPathInfoStore));
  }

  @BeforeEach
  void setUpSubject() {
    ThreadContext.bind(subject);
  }

  @AfterEach
  void tearDownSubject() {
    ThreadContext.unbindSubject();
  }

  @Nested
  class WithPermission {

    @BeforeEach
    void prepareSubject() {
      when(subject.isPermitted("metrics:read")).thenReturn(true);
    }

    @Test
    void shouldNotEnrichWithoutMonitoringSystems() {
      MetricsIndexEnricher enricher = new MetricsIndexEnricher(
        resourceLinks,
        Collections.emptySet()
      );
      enricher.enrich(context, appender);

      verify(appender, never()).linkArrayBuilder("metrics");
    }

    @Test
    void shouldNotEnrichWithMonitoringSystemsWithScrapeTarget() {
      MetricsIndexEnricher enricher = new MetricsIndexEnricher(
        resourceLinks,
        Collections.singleton(new NoScrapeMonitoringSystem())
      );
      enricher.enrich(context, appender);

      verify(appender, never()).linkArrayBuilder("metrics");
    }

    @Test
    void shouldEnrichIndex() {
      CapturingLinkArrayBuilder linkBuilder = new CapturingLinkArrayBuilder();
      when(appender.linkArrayBuilder("metrics")).thenReturn(linkBuilder);

      MetricsIndexEnricher enricher = new MetricsIndexEnricher(
        resourceLinks,
        ImmutableSet.of(
          new NoScrapeMonitoringSystem(),
          new ScrapeMonitoringSystem("one"),
          new ScrapeMonitoringSystem("two"))
      );
      enricher.enrich(context, appender);

      assertThat(linkBuilder.buildWasCalled).isTrue();
      assertThat(linkBuilder.links)
        .containsEntry("one", "/v2/metrics/one")
        .containsEntry("two", "/v2/metrics/two")
        .hasSize(2);
    }
  }

  @Nested
  class WithoutPermission {

    @BeforeEach
    void prepareSubject() {
      when(subject.isPermitted("metrics:read")).thenReturn(false);
    }

    @Test
    void shouldNotEnrichWithoutPermission() {
      MetricsIndexEnricher enricher = new MetricsIndexEnricher(
        resourceLinks,
        ImmutableSet.of(
          new ScrapeMonitoringSystem("one")
        )
      );
      enricher.enrich(context, appender);

      verify(appender, never()).linkArrayBuilder("metrics");
    }

  }

  private static class NoScrapeMonitoringSystem implements MonitoringSystem {

    @Override
    public String getName() {
      return "noscrap";
    }

    @Override
    public MeterRegistry getRegistry() {
      return new SimpleMeterRegistry();
    }
  }

  private static class ScrapeMonitoringSystem implements MonitoringSystem {

    private final String type;

    private ScrapeMonitoringSystem(String type) {
      this.type = type;
    }

    @Override
    public String getName() {
      return type;
    }

    @Override
    public MeterRegistry getRegistry() {
      return new SimpleMeterRegistry();
    }

    @Override
    public Optional<ScrapeTarget> getScrapeTarget() {
      return Optional.of(new NoopScrapeTarget());
    }
  }

  private static class NoopScrapeTarget implements ScrapeTarget {

    @Override
    public String getContentType() {
      return null;
    }

    @Override
    public void write(OutputStream outputStream) throws IOException {

    }
  }

  private static class CapturingLinkArrayBuilder implements HalAppender.LinkArrayBuilder {

    private final Map<String, String> links = new HashMap<>();
    private boolean buildWasCalled = false;

    @Override
    public HalAppender.LinkArrayBuilder append(String name, String href) {
      links.put(name, href);
      return this;
    }

    @Override
    public void build() {
      buildWasCalled = true;
    }
  }
}
