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

package sonia.scm.api.v2.resources;

import de.otto.edison.hal.HalRepresentation;
import org.apache.shiro.authz.UnauthorizedException;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.ThreadContext;
import org.jboss.resteasy.mock.MockHttpRequest;
import org.jboss.resteasy.mock.MockHttpResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.plugin.AvailablePlugin;
import sonia.scm.plugin.AvailablePluginDescriptor;
import sonia.scm.plugin.InstalledPlugin;
import sonia.scm.plugin.InstalledPluginDescriptor;
import sonia.scm.plugin.PluginCondition;
import sonia.scm.plugin.PluginInformation;
import sonia.scm.plugin.PluginManager;
import sonia.scm.plugin.PluginSet;
import sonia.scm.web.RestDispatcher;
import sonia.scm.web.VndMediaType;

import javax.inject.Provider;
import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AvailablePluginResourceTest {

  private RestDispatcher dispatcher = new RestDispatcher();

  @Mock
  Provider<AvailablePluginResource> availablePluginResourceProvider;

  @Mock
  private PluginDtoCollectionMapper collectionMapper;

  @Mock
  private PluginManager pluginManager;

  @Mock
  private PluginDtoMapper mapper;

  @InjectMocks
  AvailablePluginResource availablePluginResource;

  PluginRootResource pluginRootResource;

  @Mock
  Subject subject;


  @BeforeEach
  void prepareEnvironment() {
    pluginRootResource = new PluginRootResource(null, availablePluginResourceProvider, null, null);
    when(availablePluginResourceProvider.get()).thenReturn(availablePluginResource);
    dispatcher.addSingletonResource(pluginRootResource);
  }

  @Nested
  class withAuthorization {

    @BeforeEach
    void bindSubject() {
      ThreadContext.bind(subject);
      doNothing().when(subject).checkPermission(any(String.class));
    }

    @AfterEach
    public void unbindSubject() {
      ThreadContext.unbindSubject();
    }

    @Test
    void getAvailablePlugins() throws URISyntaxException, UnsupportedEncodingException {
      AvailablePlugin plugin = createAvailablePlugin();
      PluginSet pluginSet = createPluginSet();

      when(pluginManager.getAvailable()).thenReturn(Collections.singletonList(plugin));
      when(pluginManager.getInstalled()).thenReturn(Collections.emptyList());
      when(pluginManager.getPluginSets()).thenReturn(Collections.singleton(pluginSet));
      when(collectionMapper.mapAvailable(Collections.singletonList(plugin))).thenReturn(new MockedResultDto());

      MockHttpRequest request = MockHttpRequest.get("/v2/plugins/available");
      request.accept(VndMediaType.PLUGIN_COLLECTION);
      MockHttpResponse response = new MockHttpResponse();

      dispatcher.invoke(request, response);

      assertThat(HttpServletResponse.SC_OK).isEqualTo(response.getStatus());
      assertThat(response.getContentAsString()).contains("\"marker\":\"x\"");
    }

    @Test
    void shouldNotReturnInstalledPlugins() throws URISyntaxException, UnsupportedEncodingException {
      AvailablePlugin availablePlugin = createAvailablePlugin();
      InstalledPlugin installedPlugin = createInstalledPlugin();
      PluginSet pluginSet = createPluginSet();

      when(pluginManager.getAvailable()).thenReturn(Collections.singletonList(availablePlugin));
      when(pluginManager.getInstalled()).thenReturn(Collections.singletonList(installedPlugin));
      when(pluginManager.getPluginSets()).thenReturn(Collections.singleton(pluginSet));
      lenient().when(collectionMapper.mapAvailable(Collections.singletonList(availablePlugin))).thenReturn(new MockedResultDto());

      MockHttpRequest request = MockHttpRequest.get("/v2/plugins/available");
      request.accept(VndMediaType.PLUGIN_COLLECTION);
      MockHttpResponse response = new MockHttpResponse();

      dispatcher.invoke(request, response);

      assertThat(HttpServletResponse.SC_OK).isEqualTo(response.getStatus());
      assertThat(response.getContentAsString()).doesNotContain("\"marker\":\"x\"");
    }

    @Test
    void getAvailablePlugin() throws UnsupportedEncodingException, URISyntaxException {
      PluginInformation pluginInformation = new PluginInformation();
      pluginInformation.setName("pluginName");
      pluginInformation.setVersion("2.0.0");

      AvailablePlugin plugin = createAvailablePlugin(pluginInformation);

      when(pluginManager.getAvailable("pluginName")).thenReturn(Optional.of(plugin));

      PluginDto pluginDto = new PluginDto();
      pluginDto.setName("pluginName");
      when(mapper.mapAvailable(plugin)).thenReturn(pluginDto);

      MockHttpRequest request = MockHttpRequest.get("/v2/plugins/available/pluginName");
      request.accept(VndMediaType.PLUGIN);
      MockHttpResponse response = new MockHttpResponse();

      dispatcher.invoke(request, response);

      assertThat(HttpServletResponse.SC_OK).isEqualTo(response.getStatus());
      assertThat(response.getContentAsString()).contains("\"name\":\"pluginName\"");
    }

    @Test
    void installPlugin() throws URISyntaxException {
      MockHttpRequest request = MockHttpRequest.post("/v2/plugins/available/pluginName/install");
      MockHttpResponse response = new MockHttpResponse();

      dispatcher.invoke(request, response);

      verify(pluginManager).install("pluginName", false);
      assertThat(HttpServletResponse.SC_OK).isEqualTo(response.getStatus());
    }
  }

  private PluginSet createPluginSet() {
    return new PluginSet(
      "default",
      0,
      Collections.singleton("scm-some-plugin"),
      Collections.singletonMap("en",
        new PluginSet.Description(
          "Default",
          Collections.singleton("this is an awesome set")
        )
      )
    );
  }

  private AvailablePlugin createAvailablePlugin() {
    PluginInformation pluginInformation = new PluginInformation();
    pluginInformation.setName("scm-some-plugin");
    return createAvailablePlugin(pluginInformation);
  }

  private AvailablePlugin createAvailablePlugin(PluginInformation pluginInformation) {
    AvailablePluginDescriptor descriptor = new AvailablePluginDescriptor(
      pluginInformation, new PluginCondition(), Collections.emptySet(), "https://download.hitchhiker.com", null
    );
    return new AvailablePlugin(descriptor);
  }

  private InstalledPlugin createInstalledPlugin() {
    PluginInformation pluginInformation = new PluginInformation();
    pluginInformation.setName("scm-some-plugin");
    return createInstalledPlugin(pluginInformation);
  }

  private InstalledPlugin createInstalledPlugin(PluginInformation pluginInformation) {
    InstalledPluginDescriptor descriptor = mock(InstalledPluginDescriptor.class);
    lenient().when(descriptor.getInformation()).thenReturn(pluginInformation);
    return new InstalledPlugin(descriptor, null, null, null, false);
  }

  @Nested
  class WithoutAuthorization {

    @BeforeEach
    void bindSubject() {
      ThreadContext.bind(subject);
      doThrow(new UnauthorizedException()).when(subject).checkPermission(any(String.class));
    }

    @AfterEach
    public void unbindSubject() {
      ThreadContext.unbindSubject();
    }

    @Test
    void shouldNotGetAvailablePluginsIfMissingPermission() throws URISyntaxException {
      MockHttpRequest request = MockHttpRequest.get("/v2/plugins/available");
      request.accept(VndMediaType.PLUGIN_COLLECTION);
      MockHttpResponse response = new MockHttpResponse();

      dispatcher.invoke(request, response);

      assertEquals(HttpServletResponse.SC_FORBIDDEN, response.getStatus());
      verify(subject).checkPermission(any(String.class));
    }

    @Test
    void shouldNotGetAvailablePluginIfMissingPermission() throws URISyntaxException {
      MockHttpRequest request = MockHttpRequest.get("/v2/plugins/available/pluginName");
      request.accept(VndMediaType.PLUGIN);
      MockHttpResponse response = new MockHttpResponse();

      dispatcher.invoke(request, response);

      assertEquals(HttpServletResponse.SC_FORBIDDEN, response.getStatus());
      verify(subject).checkPermission(any(String.class));
    }

    @Test
    void shouldNotInstallPluginIfMissingPermission() throws URISyntaxException {
      MockHttpRequest request = MockHttpRequest.post("/v2/plugins/available/pluginName/install");
      request.accept(VndMediaType.PLUGIN);
      MockHttpResponse response = new MockHttpResponse();

      dispatcher.invoke(request, response);

      assertEquals(HttpServletResponse.SC_FORBIDDEN, response.getStatus());
      verify(subject).checkPermission(any(String.class));
    }
  }

  public class MockedResultDto extends HalRepresentation {
    public String getMarker() {
      return "x";
    }
  }
}
