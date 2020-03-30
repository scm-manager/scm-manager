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
