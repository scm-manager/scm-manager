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
