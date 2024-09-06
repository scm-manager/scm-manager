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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.io.Resources;
import sonia.scm.config.WebappConfigProvider;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

public class ConfigurationResolver {

  private static final String PREFIX = "SCM_WEBAPP_";

  private final JsonNode rootNode;

  private final Map<String, String> environment;

  public ConfigurationResolver() {
    this(System.getenv(), "config.yml");
  }

  @VisibleForTesting
  ConfigurationResolver(Map<String, String> environment, String configPath) {
    this.environment = environment;
    URL resource = Resources.getResource(configPath);
    try {
      rootNode = new ObjectMapper(new YAMLFactory()).readTree(resource).get("webapp");
      WebappConfigProvider.setConfigBindings(readConfigurationFile(rootNode));
    } catch (IOException e) {
      throw new IllegalArgumentException("failed to read configuration from file " + configPath, e);
    }
  }

  private Map<String, String> readConfigurationFile(JsonNode rootNode) {
    return readConfigurationFile(rootNode, "");
  }

  private Map<String, String> readConfigurationFile(JsonNode rootNode, String prefix) {
    Map<String, String> configurationFile = new HashMap<>();
    rootNode.fields().forEachRemaining(entry -> {
      if (entry.getValue().isValueNode()) {
        if (entry.getValue().isNull()) {
          configurationFile.put(prefix + entry.getKey(), null);
        } else {
          configurationFile.put(prefix + entry.getKey(), entry.getValue().asText());
        }
      } else {
        configurationFile.putAll(readConfigurationFile(entry.getValue(), prefix + entry.getKey() + "."));
      }
    });
    return configurationFile;
  }

  public String resolve(String key, String defaultValue) {
    Optional<String> value = resolveFromEnv(key);
    if (value.isEmpty()) {
      value = resolveFromFile(key);
    }
    return value.orElse(defaultValue);
  }

  private Optional<String> resolveFromFile(String key) {
    String[] keyFragments = key.split("\\.");
    JsonNode node = rootNode;
    for (String fragment : keyFragments) {
      if (node.has(fragment)) {
        node = node.get(fragment);
      } else {
        return Optional.empty();
      }
    }
    if (node.isNull()) {
      return Optional.empty();
    }
    return Optional.of(node.asText());
  }

  private Optional<String> resolveFromEnv(String key) {
    String envKey = createEnvKey(key);
    String envVariable = environment.get(envKey);

    return Optional.ofNullable(envVariable);
  }

  private String createEnvKey(String key) {
    return PREFIX + key.toUpperCase(Locale.ENGLISH).replaceAll("[.\\-/]", "_");
  }
}
