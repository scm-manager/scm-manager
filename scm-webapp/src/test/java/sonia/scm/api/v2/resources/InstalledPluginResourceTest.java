package sonia.scm.api.v2.resources;

import com.google.common.io.Resources;
import de.otto.edison.hal.HalRepresentation;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.ThreadContext;
import org.jboss.resteasy.core.Dispatcher;
import org.jboss.resteasy.mock.MockDispatcherFactory;
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
import sonia.scm.plugin.Plugin;
import sonia.scm.plugin.PluginInformation;
import sonia.scm.plugin.PluginLoader;
import sonia.scm.plugin.PluginManager;
import sonia.scm.plugin.PluginState;
import sonia.scm.plugin.PluginWrapper;
import sonia.scm.web.VndMediaType;

import javax.inject.Provider;
import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InstalledPluginResourceTest {

  private Dispatcher dispatcher;
  private URL resources = Resources.getResource("sonia/scm/api/v2/installedPlugins-001.json");

  @Mock
  Provider<InstalledPluginResource> installedPluginResourceProvider;

  @Mock
  Provider<AvailablePluginResource> availablePluginResourceProvider;

  @Mock
  private PluginManager pluginManager;

  @Mock
  private PluginLoader pluginLoader;

  @Mock
  private PluginDtoCollectionMapper collectionMapper;

  @Mock
  private PluginDtoMapper mapper;

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
      PluginInformation pluginInformation = new PluginInformation();
      pluginInformation.setVersion("2.0.0");
      pluginInformation.setName("plugin-name");
      pluginInformation.setState(PluginState.INSTALLED);
      Plugin plugin = new Plugin(2, pluginInformation, null, null, false, null);
      PluginWrapper pluginWrapper = new PluginWrapper(plugin, null, null, null);
      when(pluginLoader.getInstalledPlugins()).thenReturn(Collections.singletonList(pluginWrapper));

      PluginDto pluginDto = new PluginDto();
      pluginDto.setName("plugin-name");
      pluginDto.setVersion("2.0.0");
      when(collectionMapper.map(Collections.singletonList(pluginWrapper))).thenReturn(new MockedResultDto());

      MockHttpRequest request = MockHttpRequest.get("/v2/plugins/installed");
      request.accept(VndMediaType.PLUGIN_COLLECTION);
      MockHttpResponse response = new MockHttpResponse();

      dispatcher.invoke(request, response);

      assertThat(HttpServletResponse.SC_OK).isEqualTo(response.getStatus());
      assertThat(response.getContentAsString()).contains("\"marker\":\"x\"");
    }

    @Test
    void getInstalledPlugin() {
    }

    public class MockedResultDto extends HalRepresentation {
      public String getMarker() {
        return "x";
      }
    }
  }
}
