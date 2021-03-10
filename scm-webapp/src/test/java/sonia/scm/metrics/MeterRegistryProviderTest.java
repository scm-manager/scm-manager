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

package sonia.scm.metrics;

import io.micrometer.core.instrument.MeterRegistry;
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
