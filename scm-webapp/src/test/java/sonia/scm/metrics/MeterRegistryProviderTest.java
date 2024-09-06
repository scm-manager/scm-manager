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

package sonia.scm.metrics;

import com.google.common.collect.ImmutableSet;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.composite.CompositeMeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;


class MeterRegistryProviderTest {

  @Test
  void shouldReturnNoopRegistry() {
    MeterRegistryProvider provider = new MeterRegistryProvider(Collections.emptySet());
    MeterRegistry registry = provider.get();
    registry.counter("sample").increment();
    assertThat(registry.get("sample").counter().count()).isEqualTo(0.0);
  }

  @Test
  void shouldNotRegisterCommonMetricsForDefaultRegistry() {
    MeterRegistryProvider provider = new MeterRegistryProvider(Collections.emptySet());
    assertThat(provider.get().getMeters()).withFailMessage("no common metrics should be registered").isEmpty();
  }

  @Test
  void shouldStoreMetrics() {
    MeterRegistryProvider provider = new MeterRegistryProvider(Collections.singleton(new SimpleMonitoringSystem()));
    MeterRegistry registry = provider.get();
    registry.counter("sample").increment();
    assertThat(registry.get("sample").counter().count()).isEqualTo(1.0);
  }

  @Test
  void shouldRegisterCommonMetrics() {
    MeterRegistryProvider provider = new MeterRegistryProvider(Collections.singleton(new SimpleMonitoringSystem()));
    MeterRegistry registry = provider.get();
    assertThat(registry.getMeters()).isNotEmpty();
  }

  @Test
  void shouldCreateCompositeMeterRegistry() {
    MeterRegistryProvider provider = new MeterRegistryProvider(
      ImmutableSet.of(new SimpleMonitoringSystem(), new SimpleMonitoringSystem())
    );
    assertThat(provider.get()).isInstanceOf(CompositeMeterRegistry.class);
  }

  @Test
  void shouldReturnSingleRegistryUnwrapped() {
    MeterRegistryProvider provider = new MeterRegistryProvider(Collections.singleton(new SimpleMonitoringSystem()));
    assertThat(provider.get()).isInstanceOf(SimpleMeterRegistry.class);
  }

  private static class SimpleMonitoringSystem implements MonitoringSystem {

    @Override
    public String getName() {
      return "simple";
    }

    @Override
    public MeterRegistry getRegistry() {
      return new SimpleMeterRegistry();
    }
  }

}
