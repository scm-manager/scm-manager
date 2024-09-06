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
