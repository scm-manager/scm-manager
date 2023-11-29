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
      throw new RuntimeException(e);
    }
  }

  private Map<String, String> readConfigurationFile(JsonNode rootNode) {
    return readConfigurationFile(rootNode, "");
  }

  private Map<String, String> readConfigurationFile(JsonNode rootNode, String prefix) {
    Map<String, String> configurationFile = new HashMap<>();
    rootNode.fields().forEachRemaining(entry -> {
      if (entry.getValue().isValueNode()) {
        configurationFile.put(prefix + entry.getKey(), entry.getValue().asText());
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

    return Optional.of(node.asText());
  }

  private Optional<String> resolveFromEnv(String key) {
    String envKey = createEnvKey(key);
    String envVariable = environment.get(envKey);

    return Optional.ofNullable(envVariable);
  }

  private String createEnvKey(String key) {
    return PREFIX + key.toUpperCase(Locale.ENGLISH).replaceAll("\\.-/", "_");
  }
}
