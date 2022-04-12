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

package sonia.scm.i18n;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.legman.Subscribe;
import com.google.common.annotations.VisibleForTesting;
import lombok.extern.slf4j.Slf4j;
import sonia.scm.SCMContextProvider;
import sonia.scm.Stage;
import sonia.scm.cache.Cache;
import sonia.scm.cache.CacheManager;
import sonia.scm.lifecycle.RestartEvent;
import sonia.scm.plugin.PluginLoader;
import sonia.scm.util.JsonMerger;

import javax.inject.Inject;
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
