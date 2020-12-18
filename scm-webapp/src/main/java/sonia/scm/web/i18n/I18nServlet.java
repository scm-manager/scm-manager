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

package sonia.scm.web.i18n;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.legman.Subscribe;
import com.google.common.annotations.VisibleForTesting;
import com.google.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.SCMContextProvider;
import sonia.scm.Stage;
import sonia.scm.cache.Cache;
import sonia.scm.cache.CacheManager;
import sonia.scm.filter.WebElement;
import sonia.scm.lifecycle.RestartEvent;
import sonia.scm.plugin.PluginLoader;

import javax.inject.Inject;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Optional;


/**
 * Collect the plugin translations.
 */
@Singleton
@WebElement(value = I18nServlet.PATTERN, regex = true)
public class I18nServlet extends HttpServlet {

  private static final Logger LOG = LoggerFactory.getLogger(I18nServlet.class);

  public static final String PLUGINS_JSON = "plugins.json";
  public static final String PATTERN = "/locales/[a-z\\-A-Z]*/" + PLUGINS_JSON;
  public static final String CACHE_NAME = "sonia.cache.plugins.translations";

  private final SCMContextProvider context;
  private final ClassLoader classLoader;
  private final Cache<String, JsonNode> cache;
  private final ObjectMapper objectMapper = new ObjectMapper();


  @Inject
  public I18nServlet(SCMContextProvider context, PluginLoader pluginLoader, CacheManager cacheManager) {
    this.context = context;
    this.classLoader = pluginLoader.getUberClassLoader();
    this.cache = cacheManager.getCache(CACHE_NAME);
  }

  @Subscribe(async = false)
  public void handleRestartEvent(RestartEvent event) {
    LOG.debug("Clear cache on restart event with reason {}", event.getReason());
    cache.clear();
  }

  @VisibleForTesting
  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response) {
    String path = request.getServletPath();
    try {
      Optional<JsonNode> json = findJson(path);
      if (json.isPresent()) {
        write(response, json.get());
      } else {
        LOG.debug("could not find translation at {}", path);
        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
      }
    } catch (IOException ex) {
      LOG.error("Error on getting the translation of the plugins", ex);
      response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }
  }

  private void write(HttpServletResponse response, JsonNode jsonNode) throws IOException {
    response.setCharacterEncoding("UTF-8");
    response.setContentType("application/json");
    response.setHeader("Cache-Control", "no-cache");

    try (PrintWriter writer = response.getWriter()) {
      objectMapper.writeValue(writer, jsonNode);
    }
  }

  public Optional<JsonNode> findJson(String path) throws IOException {
    if (isProductionStage()) {
      return findJsonCached(path);
    }
    return collectJsonFile(path);
  }

  private Optional<JsonNode> findJsonCached(String path) throws IOException {
    JsonNode jsonNode = cache.get(path);
    if (jsonNode != null) {
      LOG.debug("return json node from cache for path {}", path);
      return Optional.of(jsonNode);
    }

    LOG.debug("collect json for path {}", path);
    Optional<JsonNode> collected = collectJsonFile(path);
    collected.ifPresent(node -> cache.put(path, node));
    return collected;
  }

  @VisibleForTesting
  protected boolean isProductionStage() {
    return context.getStage() == Stage.PRODUCTION;
  }

  private Optional<JsonNode> collectJsonFile(String path) throws IOException {
    LOG.debug("Collect plugin translations from path {} for every plugin", path);
    JsonNode mergedJsonNode = null;
    Enumeration<URL> resources = classLoader.getResources(path.replaceFirst("/", ""));
    while (resources.hasMoreElements()) {
      URL url = resources.nextElement();
      JsonNode jsonNode = objectMapper.readTree(url);
      if (mergedJsonNode != null) {
        merge(mergedJsonNode, jsonNode);
      } else {
        mergedJsonNode = jsonNode;
      }
    }

    return Optional.ofNullable(mergedJsonNode);
  }

  private JsonNode merge(JsonNode mainNode, JsonNode updateNode) {
    Iterator<String> fieldNames = updateNode.fieldNames();
    while (fieldNames.hasNext()) {
      String fieldName = fieldNames.next();
      JsonNode jsonNode = mainNode.get(fieldName);
      if (jsonNode != null) {
        mergeNode(updateNode, fieldName, jsonNode);
      } else {
        mergeField(mainNode, updateNode, fieldName);
      }
    }
    return mainNode;
  }

  private void mergeField(JsonNode mainNode, JsonNode updateNode, String fieldName) {
    if (mainNode instanceof ObjectNode) {
      JsonNode value = updateNode.get(fieldName);
      if (value.isNull()) {
        return;
      }
      if (value.isIntegralNumber() && value.toString().equals("0")) {
        return;
      }
      if (value.isFloatingPointNumber() && value.toString().equals("0.0")) {
        return;
      }
      ((ObjectNode) mainNode).set(fieldName, value);
    }
  }

  private void mergeNode(JsonNode updateNode, String fieldName, JsonNode jsonNode) {
    if (jsonNode.isObject()) {
      merge(jsonNode, updateNode.get(fieldName));
    } else if (jsonNode.isArray()) {
      for (int i = 0; i < jsonNode.size(); i++) {
        merge(jsonNode.get(i), updateNode.get(fieldName).get(i));
      }
    }
  }

}
