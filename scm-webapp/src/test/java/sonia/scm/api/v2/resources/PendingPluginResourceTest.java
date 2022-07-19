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

import com.google.inject.util.Providers;
import org.apache.shiro.ShiroException;
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
import sonia.scm.lifecycle.Restarter;
import sonia.scm.plugin.AvailablePlugin;
import sonia.scm.plugin.AvailablePluginDescriptor;
import sonia.scm.plugin.InstalledPlugin;
import sonia.scm.plugin.InstalledPluginDescriptor;
import sonia.scm.plugin.PendingPlugins;
import sonia.scm.plugin.PluginInformation;
import sonia.scm.plugin.PluginManager;
import sonia.scm.web.RestDispatcher;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.Response;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;

import static java.net.URI.create;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PendingPluginResourceTest {

  private final RestDispatcher dispatcher = new RestDispatcher();

  @SuppressWarnings("unused")
  ResourceLinks resourceLinks = ResourceLinksMock.createMock(create("/"));

  @Mock
  PluginManager pluginManager;

  @Mock
  Restarter restarter;

  @Mock
  PluginDtoMapper mapper;

  @Mock
  Subject subject;

  @InjectMocks
  PendingPluginResource pendingPluginResource;

  MockHttpResponse response = new MockHttpResponse();

  @BeforeEach
  void prepareEnvironment() {
    dispatcher.registerException(ShiroException.class, Response.Status.UNAUTHORIZED);
    PluginRootResource pluginRootResource = new PluginRootResource(null, null, Providers.of(pendingPluginResource), null);
    dispatcher.addSingletonResource(pluginRootResource);
  }

  @BeforeEach
  void mockMapper() {
    lenient().when(mapper.mapAvailable(any())).thenAnswer(invocation -> {
      PluginDto dto = new PluginDto();
      dto.setName(((AvailablePlugin) invocation.getArgument(0)).getDescriptor().getInformation().getName());
      return dto;
    });
    lenient().when(mapper.mapInstalled(any(), any())).thenAnswer(invocation -> {
      PluginDto dto = new PluginDto();
      dto.setName(((InstalledPlugin) invocation.getArgument(0)).getDescriptor().getInformation().getName());
      return dto;
    });
  }

  @Nested
  class withAuthorization {

    @BeforeEach
    void bindSubject() {
      ThreadContext.bind(subject);
      lenient().when(subject.isPermitted("plugin:write")).thenReturn(true);
      lenient().when(restarter.isSupported()).thenReturn(true);
    }

    @AfterEach
    void unbindSubject() {
      ThreadContext.unbindSubject();
    }

    @Test
    void shouldGetEmptyPluginListsWithoutInstallLinkWhenNoPendingPluginsPresent() throws URISyntaxException, UnsupportedEncodingException {
      PendingPlugins pendingPlugins = mock(PendingPlugins.class);
      when(pluginManager.getPending()).thenReturn(pendingPlugins);

      MockHttpRequest request = MockHttpRequest.get("/v2/plugins/pending");
      dispatcher.invoke(request, response);

      assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_OK);
      assertThat(response.getContentAsString()).contains("\"_links\":{\"self\":{\"href\":\"/v2/plugins/pending\"}}");
    }

    @Test
    void shouldGetPendingAvailablePluginListWithInstallAndCancelLink() throws URISyntaxException, UnsupportedEncodingException {
      AvailablePlugin availablePlugin = createAvailablePlugin("pending-available-plugin");
      PendingPlugins pendingPlugins = mock(PendingPlugins.class);
      when(pluginManager.getPending()).thenReturn(pendingPlugins);
      when(pendingPlugins.getInstall()).thenReturn(singletonList(availablePlugin));

      MockHttpRequest request = MockHttpRequest.get("/v2/plugins/pending");
      dispatcher.invoke(request, response);

      assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_OK);
      assertThat(response.getContentAsString()).contains("\"new\":[{\"name\":\"pending-available-plugin\"");
      assertThat(response.getContentAsString()).contains("\"execute\":{\"href\":\"/v2/plugins/pending/execute\"}");
      assertThat(response.getContentAsString()).contains("\"cancel\":{\"href\":\"/v2/plugins/pending/cancel\"}");
    }

    @Test
    void shouldGetPendingUpdatePluginListWithInstallLink() throws URISyntaxException, UnsupportedEncodingException {
      AvailablePlugin availablePlugin = createAvailablePlugin("available-plugin");
      InstalledPlugin installedPlugin = createInstalledPlugin("available-plugin");
      PendingPlugins pendingPlugins = mock(PendingPlugins.class);
      when(pluginManager.getPending()).thenReturn(pendingPlugins);
      when(pendingPlugins.getUpdate()).thenReturn(singletonList(installedPlugin));
      when(pendingPlugins.getInstall()).thenReturn(singletonList(availablePlugin));

      MockHttpRequest request = MockHttpRequest.get("/v2/plugins/pending");
      dispatcher.invoke(request, response);

      assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_OK);
      assertThat(response.getContentAsString()).contains("\"update\":[{\"name\":\"available-plugin\"");
      assertThat(response.getContentAsString()).contains("\"execute\":{\"href\":\"/v2/plugins/pending/execute\"}");
    }

    @Test
    void shouldGetPendingUninstallPluginListWithInstallLink() throws URISyntaxException, UnsupportedEncodingException {
      InstalledPlugin installedPlugin = createInstalledPlugin("uninstalled-plugin");
      PendingPlugins pendingPlugins = mock(PendingPlugins.class);
      when(pendingPlugins.getUninstall()).thenReturn(singletonList(installedPlugin));
      when(pluginManager.getPending()).thenReturn(pendingPlugins);

      MockHttpRequest request = MockHttpRequest.get("/v2/plugins/pending");
      dispatcher.invoke(request, response);

      assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_OK);
      assertThat(response.getContentAsString()).contains("\"uninstall\":[{\"name\":\"uninstalled-plugin\"");
      assertThat(response.getContentAsString()).contains("\"execute\":{\"href\":\"/v2/plugins/pending/execute\"}");
    }

    @Test
    void shouldNotReturnExecuteLinkIfRestartIsNotSupported() throws URISyntaxException, UnsupportedEncodingException {
      when(restarter.isSupported()).thenReturn(false);

      InstalledPlugin installedPlugin = createInstalledPlugin("uninstalled-plugin");
      PendingPlugins pendingPlugins = mock(PendingPlugins.class);
      when(pluginManager.getPending()).thenReturn(pendingPlugins);
      when(pendingPlugins.getUninstall()).thenReturn(singletonList(installedPlugin));

      MockHttpRequest request = MockHttpRequest.get("/v2/plugins/pending");
      dispatcher.invoke(request, response);

      assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_OK);
      assertThat(response.getContentAsString()).contains("\"uninstall\":[{\"name\":\"uninstalled-plugin\"");
      assertThat(response.getContentAsString()).doesNotContain("\"execute\"");
    }

    @Test
    void shouldExecutePendingPlugins() throws URISyntaxException {
      MockHttpRequest request = MockHttpRequest.post("/v2/plugins/pending/execute");

      dispatcher.invoke(request, response);

      assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_OK);
      verify(pluginManager).executePendingAndRestart();
    }

    @Test
    void shouldCancelPendingPlugins() throws URISyntaxException {
      MockHttpRequest request = MockHttpRequest.post("/v2/plugins/pending/cancel");

      dispatcher.invoke(request, response);

      assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_OK);
      verify(pluginManager).cancelPending();
    }

  }

  @Nested
  class WithoutAuthorization {

    @BeforeEach
    void bindSubject() {
      ThreadContext.bind(subject);
      when(subject.isPermitted("plugin:write")).thenReturn(false);
    }

    @AfterEach
    void unbindSubject() {
      ThreadContext.unbindSubject();
    }

    @Test
    void shouldGetPendingAvailablePluginListWithoutInstallAndCancelLink() throws URISyntaxException, UnsupportedEncodingException {
      AvailablePlugin availablePlugin = createAvailablePlugin("pending-available-plugin");
      PendingPlugins pendingPlugins = mock(PendingPlugins.class);
      when(pluginManager.getPending()).thenReturn(pendingPlugins);
      when(pendingPlugins.getInstall()).thenReturn(singletonList(availablePlugin));

      MockHttpRequest request = MockHttpRequest.get("/v2/plugins/pending");
      dispatcher.invoke(request, response);

      assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_OK);
      assertThat(response.getContentAsString()).contains("\"new\":[{\"name\":\"pending-available-plugin\"");
      assertThat(response.getContentAsString()).doesNotContain("\"execute\":{\"href\":\"/v2/plugins/pending/execute\"}");
      assertThat(response.getContentAsString()).doesNotContain("\"cancel\":{\"href\":\"/v2/plugins/pending/cancel\"}");
    }
  }

  private AvailablePlugin createAvailablePlugin(String name) {
    PluginInformation pluginInformation = new PluginInformation();
    pluginInformation.setName(name);
    return createAvailablePlugin(pluginInformation);
  }

  private AvailablePlugin createAvailablePlugin(PluginInformation pluginInformation) {
    AvailablePluginDescriptor descriptor = mock(AvailablePluginDescriptor.class);
    lenient().when(descriptor.getInformation()).thenReturn(pluginInformation);
    AvailablePlugin availablePlugin = mock(AvailablePlugin.class);
    lenient().when(availablePlugin.getDescriptor()).thenReturn(descriptor);
    return availablePlugin;
  }

  private InstalledPlugin createInstalledPlugin(String name) {
    PluginInformation pluginInformation = new PluginInformation();
    pluginInformation.setName(name);
    return createInstalledPlugin(pluginInformation);
  }

  private InstalledPlugin createInstalledPlugin(PluginInformation pluginInformation) {
    InstalledPluginDescriptor descriptor = mock(InstalledPluginDescriptor.class);
    lenient().when(descriptor.getInformation()).thenReturn(pluginInformation);
    InstalledPlugin installedPlugin = mock(InstalledPlugin.class);
    lenient().when(installedPlugin.getDescriptor()).thenReturn(descriptor);
    return installedPlugin;
  }
}
