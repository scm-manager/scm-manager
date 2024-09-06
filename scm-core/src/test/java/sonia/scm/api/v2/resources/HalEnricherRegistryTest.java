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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class HalEnricherRegistryTest {

  private HalEnricherRegistry registry;

  @BeforeEach
  void setUpObjectUnderTest() {
    registry = new HalEnricherRegistry();
  }

  @Test
  void shouldRegisterTheEnricher() {
    SampleHalEnricher enricher = new SampleHalEnricher();
    registry.register(String.class, enricher);

    Iterable<HalEnricher> enrichers = registry.allByType(String.class);
    assertThat(enrichers).containsOnly(enricher);
  }

  @Test
  void shouldRegisterMultipleEnrichers() {
    SampleHalEnricher one = new SampleHalEnricher();
    registry.register(String.class, one);

    SampleHalEnricher two = new SampleHalEnricher();
    registry.register(String.class, two);

    Iterable<HalEnricher> enrichers = registry.allByType(String.class);
    assertThat(enrichers).containsOnly(one, two);
  }

  @Test
  void shouldRegisterEnrichersForDifferentTypes() {
    SampleHalEnricher one = new SampleHalEnricher();
    registry.register(String.class, one);

    SampleHalEnricher two = new SampleHalEnricher();
    registry.register(Integer.class, two);

    Iterable<HalEnricher> enrichers = registry.allByType(String.class);
    assertThat(enrichers).containsOnly(one);

    enrichers = registry.allByType(Integer.class);
    assertThat(enrichers).containsOnly(two);
  }

  private static class SampleHalEnricher implements HalEnricher {
    @Override
    public void enrich(HalEnricherContext context, HalAppender appender) {

    }
  }

}
