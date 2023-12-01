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

package sonia.scm.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static java.util.Collections.emptyMap;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class WebappConfigProviderTest {

  @Test
  void shouldHandleEmptyConfig() {
    WebappConfigProvider.setConfigBindings(emptyMap());

    assertThat(WebappConfigProvider.resolveAsString("key")).isEmpty();
  }

  @Test
  void shouldResolveStringValueFromConfig() {
    WebappConfigProvider.setConfigBindings(Map.of("key", "value"));

    assertThat(WebappConfigProvider.resolveAsString("key")).contains("value");
  }

  @Test
  void shouldResolveNumberValueFromConfig() {
    WebappConfigProvider.setConfigBindings(Map.of("key", "42"));

    assertThat(WebappConfigProvider.resolveAsInteger("key")).contains(42);
  }

  @Test
  void shouldResolveBooleanValueFromConfig() {
    WebappConfigProvider.setConfigBindings(Map.of("key", "true"));

    assertThat(WebappConfigProvider.resolveAsBoolean("key")).contains(true);
  }

  @Test
  void shouldLetEnvironmentOverrideConfig() {
    WebappConfigProvider.setConfigBindings(Map.of("key", "value"), Map.of("SCM_WEBAPP_KEY", "env"));

    assertThat(WebappConfigProvider.resolveAsString("key")).contains("env");
  }

  @Test
  void shouldCreateCorrectKeyForEnvironment() {
    WebappConfigProvider.setConfigBindings(Map.of("some.more.complex.key", "value"), Map.of("SCM_WEBAPP_SOME_MORE_COMPLEX_KEY", "env"));

    assertThat(WebappConfigProvider.resolveAsString("some.more.complex.key")).contains("env");
  }
}
