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

package sonia.scm.cache;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.config.WebappConfigProvider;

import java.io.StringReader;

import static java.util.Map.of;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class GuavaNamedCacheConfigurationTest {

  public static final String CACHE_CONFIGURATION =
    "<caches>" +
      "  <cache" +
      "    name=\"sonia.cache.externalGroups\"" +
      "    maximumSize=\"1000\"" +
      "  />" +
      "</caches>";

  @Test
  void shouldTakeValueFromConfigurationFile() throws JAXBException {
    WebappConfigProvider.setConfigBindings(of());
    GuavaCacheManagerConfiguration configuration = readConfiguration();

    assertThat(configuration.getCaches().get(0).getName()).isEqualTo("sonia.cache.externalGroups");
    assertThat(configuration.getCaches().get(0).getMaximumSize()).isEqualTo(1000);
  }

  @Nested
  class WithConfig {

    @AfterEach
    void tearDown() {
      WebappConfigProvider.setConfigBindings(of());
    }

    @Test
    void shouldTakeValueFromConfigIfPresent() throws JAXBException {
      WebappConfigProvider.setConfigBindings(of("scm.cache.externalGroups.maximumSize", "42"));
      GuavaCacheManagerConfiguration configuration = readConfiguration();

      assertThat(configuration.getCaches().get(0).getMaximumSize()).isEqualTo(42);
    }
  }

  private static GuavaCacheManagerConfiguration readConfiguration() throws JAXBException {
    JAXBContext context = JAXBContext.newInstance(GuavaCacheManagerConfiguration.class);
    return (GuavaCacheManagerConfiguration) context
      .createUnmarshaller()
      .unmarshal(new StringReader(CACHE_CONFIGURATION));
  }
}
