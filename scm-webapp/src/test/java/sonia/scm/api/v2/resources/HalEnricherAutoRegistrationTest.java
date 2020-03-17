/**
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

import com.google.common.collect.ImmutableSet;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Java6Assertions.assertThat;

class HalEnricherAutoRegistrationTest {

  @Test
  void shouldRegisterAllAvailableLinkEnrichers() {
    HalEnricher one = new One();
    HalEnricher two = new Two();
    HalEnricher three = new Three();
    HalEnricher four = new Four();
    Set<HalEnricher> enrichers = ImmutableSet.of(one, two, three, four);

    HalEnricherRegistry registry = new HalEnricherRegistry();

    LinkEnricherAutoRegistration autoRegistration = new LinkEnricherAutoRegistration(registry, enrichers);
    autoRegistration.contextInitialized(null);

    assertThat(registry.allByType(String.class)).containsOnly(one, two);
    assertThat(registry.allByType(Integer.class)).containsOnly(three);
  }

  @Enrich(String.class)
  public static class One implements HalEnricher {

    @Override
    public void enrich(HalEnricherContext context, HalAppender appender) {

    }
  }

  @Enrich(String.class)
  public static class Two implements HalEnricher {

    @Override
    public void enrich(HalEnricherContext context, HalAppender appender) {

    }
  }

  @Enrich(Integer.class)
  public static class Three implements HalEnricher {

    @Override
    public void enrich(HalEnricherContext context, HalAppender appender) {

    }
  }

  public static class Four implements HalEnricher {

    @Override
    public void enrich(HalEnricherContext context, HalAppender appender) {

    }
  }

}
