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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import io.github.mweirauch.micrometer.jvm.extras.ProcessMemoryMetrics;
import io.github.mweirauch.micrometer.jvm.extras.ProcessThreadMetrics;
import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.jvm.ClassLoaderMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmGcMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmMemoryMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmThreadMetrics;
import io.micrometer.core.instrument.binder.logging.LogbackMetrics;
import io.micrometer.core.instrument.binder.system.FileDescriptorMetrics;
import io.micrometer.core.instrument.binder.system.ProcessorMetrics;
import io.micrometer.core.instrument.binder.system.UptimeMetrics;
import io.micrometer.core.instrument.composite.CompositeMeterRegistry;
import jakarta.inject.Inject;
import jakarta.inject.Provider;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Set;

@Singleton
public class MeterRegistryProvider implements Provider<MeterRegistry> {

  private static final CompositeMeterRegistry staticRegistry = new CompositeMeterRegistry();

  public static MeterRegistry getStaticRegistry() {
    return staticRegistry;
  }

  private static final Logger LOG = LoggerFactory.getLogger(MeterRegistryProvider.class);

  private final Set<MonitoringSystem> providerSet;

  @Inject
  public MeterRegistryProvider(Set<MonitoringSystem> providerSet) {
    this.providerSet = providerSet;
    resetStaticRegistry();
  }

  private void resetStaticRegistry() {
    Set<MeterRegistry> registries = ImmutableSet.copyOf(staticRegistry.getRegistries());
    for (MeterRegistry registry : registries) {
      staticRegistry.remove(registry);
    }

    List<Meter> meters = ImmutableList.copyOf(staticRegistry.getMeters());
    for (Meter meter : meters) {
      staticRegistry.remove(meter);
    }
  }

  @Override
  public MeterRegistry get() {
    MeterRegistry registry = createRegistry();
    if (!providerSet.isEmpty()) {
      bindCommonMetrics(registry);
    }
    return registry;
  }

  private MeterRegistry createRegistry() {
    if (providerSet.size() == 1) {
      MeterRegistry registry = providerSet.iterator().next().getRegistry();
      LOG.debug("create meter registry from single registration: {}", registry.getClass());
      staticRegistry.add(registry);
      return registry;
    }
    return createCompositeRegistry();
  }

  private CompositeMeterRegistry createCompositeRegistry() {
    LOG.debug("create composite meter registry");
    for (MonitoringSystem provider : providerSet) {
      MeterRegistry subRegistry = provider.getRegistry();
      LOG.debug("register {} as part of composite meter registry", subRegistry.getClass());
      staticRegistry.add(subRegistry);
    }
    return staticRegistry;
  }

  @SuppressWarnings("java:S2095") // we can't close, but it should be ok
  private void bindCommonMetrics(MeterRegistry registry) {
    // bind all metrics for https://grafana.com/grafana/dashboards/4701
    // expect those for tomcat
    new ClassLoaderMetrics().bindTo(registry);
    new JvmMemoryMetrics().bindTo(registry);
    new JvmGcMetrics().bindTo(registry);
    new ProcessorMetrics().bindTo(registry);
    new JvmThreadMetrics().bindTo(registry);
    new UptimeMetrics().bindTo(registry);
    new FileDescriptorMetrics().bindTo(registry);
    new ProcessMemoryMetrics().bindTo(registry);
    new ProcessThreadMetrics().bindTo(registry);
    new LogbackMetrics().bindTo(registry);
  }
}
