package sonia.scm.web.i18n;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.sdorra.shiro.ShiroRule;
import com.github.sdorra.shiro.SubjectAware;
import com.google.common.base.Charsets;
import com.google.common.io.Files;
import org.assertj.core.util.Lists;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
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
import sonia.scm.plugin.UberClassLoader;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doCallRealMethod;
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
    "}\n";
  private static final String HG_PLUGIN_JSON = "{\n" +
    "  \"scm-hg-plugin\": {\n" +
    "    \"information\": {\n" +
    "      \"clone\" : \"Clone\",\n" +
    "      \"create\" : \"Create\",\n" +
    "      \"replace\" : \"Push\"\n" +
    "    }\n" +
    "  }\n" +
    "}\n";
  private static String SVN_PLUGIN_JSON = "{\n" +
    "  \"scm-svn-plugin\": {\n" +
    "    \"information\": {\n" +
    "      \"checkout\" : \"Checkout\"\n" +
    "    }\n" +
    "  }\n" +
    "}\n";

  @Rule
  public TemporaryFolder temporaryFolder = new TemporaryFolder();

  @Mock
  PluginLoader pluginLoader;

  @Mock
  CacheManager cacheManager;

  @Mock
  UberClassLoader uberClassLoader;

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
    when(pluginLoader.getUberClassLoader()).thenReturn(uberClassLoader);
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
    when(uberClassLoader.getResources("locales/de/plugins.json")).thenThrow(IOException.class);

    servlet.doGet(request, response);

    verify(response).setStatus(404);
  }

  @Test
  @SuppressWarnings("unchecked")
  public void shouldFailWith500OnIOException() throws IOException {
    String path = "/locales/de/plugins.json";
    HttpServletRequest request = mock(HttpServletRequest.class);
    HttpServletResponse response = mock(HttpServletResponse.class);
    PrintWriter writer = mock(PrintWriter.class);
    when(response.getWriter()).thenReturn(writer);
    when(request.getServletPath()).thenReturn(path);
    when(uberClassLoader.getResources("locales/de/plugins.json")).thenReturn(resources);
    doThrow(IOException.class).when(writer).write(any(String.class));

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
    PrintWriter writer = mock(PrintWriter.class);
    when(response.getWriter()).thenReturn(writer);
    when(request.getServletPath()).thenReturn(path);
    when(uberClassLoader.getResources("locales/de/plugins.json")).thenReturn(resources);
    ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
    doCallRealMethod().when(writer).write(captor.capture());

    servlet.doGet(request, response);

    assertJsonMap(jsonStringToMap(captor.getValue()));
    verify(cache, never()).get(any());
  }

  @Test
  @SuppressWarnings("unchecked")
  public void inProductionStageShouldUseCache() throws IOException {
    String path = "/locales/de/plugins.json";
    when(servlet.isProductionStage()).thenReturn(true);
    HttpServletRequest request = mock(HttpServletRequest.class);
    HttpServletResponse response = mock(HttpServletResponse.class);
    PrintWriter writer = mock(PrintWriter.class);
    when(response.getWriter()).thenReturn(writer);
    when(request.getServletPath()).thenReturn(path);
    when(uberClassLoader.getResources("locales/de/plugins.json")).thenReturn(resources);
    ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
    doCallRealMethod().when(writer).write(captor.capture());

    servlet.doGet(request, response);

    assertJsonMap(jsonStringToMap(captor.getValue()));
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
    PrintWriter writer = mock(PrintWriter.class);
    when(response.getWriter()).thenReturn(writer);
    when(request.getServletPath()).thenReturn(path);
    when(uberClassLoader.getResources("locales/de/plugins.json")).thenReturn(resources);
    ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
    doCallRealMethod().when(writer).write(captor.capture());
    Map cachedMap = jsonStringToMap(GIT_PLUGIN_JSON);
    cachedMap.putAll(jsonStringToMap(HG_PLUGIN_JSON));
    cachedMap.putAll(jsonStringToMap(SVN_PLUGIN_JSON));
    when(cache.get(path)).thenReturn(cachedMap);
    servlet.doGet(request, response);
    verify(servlet, never()).collectJsonFile(path);
    verify(cache, never()).put(eq(path), any());
    verify(cache).get(path);
    assertJsonMap(jsonStringToMap(captor.getValue()));
  }

  @Test
  @SuppressWarnings("unchecked")
  public void shouldCollectJsonFile() throws IOException {
    String path = "locales/de/plugins.json";

    when(uberClassLoader.getResources(path)).thenReturn(resources);
    Optional<Map> mapOptional = servlet.collectJsonFile("/" + path);

    assertJsonMap(mapOptional.orElse(null));
  }

  @SuppressWarnings("unchecked")
  public void assertJsonMap(Map actual) throws IOException {
    assertThat(actual)
      .isNotEmpty()
      .containsAllEntriesOf(jsonStringToMap(GIT_PLUGIN_JSON))
      .containsAllEntriesOf(jsonStringToMap(HG_PLUGIN_JSON))
      .containsAllEntriesOf(jsonStringToMap(SVN_PLUGIN_JSON));
  }

  public File createFileFromString(String json) throws IOException {
    File file = temporaryFolder.newFile();
    Files.write(json.getBytes(Charsets.UTF_8), file);
    return file;
  }

  private Map jsonStringToMap(String fileAsString) throws IOException {
    ObjectMapper mapper = new ObjectMapper();
    return mapper.readValue(fileAsString, Map.class);
  }

}
