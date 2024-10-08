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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.SCMContextProvider;
import sonia.scm.Stage;
import sonia.scm.cache.Cache;
import sonia.scm.cache.CacheManager;
import sonia.scm.plugin.PluginLoader;
import sonia.scm.util.JsonMerger;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class I18nCollectorTest {

  private static final String GIT_PLUGIN_JSON = json(
    "{",
    "'scm-git-plugin': {",
    "'information': {",
    "'clone' : 'Clone',",
    "'create' : 'Create',",
    "'replace' : 'Push'",
    "}",
    "}",
    "}"
  );

  private static final String HG_PLUGIN_JSON = json(
    "{",
    "'scm-hg-plugin': {",
    "'information': {",
    "'clone' : 'Clone',",
    "'create' : 'Create',",
    "'replace' : 'Push'",
    "}",
    "}",
    "}"
  );

  private static final String SVN_PLUGIN_JSON = json(
    "{",
    "'scm-svn-plugin': {",
    "'information': {",
    "'checkout' : 'Checkout'",
    "}",
    "}",
    "}"
  );

  private static final String[] ALL_PLUGIN_JSON = new String[]{
    GIT_PLUGIN_JSON, HG_PLUGIN_JSON, SVN_PLUGIN_JSON
  };

  private static String json(String... parts) {
    return String.join("\n", parts).replaceAll("'", "\"");
  }

  @Mock
  private SCMContextProvider context;

  @Mock
  private PluginLoader pluginLoader;

  @Mock
  private CacheManager cacheManager;

  @Mock
  private Cache<String, JsonNode> cache;

  private I18nCollector collector;

  @BeforeEach
  void mockCache() {
    when(cacheManager.<String, JsonNode>getCache(I18nCollector.CACHE_NAME)).thenReturn(cache);
  }

  @Nested
  class WithoutResourcesTest {

    @BeforeEach
    void mockClassLoader(@TempDir Path directory) throws IOException {
      mockUberClassLoader(directory);
      createCollector();
    }

    @Test
    void shouldReturnEmptyForMissingLanguage() throws IOException {
      Optional<JsonNode> json = collector.findJson("de");
      assertThat(json).isEmpty();
    }
  }

  @Nested
  class WithResourcesTest {

    @BeforeEach
    void mockClassLoader(@TempDir Path directory) throws IOException {
      mockResources(directory);
      createCollector();
    }

    @Test
    void inDevelopmentStageShouldNotUseCache() throws IOException {
      stage(Stage.DEVELOPMENT);

      Optional<JsonNode> json = collector.findJson("de");

      verifyJson(json);
      verify(cache, never()).get(any());
    }

    @Test
    void shouldGetFromCacheInProductionStage() throws IOException {
      stage(Stage.PRODUCTION);

      ObjectMapper mapper = new ObjectMapper();
      JsonNode jsonNode = mapper.readTree(GIT_PLUGIN_JSON);
      when(cache.get("de")).thenReturn(jsonNode);

      Optional<JsonNode> json = collector.findJson("de");
      assertThat(json).get().isSameAs(jsonNode);
    }

    @Test
    void shouldStoreToCacheInProductionStage() throws IOException {
      stage(Stage.PRODUCTION);

      Optional<JsonNode> json = collector.findJson("de");

      verifyJson(json);
      verify(cache).put(eq("de"), any());
    }
  }

  private void verifyJson(Optional<JsonNode> json) {
    assertThat(json).isNotEmpty();
    assertThat(json.get().get("scm-svn-plugin")).isNotNull();
    assertThat(json.get().get("scm-git-plugin")).isNotNull();
    assertThat(json.get().get("scm-hg-plugin")).isNotNull();
  }

  private void createCollector() {
    collector = new I18nCollector(context, pluginLoader, new JsonMerger(new ObjectMapper()), cacheManager);
  }

  private void mockUberClassLoader(Path... directories) throws MalformedURLException {
    mockUberClassLoader(Arrays.asList(directories));
  }

  private void mockUberClassLoader(Collection<Path> directories) throws MalformedURLException {
    List<URL> urls = new ArrayList<>();
    for (Path directory : directories) {
      urls.add(directory.toUri().toURL());
    }
    ClassLoader bootstrapLoader = ClassLoader.getSystemClassLoader().getParent();
    URLClassLoader classLoader = new URLClassLoader(urls.toArray(new URL[0]), bootstrapLoader);
    when(pluginLoader.getUberClassLoader()).thenReturn(classLoader);
  }

  private void stage(Stage stage) {
    when(context.getStage()).thenReturn(stage);
  }

  private void mockResources(Path directory) throws IOException {
    List<Path> directories = new ArrayList<>();
    for (int i = 0; i < ALL_PLUGIN_JSON.length; i++) {
      Path pluginDirectory = directory.resolve("plugin-" + i);
      Path file = pluginDirectory.resolve("locales/de/plugins.json");
      Files.createDirectories(file.getParent());
      Files.write(file, ALL_PLUGIN_JSON[i].getBytes(StandardCharsets.UTF_8));
      directories.add(pluginDirectory);
    }
    mockUberClassLoader(directories);
  }
}
