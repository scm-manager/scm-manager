package sonia.scm.api.v2.resources;

import de.otto.edison.hal.HalRepresentation;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.ThreadContext;
import org.jboss.resteasy.core.Dispatcher;
import org.jboss.resteasy.mock.MockDispatcherFactory;
import org.jboss.resteasy.mock.MockHttpRequest;
import org.jboss.resteasy.mock.MockHttpResponse;
import org.jboss.resteasy.spi.UnhandledException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.plugin.InstalledPlugin;
import sonia.scm.plugin.InstalledPluginDescriptor;
import sonia.scm.plugin.PluginInformation;
import sonia.scm.plugin.PluginManager;
import sonia.scm.web.VndMediaType;

import javax.inject.Provider;
import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static sonia.scm.plugin.PluginTestHelper.createInstalled;

@ExtendWith(MockitoExtension.class)
class InstalledPluginResourceTest {

  private Dispatcher dispatcher;

  @Mock
  Provider<InstalledPluginResource> installedPluginResourceProvider;

  @Mock
  Provider<AvailablePluginResource> availablePluginResourceProvider;

  @Mock
  private PluginDtoCollectionMapper collectionMapper;

  @Mock
  private PluginDtoMapper mapper;

  @Mock
  private PluginManager pluginManager;

  @InjectMocks
  InstalledPluginResource installedPluginResource;

  PluginRootResource pluginRootResource;

  private final Subject subject = mock(Subject.class);

  @BeforeEach
  void prepareEnvironment() {
    dispatcher = MockDispatcherFactory.createDispatcher();
    pluginRootResource = new PluginRootResource(installedPluginResourceProvider, availablePluginResourceProvider);
    when(installedPluginResourceProvider.get()).thenReturn(installedPluginResource);
    dispatcher.getRegistry().addSingletonResource(pluginRootResource);
  }

  @Nested
  class withAuthorization {

    @BeforeEach
    void bindSubject() {
      ThreadContext.bind(subject);
      when(subject.isPermitted(any(String.class))).thenReturn(true);
    }

    @AfterEach
    public void unbindSubject() {
      ThreadContext.unbindSubject();
    }

    @Test
    void getInstalledPlugins() throws URISyntaxException, UnsupportedEncodingException {
      InstalledPlugin installedPlugin = createInstalled("");
      when(pluginManager.getInstalled()).thenReturn(Collections.singletonList(installedPlugin));
      when(collectionMapper.mapInstalled(Collections.singletonList(installedPlugin))).thenReturn(new MockedResultDto());

      MockHttpRequest request = MockHttpRequest.get("/v2/plugins/installed");
      request.accept(VndMediaType.PLUGIN_COLLECTION);
      MockHttpResponse response = new MockHttpResponse();

      dispatcher.invoke(request, response);

      assertThat(HttpServletResponse.SC_OK).isEqualTo(response.getStatus());
      assertThat(response.getContentAsString()).contains("\"marker\":\"x\"");
    }

    @Test
    void getInstalledPlugin() throws UnsupportedEncodingException, URISyntaxException {
      PluginInformation pluginInformation = new PluginInformation();
      pluginInformation.setVersion("2.0.0");
      pluginInformation.setName("pluginName");
      InstalledPlugin installedPlugin = createInstalled(pluginInformation);

      when(pluginManager.getInstalled("pluginName")).thenReturn(Optional.of(installedPlugin));

      PluginDto pluginDto = new PluginDto();
      pluginDto.setName("pluginName");
      when(mapper.mapInstalled(installedPlugin)).thenReturn(pluginDto);

      MockHttpRequest request = MockHttpRequest.get("/v2/plugins/installed/pluginName");
      request.accept(VndMediaType.PLUGIN);
      MockHttpResponse response = new MockHttpResponse();

      dispatcher.invoke(request, response);

      assertThat(HttpServletResponse.SC_OK).isEqualTo(response.getStatus());
      assertThat(response.getContentAsString()).contains("\"name\":\"pluginName\"");
    }
  }

  @Nested
  class WithoutAuthorization {

    @BeforeEach
    void unbindSubject() {
      ThreadContext.unbindSubject();
    }

    @Test
    void shouldNotGetInstalledPluginsIfMissingPermission() throws URISyntaxException {
      MockHttpRequest request = MockHttpRequest.get("/v2/plugins/installed");
      request.accept(VndMediaType.PLUGIN_COLLECTION);
      MockHttpResponse response = new MockHttpResponse();

      assertThrows(UnhandledException.class, () -> dispatcher.invoke(request, response));
    }

    @Test
    void shouldNotGetInstalledPluginIfMissingPermission() throws URISyntaxException {
      MockHttpRequest request = MockHttpRequest.get("/v2/plugins/installed/pluginName");
      request.accept(VndMediaType.PLUGIN);
      MockHttpResponse response = new MockHttpResponse();

      assertThrows(UnhandledException.class, () -> dispatcher.invoke(request, response));
    }
  }

  public class MockedResultDto extends HalRepresentation {
    public String getMarker() {
      return "x";
    }
  }
}
