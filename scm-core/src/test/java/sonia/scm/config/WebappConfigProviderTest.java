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
