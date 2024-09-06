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

package sonia.scm.api.v2.resources;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.inject.util.Providers;
import jakarta.servlet.http.HttpServletRequest;
import org.jboss.resteasy.mock.MockHttpRequest;
import org.jboss.resteasy.mock.MockHttpResponse;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import sonia.scm.plugin.InstalledPlugin;
import sonia.scm.plugin.InstalledPluginDescriptor;
import sonia.scm.plugin.PluginInformation;
import sonia.scm.plugin.PluginLoader;
import sonia.scm.plugin.PluginResources;
import sonia.scm.web.RestDispatcher;
import sonia.scm.web.VndMediaType;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashSet;

import static jakarta.servlet.http.HttpServletResponse.SC_NOT_FOUND;
import static jakarta.servlet.http.HttpServletResponse.SC_OK;
import static org.hamcrest.Matchers.equalToIgnoringCase;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.Silent.class)
public class UIRootResourceTest {

  private RestDispatcher dispatcher = new RestDispatcher();

  @Mock
  private PluginLoader pluginLoader;

  @Mock
  private HttpServletRequest request;

  private final URI baseUri = URI.create("/");
  private final ResourceLinks resourceLinks = ResourceLinksMock.createMock(baseUri);

  @Before
  public void setUpRestService() {
    UIPluginDtoMapper mapper = new UIPluginDtoMapper(resourceLinks, request);
    UIPluginDtoCollectionMapper collectionMapper = new UIPluginDtoCollectionMapper(resourceLinks, mapper);

    UIPluginResource pluginResource = new UIPluginResource(pluginLoader, collectionMapper, mapper);
    UIRootResource rootResource = new UIRootResource(Providers.of(pluginResource));

    dispatcher.addSingletonResource(rootResource);
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
  public void shouldReturnPlugin() throws URISyntaxException, UnsupportedEncodingException {
    mockPlugins(mockPlugin("awesome", "Awesome", createPluginResources("my/awesome.bundle.js")));

    MockHttpRequest request = MockHttpRequest.get("/v2/ui/plugins/awesome");
    MockHttpResponse response = new MockHttpResponse();

    dispatcher.invoke(request, response);

    assertEquals(SC_OK, response.getStatus());
    assertTrue(response.getContentAsString().contains("Awesome"));
    assertTrue(response.getContentAsString().contains("my/awesome.bundle.js"));
  }

  @Test
  public void shouldReturnPlugins() throws URISyntaxException, UnsupportedEncodingException {
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
  public void shouldNotReturnPluginsWithoutResources() throws URISyntaxException, UnsupportedEncodingException {
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

  @Test
  public void shouldHaveBundleWithContextPath() throws Exception {
    when(request.getContextPath()).thenReturn("/scm");
    mockPlugins(mockPlugin("awesome", "Awesome", createPluginResources("my/bundle.js")));

    String uri = "/v2/ui/plugins/awesome";
    MockHttpRequest request = MockHttpRequest.get(uri);
    MockHttpResponse response = new MockHttpResponse();

    dispatcher.invoke(request, response);

    assertEquals(SC_OK, response.getStatus());

    System.out.println();

    assertTrue(response.getContentAsString().contains("/scm/my/bundle.js"));
  }

  private void mockPlugins(InstalledPlugin... plugins) {
    when(pluginLoader.getInstalledPlugins()).thenReturn(Lists.newArrayList(plugins));
  }

  private PluginResources createPluginResources(String... bundles) {
    HashSet<String> scripts = Sets.newHashSet(bundles);
    HashSet<String> styles = Sets.newHashSet();
    return new PluginResources(scripts, styles);
  }

  private InstalledPlugin mockPlugin(String id) {
    return mockPlugin(id, id, null);
  }

  private InstalledPlugin mockPlugin(String id, String name, PluginResources pluginResources) {
    InstalledPlugin wrapper = mock(InstalledPlugin.class);
    when(wrapper.getId()).thenReturn(id);

    InstalledPluginDescriptor plugin = mock(InstalledPluginDescriptor.class);
    when(wrapper.getDescriptor()).thenReturn(plugin);
    when(plugin.getResources()).thenReturn(pluginResources);

    PluginInformation information = mock(PluginInformation.class);
    when(plugin.getInformation()).thenReturn(information);
    when(information.getName()).thenReturn(name);

    return wrapper;
  }
}
