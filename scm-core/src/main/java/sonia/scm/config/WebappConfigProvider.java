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

import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.Optional;

import static java.util.Optional.empty;

@Slf4j
public final class WebappConfigProvider {

  private static WebappConfigProvider instance;

  private final Map<String, String> configBindings;
  private final Map<String, String> environment;

  private WebappConfigProvider(Map<String, String> configBindings, Map<String, String> environment) {
    this.configBindings = configBindings;
    this.environment = environment;
  }

  public static void setConfigBindings(Map<String, String> newBindings) {
    WebappConfigProvider.setConfigBindings(newBindings, System.getenv());
  }

  static void setConfigBindings(Map<String, String> newBindings, Map<String, String> environment) {
    instance = new WebappConfigProvider(newBindings, environment);
  }

  public static Optional<String> resolveAsString(String key) {
    return resolveConfig(key);
  }

  public static Optional<Boolean> resolveAsBoolean(String key) {
    return resolveConfig(key).map(Boolean::parseBoolean);
  }

  public static Optional<Integer> resolveAsInteger(String key) {
    return resolveConfig(key).map(Integer::parseInt);
  }

  public static Optional<Long> resolveAsLong(String key) {
    return resolveConfig(key).map(Long::parseLong);
  }

  private static Optional<String> resolveConfig(String key) {
    if (instance == null) {
      return empty();
    }
    return instance.resolveConfigInternal(key);
  }

  private Optional<String> resolveConfigInternal(String key) {
    String envValue = environment.get("SCM_WEBAPP_" + key.replace('.', '_').toUpperCase());
    if (envValue != null) {
      log.debug("resolve config for key '{}' to value '{}' from environment", key, envValue);
      return Optional.of(envValue);
    }
    String value = instance.configBindings.get(key);
    if (value == null) {
      log.debug("could not resolve config for key '{}'", key);
      return empty();
    } else {
      log.debug("resolve config for key '{}' to value '{}'", key, value);
      return Optional.of(value);
    }
  }
}
