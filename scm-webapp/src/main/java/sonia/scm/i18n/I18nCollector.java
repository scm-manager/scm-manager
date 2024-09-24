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

package sonia.scm.i18n;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.annotations.VisibleForTesting;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import sonia.scm.SCMContextProvider;
import sonia.scm.Stage;
import sonia.scm.cache.Cache;
import sonia.scm.cache.CacheManager;
import sonia.scm.plugin.PluginLoader;
import sonia.scm.util.JsonMerger;

import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.Optional;

@Slf4j
public class I18nCollector {

  public static final String CACHE_NAME = "sonia.cache.plugins.translations";

  private final SCMContextProvider context;
  private final ClassLoader classLoader;
  private final JsonMerger jsonMerger;

  private final Cache<String, JsonNode> cache;
  private final ObjectMapper objectMapper = new ObjectMapper();

  @Inject
  public I18nCollector(SCMContextProvider context, PluginLoader pluginLoader, JsonMerger jsonMerger, CacheManager cacheManager) {
    this.cache = cacheManager.getCache(CACHE_NAME);
    this.context = context;
    this.classLoader = pluginLoader.getUberClassLoader();
    this.jsonMerger = jsonMerger;
  }

  public Optional<JsonNode> findJson(String languageCode) throws IOException {
    if (isProductionStage()) {
      return findJsonCached(languageCode);
    }
    return collectJsonFile(languageCode);
  }

  private Optional<JsonNode> findJsonCached(String languageCode) throws IOException {
    JsonNode jsonNode = cache.get(languageCode);
    if (jsonNode != null) {
      log.debug("return json node from cache for language {}", languageCode);
      return Optional.of(jsonNode);
    }

    log.debug("collect json for language {}", languageCode);
    Optional<JsonNode> collected = collectJsonFile(languageCode);
    collected.ifPresent(node -> cache.put(languageCode, node));
    return collected;
  }

  @VisibleForTesting
  protected boolean isProductionStage() {
    return context.getStage() == Stage.PRODUCTION;
  }

  private Optional<JsonNode> collectJsonFile(String languageCode) throws IOException {
    String path = String.format("locales/%s/plugins.json", languageCode);
    log.debug("Collect plugin translations from path {} for every plugin", path);
    JsonNode mergedJsonNode = null;
    Enumeration<URL> resources = classLoader.getResources(path);
    while (resources.hasMoreElements()) {
      URL url = resources.nextElement();
      JsonNode jsonNode = objectMapper.readTree(url);
      if (mergedJsonNode != null) {
        mergedJsonNode = jsonMerger.fromJson(mergedJsonNode).mergeWithJson(jsonNode).toJsonNode();
      } else {
        mergedJsonNode = jsonNode;
      }
    }

    return Optional.ofNullable(mergedJsonNode);
  }
}
