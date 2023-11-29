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
