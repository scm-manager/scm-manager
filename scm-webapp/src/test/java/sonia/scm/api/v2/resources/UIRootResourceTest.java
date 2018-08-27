package sonia.scm.api.v2.resources;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.inject.util.Providers;
import org.jboss.resteasy.core.Dispatcher;
import org.jboss.resteasy.mock.MockDispatcherFactory;
import org.jboss.resteasy.mock.MockHttpRequest;
import org.jboss.resteasy.mock.MockHttpResponse;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import sonia.scm.api.rest.resources.PluginResource;
import sonia.scm.plugin.*;
import sonia.scm.web.VndMediaType;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.List;

import static javax.servlet.http.HttpServletResponse.SC_NOT_FOUND;
import static javax.servlet.http.HttpServletResponse.SC_OK;
import static org.hamcrest.Matchers.equalToIgnoringCase;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.Silent.class)
public class UIRootResourceTest {

  private final Dispatcher dispatcher = MockDispatcherFactory.createDispatcher();

  @Mock
  private PluginLoader pluginLoader;

  private final URI baseUri = URI.create("/");
  private final ResourceLinks resourceLinks = ResourceLinksMock.createMock(baseUri);

  @Before
  public void setUpRestService() {
    UIPluginDtoMapper mapper = new UIPluginDtoMapper(resourceLinks);
    UIPluginDtoCollectionMapper collectionMapper = new UIPluginDtoCollectionMapper(resourceLinks, mapper);

    UIPluginResource pluginResource = new UIPluginResource(pluginLoader, collectionMapper, mapper);
    UIRootResource rootResource = new UIRootResource(Providers.of(pluginResource));

    dispatcher.getRegistry().addSingletonResource(rootResource);
  }

  @Test
  public void shouldHaveVndCollectionMediaType() throws URISyntaxException {
    MockHttpRequest request = MockHttpRequest.get("/v2/ui/plugins");
    MockHttpResponse response = new MockHttpResponse();

    dispatcher.invoke(request, response);

    assertEquals(SC_OK, response.getStatus());
    String contentType = response.getOutputHeaders().getFirst("Content-Type").toString();
    assertThat(VndMediaType.UI_PLUGIN_COLLECTION, equalToIgnoringCase(contentType));
  }

  @Test
  public void shouldReturnNotFoundIfPluginNotAvailable() throws URISyntaxException {
    MockHttpRequest request = MockHttpRequest.get("/v2/ui/plugins/awesome");
    MockHttpResponse response = new MockHttpResponse();

    dispatcher.invoke(request, response);

    assertEquals(SC_NOT_FOUND, response.getStatus());
  }

  @Test
  public void shouldReturnNotFoundIfPluginHasNoResources() throws URISyntaxException {
    mockPlugins(mockPlugin("awesome"));

    MockHttpRequest request = MockHttpRequest.get("/v2/ui/plugins/awesome");
    MockHttpResponse response = new MockHttpResponse();

    dispatcher.invoke(request, response);

    assertEquals(SC_NOT_FOUND, response.getStatus());
  }

  @Test
  public void shouldReturnPlugin() throws URISyntaxException {
    mockPlugins(mockPlugin("awesome", "Awesome", createPluginResources("my/awesome.bundle.js")));

    MockHttpRequest request = MockHttpRequest.get("/v2/ui/plugins/awesome");
    MockHttpResponse response = new MockHttpResponse();

    dispatcher.invoke(request, response);

    assertEquals(SC_OK, response.getStatus());
    assertTrue(response.getContentAsString().contains("Awesome"));
    assertTrue(response.getContentAsString().contains("my/awesome.bundle.js"));
  }

  @Test
  public void shouldReturnPlugins() throws URISyntaxException {
    mockPlugins(
      mockPlugin("awesome", "Awesome", createPluginResources("my/awesome.bundle.js")),
      mockPlugin("special", "Special", createPluginResources("my/special.bundle.js"))
    );

    MockHttpRequest request = MockHttpRequest.get("/v2/ui/plugins");
    MockHttpResponse response = new MockHttpResponse();

    dispatcher.invoke(request, response);

    assertEquals(SC_OK, response.getStatus());
    assertTrue(response.getContentAsString().contains("Awesome"));
    assertTrue(response.getContentAsString().contains("my/awesome.bundle.js"));
    assertTrue(response.getContentAsString().contains("Special"));
    assertTrue(response.getContentAsString().contains("my/special.bundle.js"));
  }

  @Test
  public void shouldNotReturnPluginsWithoutResources() throws URISyntaxException {
    mockPlugins(
      mockPlugin("awesome", "Awesome", createPluginResources("my/awesome.bundle.js")),
      mockPlugin("special")
    );

    MockHttpRequest request = MockHttpRequest.get("/v2/ui/plugins");
    MockHttpResponse response = new MockHttpResponse();

    dispatcher.invoke(request, response);

    assertEquals(SC_OK, response.getStatus());
    assertTrue(response.getContentAsString().contains("Awesome"));
    assertTrue(response.getContentAsString().contains("my/awesome.bundle.js"));
    assertFalse(response.getContentAsString().contains("Special"));
  }

  @Test
  public void shouldHaveSelfLink() throws Exception {
    mockPlugins(mockPlugin("awesome", "Awesome", createPluginResources("my/bundle.js")));

    String uri = "/v2/ui/plugins/awesome";
    MockHttpRequest request = MockHttpRequest.get(uri);
    MockHttpResponse response = new MockHttpResponse();

    dispatcher.invoke(request, response);

    assertEquals(SC_OK, response.getStatus());
    assertTrue(response.getContentAsString().contains("\"self\":{\"href\":\"" + uri + "\"}"));
  }

  private void mockPlugins(PluginWrapper... plugins) {
    when(pluginLoader.getInstalledPlugins()).thenReturn(Lists.newArrayList(plugins));
  }

  private PluginResources createPluginResources(String... bundles) {
    HashSet<String> scripts = Sets.newHashSet(bundles);
    HashSet<String> styles = Sets.newHashSet();
    return new PluginResources(scripts, styles);
  }

  private PluginWrapper mockPlugin(String id) {
    return mockPlugin(id, id, null);
  }

  private PluginWrapper mockPlugin(String id, String name, PluginResources pluginResources) {
    PluginWrapper wrapper = mock(PluginWrapper.class);
    when(wrapper.getId()).thenReturn(id);

    Plugin plugin = mock(Plugin.class);
    when(wrapper.getPlugin()).thenReturn(plugin);
    when(plugin.getResources()).thenReturn(pluginResources);

    PluginInformation information = mock(PluginInformation.class);
    when(plugin.getInformation()).thenReturn(information);
    when(information.getName()).thenReturn(name);

    return wrapper;
  }
}
