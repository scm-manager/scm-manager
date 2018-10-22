package sonia.scm.web.i18n;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.sdorra.shiro.ShiroRule;
import com.github.sdorra.shiro.SubjectAware;
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
import sonia.scm.boot.RestartEvent;
import sonia.scm.cache.Cache;
import sonia.scm.cache.CacheManager;
import sonia.scm.event.ScmEventBus;
import sonia.scm.plugin.PluginLoader;
import sonia.scm.util.JacksonUtils;

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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.Silent.class)
@SubjectAware(configuration = "classpath:sonia/scm/shiro-001.ini")
public class I18nServletTest {

  @Rule
  public ShiroRule shiro = new ShiroRule();

  private static final String GIT_PLUGIN_JSON = "{\n" +
    "  \"scm-git-plugin\": {\n" +
    "    \"information\": {\n" +
    "      \"clone\" : \"Clone\",\n" +
    "      \"create\" : \"Create\",\n" +
    "      \"replace\" : \"Push\"\n" +
    "    }\n" +
    "  }\n" +
    "}";
  private static final String HG_PLUGIN_JSON = "{\n" +
    "  \"scm-hg-plugin\": {\n" +
    "    \"information\": {\n" +
    "      \"clone\" : \"Clone\",\n" +
    "      \"create\" : \"Create\",\n" +
    "      \"replace\" : \"Push\"\n" +
    "    }\n" +
    "  }\n" +
    "}";
  private static String SVN_PLUGIN_JSON = "{\n" +
    "  \"scm-svn-plugin\": {\n" +
    "    \"information\": {\n" +
    "      \"checkout\" : \"Checkout\"\n" +
    "    }\n" +
    "  }\n" +
    "}";

  @Rule
  public TemporaryFolder temporaryFolder = new TemporaryFolder();

  @Mock
  PluginLoader pluginLoader;

  @Mock
  CacheManager cacheManager;

  @Mock
  ClassLoader classLoader;

  I18nServlet servlet;

  @Mock
  private Cache cache;
  private Enumeration<URL> resources;

  @Before
  @SuppressWarnings("unchecked")
  public void init() throws IOException {
    resources = Collections.enumeration(Lists.newArrayList(
      createFileFromString(SVN_PLUGIN_JSON).toURL(),
      createFileFromString(GIT_PLUGIN_JSON).toURL(),
      createFileFromString(HG_PLUGIN_JSON).toURL()
    ));
    when(pluginLoader.getUberClassLoader()).thenReturn(classLoader);
    when(cacheManager.getCache(I18nServlet.CACHE_NAME)).thenReturn(cache);
    MockSettings settings = new MockSettingsImpl<>();
    settings.useConstructor(pluginLoader, cacheManager);
    settings.defaultAnswer(InvocationOnMock::callRealMethod);
    servlet = mock(I18nServlet.class, settings);
  }

  @Test
  public void shouldCleanCacheOnRestartEvent() {
    ScmEventBus.getInstance().register(servlet);

    ScmEventBus.getInstance().post(new RestartEvent(I18nServlet.class, "Restart to reload the plugin resources"));

    verify(cache).clear();
  }

  @Test
  @SuppressWarnings("unchecked")
  public void shouldFailWith404OnMissingResources() throws IOException {
    String path = "/locales/de/plugins.json";
    HttpServletRequest request = mock(HttpServletRequest.class);
    HttpServletResponse response = mock(HttpServletResponse.class);
    PrintWriter writer = mock(PrintWriter.class);
    when(response.getWriter()).thenReturn(writer);
    when(request.getServletPath()).thenReturn(path);
    when(classLoader.getResources("locales/de/plugins.json")).thenThrow(IOException.class);

    servlet.doGet(request, response);

    verify(response).setStatus(404);
  }

  @Test
  @SuppressWarnings("unchecked")
  public void shouldFailWith500OnIOException() throws IOException {
    HttpServletRequest request = mock(HttpServletRequest.class);
    HttpServletResponse response = mock(HttpServletResponse.class);
    doThrow(IOException.class).when(response).getWriter();

    servlet.doGet(request, response);

    verify(response).setStatus(500);
  }

  @Test
  @SuppressWarnings("unchecked")
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
  @SuppressWarnings("unchecked")
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
  }

  @Test
  @SuppressWarnings("unchecked")
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
    node = JacksonUtils.merge(node, objectMapper.readTree(HG_PLUGIN_JSON));
    node = JacksonUtils.merge(node, objectMapper.readTree(SVN_PLUGIN_JSON));
    when(cache.get(path)).thenReturn(node);

    servlet.doGet(request, response);

    String json = Files.readLines(file, Charset.defaultCharset()).get(0);
    verify(servlet, never()).collectJsonFile(path);
    verify(cache, never()).put(eq(path), any());
    verify(cache).get(path);
    assertJson(json);
  }

  @Test
  @SuppressWarnings("unchecked")
  public void shouldCollectJsonFile() throws IOException {
    String path = "locales/de/plugins.json";
    when(classLoader.getResources(path)).thenReturn(resources);

    Optional<JsonNode> jsonNodeOptional = servlet.collectJsonFile("/" + path);

    assertJson(jsonNodeOptional.orElse(null));
  }

  public void assertJson(JsonNode actual) throws IOException {
    assertJson(actual.toString());
  }

  public void assertJson(String actual) throws IOException {
    assertThat(actual)
      .isNotEmpty()
      .contains(StringUtils.deleteWhitespace(GIT_PLUGIN_JSON.substring(1, GIT_PLUGIN_JSON.length() - 1)))
      .contains(StringUtils.deleteWhitespace(HG_PLUGIN_JSON.substring(1, HG_PLUGIN_JSON.length() - 1)))
      .contains(StringUtils.deleteWhitespace(SVN_PLUGIN_JSON.substring(1, SVN_PLUGIN_JSON.length() - 1)));
  }

  public File createFileFromString(String json) throws IOException {
    File file = temporaryFolder.newFile();
    Files.write(json.getBytes(Charsets.UTF_8), file);
    return file;
  }

}
