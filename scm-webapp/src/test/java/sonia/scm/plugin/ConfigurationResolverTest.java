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

package sonia.scm.plugin;

import org.junit.jupiter.api.Test;
import sonia.scm.config.WebappConfigProvider;

import java.util.Collections;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ConfigurationResolverTest {

  @Test
  void shouldResolveConfiguration_WithIntValue() {
    ConfigurationResolver configurationResolver = new ConfigurationResolver(Collections.emptyMap(), "sonia/scm/plugin/config.yml");

    String port = configurationResolver.resolve("https.port", "21");

    assertThat(port).isEqualTo("42");
  }

  @Test
  void shouldResolveConfiguration_WithStringValue() {
    ConfigurationResolver configurationResolver = new ConfigurationResolver(Collections.emptyMap(), "sonia/scm/plugin/config.yml");

    String port = configurationResolver.resolve("https.keyType", "jks");

    assertThat(port).isEqualTo("PKCS12");
  }

  @Test
  void shouldResolveConfiguration_WithEnv() {
    ConfigurationResolver configurationResolver = new ConfigurationResolver(Map.of("SCM_WEBAPP_CONTEXTPATH", "/scm", "SCM_WEBAPP_HTTPS_SSL", "true"), "sonia/scm/plugin/config.yml");

    String port = configurationResolver.resolve("contextPath", "/");
    String ssl = configurationResolver.resolve("https.ssl", "false");

    assertThat(port).isEqualTo("/scm");
    assertThat(ssl).isEqualTo("true");
  }

  @Test
  void shouldResolveConfiguration_WithDefaultValue() {
    ConfigurationResolver configurationResolver = new ConfigurationResolver(Collections.emptyMap(), "sonia/scm/plugin/config.yml");

    String port = configurationResolver.resolve("contextPath", "/");

    assertThat(port).isEqualTo("/");
  }

  @Test
  void shouldSetWebappConfigProvider() {
    new ConfigurationResolver(Collections.emptyMap(), "sonia/scm/plugin/config.yml");

    assertThat(WebappConfigProvider.resolveAsBoolean("redirect")).contains(true);
    assertThat(WebappConfigProvider.resolveAsInteger("https.port")).contains(42);
  }

  @Test
  void shouldReadNullValuesFromConfigYaml() {
    ConfigurationResolver configurationResolver = new ConfigurationResolver(Collections.emptyMap(), "sonia/scm/plugin/configWithNull.yml");

    assertThat(WebappConfigProvider.resolveAsInteger("https.keyType")).isEmpty();
    assertThat(WebappConfigProvider.resolveAsString("https.keyType")).isEmpty();
    assertThat(WebappConfigProvider.resolveAsBoolean("https.keyType")).isEmpty();

    assertThat(configurationResolver.resolve("https.keyType", "other")).isEqualTo("other");
  }
}
