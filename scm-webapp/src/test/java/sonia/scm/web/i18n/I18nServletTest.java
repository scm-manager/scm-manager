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
import com.google.common.base.Charsets;
import com.google.common.io.Files;
import org.apache.commons.lang3.StringUtils;
import org.assertj.core.util.Lists;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockSettings;
import org.mockito.internal.creation.MockSettingsImpl;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.MockitoJUnitRunner;
import sonia.scm.cache.Cache;
import sonia.scm.cache.CacheManager;
import sonia.scm.event.ScmEventBus;
import sonia.scm.lifecycle.RestartEventFactory;
import sonia.scm.plugin.PluginLoader;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.Silent.class)
public class I18nServletTest {

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

  private static String json(String... parts) {
    return String.join("\n", parts ).replaceAll("'", "\"");
  }

  @Rule
  public TemporaryFolder temporaryFolder = new TemporaryFolder();

  @Mock
  private PluginLoader pluginLoader;

  @Mock
  private CacheManager cacheManager;

  @Mock
  private ClassLoader classLoader;

  private I18nServlet servlet;

  @Mock
  private Cache<String, JsonNode> cache;

  private Enumeration<URL> resources;

  @Before
  public void init() throws IOException {
    resources = Collections.enumeration(Lists.newArrayList(
      createFileFromString(SVN_PLUGIN_JSON).toURI().toURL(),
      createFileFromString(GIT_PLUGIN_JSON).toURI().toURL(),
      createFileFromString(HG_PLUGIN_JSON).toURI().toURL()
    ));
    when(pluginLoader.getUberClassLoader()).thenReturn(classLoader);
    when(cacheManager.<String, JsonNode>getCache(I18nServlet.CACHE_NAME)).thenReturn(cache);
    MockSettings settings = new MockSettingsImpl<>();
    settings.useConstructor(pluginLoader, cacheManager);
    settings.defaultAnswer(InvocationOnMock::callRealMethod);
    servlet = mock(I18nServlet.class, settings);
  }

  @Test
  public void shouldCleanCacheOnRestartEvent() {
    ScmEventBus.getInstance().register(servlet);

    ScmEventBus.getInstance().post(RestartEventFactory.create(I18nServlet.class, "Restart to reload the plugin resources"));

    verify(cache).clear();
  }

  @Test
  public void shouldFailWith404OnMissingResources() throws IOException {
    String path = "/locales/de/plugins.json";
    HttpServletRequest request = mock(HttpServletRequest.class);
    HttpServletResponse response = mock(HttpServletResponse.class);
    PrintWriter writer = mock(PrintWriter.class);
    when(response.getWriter()).thenReturn(writer);
    when(request.getServletPath()).thenReturn(path);
    when(classLoader.getResources("locales/de/plugins.json")).thenReturn(
      I18nServlet.class.getClassLoader().getResources("something/not/available")
    );

    servlet.doGet(request, response);

    verify(response).setStatus(404);
  }

  @Test
  public void shouldFailWith500OnIOException() throws IOException {
    when(servlet.isProductionStage()).thenReturn(false);
    HttpServletRequest request = mock(HttpServletRequest.class);
    when(request.getServletPath()).thenReturn("/locales/de/plugins.json");
    HttpServletResponse response = mock(HttpServletResponse.class);
    doThrow(IOException.class).when(response).getWriter();

    servlet.doGet(request, response);

    verify(response).setStatus(500);
  }

  @Test
  @SuppressWarnings("UnstableApiUsage")
  public void inDevelopmentStageShouldNotUseCache() throws IOException {
    String path = "/locales/de/plugins.json";
    when(servlet.isProductionStage()).thenReturn(false);
    HttpServletRequest request = mock(HttpServletRequest.class);
    HttpServletResponse response = mock(HttpServletResponse.class);
    File file = temporaryFolder.newFile();
    PrintWriter writer = new PrintWriter(new FileOutputStream(file));
    when(response.getWriter()).thenReturn(writer);
    when(request.getServletPath()).thenReturn(path);
    when(classLoader.getResources("locales/de/plugins.json")).thenReturn(resources);

    servlet.doGet(request, response);

    String json = Files.readLines(file, Charset.defaultCharset()).get(0);
    assertJson(json);
    verify(cache, never()).get(any());
  }

  @Test
  @SuppressWarnings("UnstableApiUsage")
  public void inProductionStageShouldUseCache() throws IOException {
    String path = "/locales/de/plugins.json";
    when(servlet.isProductionStage()).thenReturn(true);
    HttpServletRequest request = mock(HttpServletRequest.class);
    HttpServletResponse response = mock(HttpServletResponse.class);
    File file = temporaryFolder.newFile();
    PrintWriter writer = new PrintWriter(new FileOutputStream(file));
    when(response.getWriter()).thenReturn(writer);
    when(request.getServletPath()).thenReturn(path);
    when(classLoader.getResources("locales/de/plugins.json")).thenReturn(resources);

    servlet.doGet(request, response);

    String json = Files.readLines(file, Charset.defaultCharset()).get(0);
    assertJson(json);
    verify(cache).get(path);
    verify(cache).put(eq(path), any());

    verifyHeaders(response);
  }

  @Test
  @SuppressWarnings("UnstableApiUsage")
  public void inProductionStageShouldGetFromCache() throws IOException {
    String path = "/locales/de/plugins.json";
    when(servlet.isProductionStage()).thenReturn(true);
    HttpServletRequest request = mock(HttpServletRequest.class);
    HttpServletResponse response = mock(HttpServletResponse.class);
    File file = temporaryFolder.newFile();
    PrintWriter writer = new PrintWriter(new FileOutputStream(file));
    when(response.getWriter()).thenReturn(writer);
    when(request.getServletPath()).thenReturn(path);
    when(classLoader.getResources("locales/de/plugins.json")).thenReturn(resources);
    ObjectMapper objectMapper = new ObjectMapper();
    JsonNode node = objectMapper.readTree(GIT_PLUGIN_JSON);
    node = servlet.merge(node, objectMapper.readTree(HG_PLUGIN_JSON));
    node = servlet.merge(node, objectMapper.readTree(SVN_PLUGIN_JSON));
    when(cache.get(path)).thenReturn(node);

    servlet.doGet(request, response);

    String json = Files.readLines(file, Charset.defaultCharset()).get(0);
    verify(servlet, never()).collectJsonFile(path);
    verify(cache, never()).put(eq(path), any());
    verify(cache).get(path);
    assertJson(json);

    verifyHeaders(response);
  }

  @Test
  public void shouldCollectJsonFile() throws IOException {
    String path = "locales/de/plugins.json";
    when(classLoader.getResources(path)).thenReturn(resources);

    Optional<JsonNode> jsonNodeOptional = servlet.collectJsonFile("/" + path);

    assertJson(jsonNodeOptional.orElse(null));
  }

  private void verifyHeaders(HttpServletResponse response) {
    verify(response).setCharacterEncoding("UTF-8");
    verify(response).setContentType("application/json");
    verify(response).setHeader("Cache-Control", "no-cache");
  }

  public void assertJson(JsonNode actual) throws IOException {
    assertJson(actual.toString());
  }

  private void assertJson(String actual) throws IOException {
    assertThat(actual)
      .isNotEmpty()
      .contains(StringUtils.deleteWhitespace(GIT_PLUGIN_JSON.substring(1, GIT_PLUGIN_JSON.length() - 1)))
      .contains(StringUtils.deleteWhitespace(HG_PLUGIN_JSON.substring(1, HG_PLUGIN_JSON.length() - 1)))
      .contains(StringUtils.deleteWhitespace(SVN_PLUGIN_JSON.substring(1, SVN_PLUGIN_JSON.length() - 1)));
  }

  private File createFileFromString(String json) throws IOException {
    File file = temporaryFolder.newFile();
    Files.write(json.getBytes(Charsets.UTF_8), file);
    return file;
  }

}
