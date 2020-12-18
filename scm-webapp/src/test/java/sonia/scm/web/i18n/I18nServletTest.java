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
import com.github.legman.EventBus;
import org.apache.commons.lang3.StringUtils;
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
import sonia.scm.lifecycle.RestartEventFactory;
import sonia.scm.plugin.PluginLoader;

import javax.annotation.Nonnull;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class I18nServletTest {

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

  @Test
  void shouldNotHaveInvalidPluginsJsonFiles() throws IOException {
    String path = getClass().getClassLoader().getResource("locales/en/plugins.json").getPath();
    assertThat(path).isNotNull();

    Path filePath = Paths.get(path);
    Path translationRootPath = filePath.getParent().getParent();
    assertThat(translationRootPath).isDirectoryContaining("glob:**/en");

    Files
      .list(translationRootPath)
      .filter(Files::isDirectory)
      .map(localePath -> localePath.resolve("plugins.json"))
      .forEach(this::validatePluginsJson);
  }

  private void validatePluginsJson(Path path) {
    try {
      new ObjectMapper().readTree(path.toFile());
    } catch (IOException e) {
      fail("error while parsing translation file " + path, e);
    }
  }

  @Nested
  class WithCacheManager {

    @BeforeEach
    void init() {
      when(cacheManager.<String, JsonNode>getCache(I18nServlet.CACHE_NAME)).thenReturn(cache);
    }

    @Test
    void shouldFailWith404OnMissingResources(@TempDir Path directory) throws IOException {
      String path = "/locales/de/plugins.json";
      HttpServletRequest request = mock(HttpServletRequest.class);
      when(request.getServletPath()).thenReturn(path);
      HttpServletResponse response = mock(HttpServletResponse.class);

      mockUberClassLoader(directory);

      createServlet().doGet(request, response);
      verify(response).setStatus(404);
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

    @Test
    void shouldFailWith500OnIOException(@TempDir Path directory) throws IOException {
      stage(Stage.DEVELOPMENT);
      HttpServletRequest request = mock(HttpServletRequest.class);
      when(request.getServletPath()).thenReturn("/locales/de/plugins.json");
      HttpServletResponse response = mock(HttpServletResponse.class);

      mockResource(directory, "locales/de/plugins.json", "invalid json");

      createServlet().doGet(request, response);

      verify(response).setStatus(500);
    }

    private void mockResource(Path directory, String resourcePath, String content) throws IOException {
      Path file = directory.resolve(resourcePath);
      Files.createDirectories(file.getParent());
      Files.write(file, content.getBytes(StandardCharsets.UTF_8));

      mockUberClassLoader(directory);
    }

    private void stage(Stage stage) {
      when(context.getStage()).thenReturn(stage);
    }

    @Test
    void inDevelopmentStageShouldNotUseCache(@TempDir Path temp) throws IOException {
      stage(Stage.DEVELOPMENT);
      mockResources(temp, "locales/de/plugins.json");
      HttpServletRequest request = mock(HttpServletRequest.class);
      when(request.getServletPath()).thenReturn("/locales/de/plugins.json");
      HttpServletResponse response = mock(HttpServletResponse.class);

      I18nServlet servlet = createServlet();
      String json = doGetString(servlet, request, response);

      assertJson(json);
      verify(cache, never()).get(any());
    }

    @Nonnull
    private I18nServlet createServlet() {
      return new I18nServlet(context, pluginLoader, cacheManager);
    }

    private String doGetString(I18nServlet servlet, HttpServletRequest request, HttpServletResponse response) throws IOException {
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      PrintWriter writer = new PrintWriter(baos);
      when(response.getWriter()).thenReturn(writer);

      servlet.doGet(request, response);

      writer.flush();
      return baos.toString(StandardCharsets.UTF_8.name());
    }

    private void mockResources(Path directory, String resourcePath) throws IOException {
      List<Path> directories = new ArrayList<>();
      for (int i = 0; i < ALL_PLUGIN_JSON.length; i++) {
        Path pluginDirectory = directory.resolve("plugin-" + i);
        Path file = pluginDirectory.resolve(resourcePath);
        Files.createDirectories(file.getParent());
        Files.write(file, ALL_PLUGIN_JSON[i].getBytes(StandardCharsets.UTF_8));
        directories.add(pluginDirectory);
      }
      mockUberClassLoader(directories);
    }

    @Test
    void shouldGetFromCacheInProductionStage() throws IOException {
      String path = "/locales/de/plugins.json";
      stage(Stage.PRODUCTION);
      HttpServletRequest request = mock(HttpServletRequest.class);
      when(request.getServletPath()).thenReturn(path);
      HttpServletResponse response = mock(HttpServletResponse.class);

      ObjectMapper mapper = new ObjectMapper();
      JsonNode jsonNode = mapper.readTree(GIT_PLUGIN_JSON);
      when(cache.get(path)).thenReturn(jsonNode);

      I18nServlet servlet = createServlet();
      String json = doGetString(servlet, request, response);
      assertThat(json).contains("scm-git-plugin").doesNotContain("scm-hg-plugin");
      verifyHeaders(response);
    }

    @Test
    void shouldStoreToCacheInProductionStage(@TempDir Path temp) throws IOException {
      String path = "/locales/de/plugins.json";
      mockResources(temp, "locales/de/plugins.json");
      stage(Stage.PRODUCTION);
      HttpServletRequest request = mock(HttpServletRequest.class);
      when(request.getServletPath()).thenReturn(path);
      HttpServletResponse response = mock(HttpServletResponse.class);

      I18nServlet servlet = createServlet();
      String json = doGetString(servlet, request, response);

      verify(cache).put(any(String.class), any(JsonNode.class));

      verifyHeaders(response);
      assertJson(json);
    }


    @Nested
    class WithDefaultClassLoader {

      @BeforeEach
      void init() {
        when(pluginLoader.getUberClassLoader()).thenReturn(I18nServletTest.class.getClassLoader());
      }

      @Test
      void shouldCleanCacheOnRestartEvent() {
        I18nServlet servlet = createServlet();
        EventBus eventBus = new EventBus("forTestingOnly");
        eventBus.register(servlet);
        eventBus.post(RestartEventFactory.create(I18nServlet.class, "Restart to reload the plugin resources"));

        verify(cache).clear();
      }

    }

  }

  private void verifyHeaders(HttpServletResponse response) {
    verify(response).setCharacterEncoding("UTF-8");
    verify(response).setContentType("application/json");
    verify(response).setHeader("Cache-Control", "no-cache");
  }

  private void assertJson(String actual) {
    assertThat(actual)
      .isNotEmpty()
      .contains(StringUtils.deleteWhitespace(GIT_PLUGIN_JSON.substring(1, GIT_PLUGIN_JSON.length() - 1)))
      .contains(StringUtils.deleteWhitespace(HG_PLUGIN_JSON.substring(1, HG_PLUGIN_JSON.length() - 1)))
      .contains(StringUtils.deleteWhitespace(SVN_PLUGIN_JSON.substring(1, SVN_PLUGIN_JSON.length() - 1)));
  }
}
