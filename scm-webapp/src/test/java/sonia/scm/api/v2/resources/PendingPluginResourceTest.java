package sonia.scm.api.v2.resources;

import com.google.inject.util.Providers;
import org.jboss.resteasy.core.Dispatcher;
import org.jboss.resteasy.mock.MockDispatcherFactory;
import org.jboss.resteasy.mock.MockHttpRequest;
import org.jboss.resteasy.mock.MockHttpResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.plugin.AvailablePlugin;
import sonia.scm.plugin.AvailablePluginDescriptor;
import sonia.scm.plugin.InstalledPlugin;
import sonia.scm.plugin.InstalledPluginDescriptor;
import sonia.scm.plugin.PluginInformation;
import sonia.scm.plugin.PluginManager;

import javax.servlet.http.HttpServletResponse;
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

  Dispatcher dispatcher = MockDispatcherFactory.createDispatcher();

  ResourceLinks resourceLinks = ResourceLinksMock.createMock(create("/"));

  @Mock
  PluginManager pluginManager;
  @Mock
  PluginDtoMapper mapper;

  @InjectMocks
  PendingPluginResource pendingPluginResource;

  MockHttpResponse response = new MockHttpResponse();

  @BeforeEach
  void prepareEnvironment() {
    dispatcher = MockDispatcherFactory.createDispatcher();
    PluginRootResource pluginRootResource = new PluginRootResource(null, null, Providers.of(pendingPluginResource));
    dispatcher.getRegistry().addSingletonResource(pluginRootResource);
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

  @Test
  void shouldGetEmptyPluginListsWithoutInstallLinkWhenNoPendingPluginsPresent() throws URISyntaxException, UnsupportedEncodingException {
    AvailablePlugin availablePlugin = createAvailablePlugin("not-pending-plugin");
    when(availablePlugin.isPending()).thenReturn(false);
    when(pluginManager.getAvailable()).thenReturn(singletonList(availablePlugin));

    MockHttpRequest request = MockHttpRequest.get("/v2/plugins/pending");
    dispatcher.invoke(request, response);

    assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_OK);
    assertThat(response.getContentAsString()).contains("\"_links\":{\"self\":{\"href\":\"/v2/plugins/pending\"}}");
    assertThat(response.getContentAsString()).doesNotContain("not-pending-plugin");
  }

  @Test
  void shouldGetPendingAvailablePluginListWithInstallLink() throws URISyntaxException, UnsupportedEncodingException {
    AvailablePlugin availablePlugin = createAvailablePlugin("pending-available-plugin");
    when(availablePlugin.isPending()).thenReturn(true);
    when(pluginManager.getAvailable()).thenReturn(singletonList(availablePlugin));

    MockHttpRequest request = MockHttpRequest.get("/v2/plugins/pending");
    dispatcher.invoke(request, response);

    assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_OK);
    assertThat(response.getContentAsString()).contains("\"new\":[{\"name\":\"pending-available-plugin\"");
    assertThat(response.getContentAsString()).contains("\"execute\":{\"href\":\"/v2/plugins/pending/execute\"}");
  }

  @Test
  void shouldGetPendingUpdatePluginListWithInstallLink() throws URISyntaxException, UnsupportedEncodingException {
    AvailablePlugin availablePlugin = createAvailablePlugin("available-plugin");
    when(availablePlugin.isPending()).thenReturn(true);
    when(pluginManager.getAvailable()).thenReturn(singletonList(availablePlugin));
    InstalledPlugin installedPlugin = createInstalledPlugin("available-plugin");
    when(pluginManager.getInstalled()).thenReturn(singletonList(installedPlugin));

    MockHttpRequest request = MockHttpRequest.get("/v2/plugins/pending");
    dispatcher.invoke(request, response);

    assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_OK);
    assertThat(response.getContentAsString()).contains("\"update\":[{\"name\":\"available-plugin\"");
    assertThat(response.getContentAsString()).contains("\"execute\":{\"href\":\"/v2/plugins/pending/execute\"}");
  }

  @Test
  void shouldGetPendingUninstallPluginListWithInstallLink() throws URISyntaxException, UnsupportedEncodingException {
    when(pluginManager.getAvailable()).thenReturn(emptyList());
    InstalledPlugin installedPlugin = createInstalledPlugin("uninstalled-plugin");
    when(installedPlugin.isMarkedForUninstall()).thenReturn(true);
    when(pluginManager.getInstalled()).thenReturn(singletonList(installedPlugin));

    MockHttpRequest request = MockHttpRequest.get("/v2/plugins/pending");
    dispatcher.invoke(request, response);

    assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_OK);
    assertThat(response.getContentAsString()).contains("\"uninstall\":[{\"name\":\"uninstalled-plugin\"");
    assertThat(response.getContentAsString()).contains("\"execute\":{\"href\":\"/v2/plugins/pending/execute\"}");
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
