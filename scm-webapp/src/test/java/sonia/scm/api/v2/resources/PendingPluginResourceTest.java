package sonia.scm.api.v2.resources;

import com.google.inject.util.Providers;
import org.apache.shiro.ShiroException;
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
import sonia.scm.plugin.AvailablePlugin;
import sonia.scm.plugin.AvailablePluginDescriptor;
import sonia.scm.plugin.PluginCondition;
import sonia.scm.plugin.PluginInformation;
import sonia.scm.plugin.PluginManager;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.util.Collections;

import static java.net.URI.create;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
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

  @Mock
  Subject subject;

  @InjectMocks
  PendingPluginResource pendingPluginResource;

  MockHttpResponse response = new MockHttpResponse();

  @BeforeEach
  void prepareEnvironment() {
    dispatcher = MockDispatcherFactory.createDispatcher();
    dispatcher.getProviderFactory().register(new PermissionExceptionMapper());
    PluginRootResource pluginRootResource = new PluginRootResource(null, null, Providers.of(pendingPluginResource));
    dispatcher.getRegistry().addSingletonResource(pluginRootResource);
  }

  @BeforeEach
  void mockMapper() {
    lenient().when(mapper.mapAvailable(any())).thenAnswer(invocation -> {
      PluginDto dto = new PluginDto();
      dto.setName(((AvailablePlugin)invocation.getArgument(0)).getDescriptor().getInformation().getName());
      return dto;
    });
  }

  @Nested
  class withAuthorization {

    @BeforeEach
    void bindSubject() {
      ThreadContext.bind(subject);
      doNothing().when(subject).checkPermission("plugin:manage");
    }

    @AfterEach
    void unbindSubject() {
      ThreadContext.unbindSubject();
    }

    @Test
    void shouldGetEmptyPluginListsWithoutInstallLinkWhenNoPendingPluginsPresent() throws URISyntaxException, UnsupportedEncodingException {
      AvailablePlugin availablePlugin = createAvailablePlugin("not-available-plugin");
      when(availablePlugin.isPending()).thenReturn(false);
      when(pluginManager.getAvailable()).thenReturn(singletonList(availablePlugin));

      MockHttpRequest request = MockHttpRequest.get("/v2/plugins/pending");
      dispatcher.invoke(request, response);

      assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_OK);
      assertThat(response.getContentAsString()).contains("\"_links\":{\"self\":{\"href\":\"/v2/plugins/pending\"}}");
      assertThat(response.getContentAsString()).doesNotContain("not-available-plugin");
    }

    @Test
    void shouldGetPendingAvailablePluginListsWithInstallLink() throws URISyntaxException, UnsupportedEncodingException {
      AvailablePlugin availablePlugin = createAvailablePlugin("available-plugin");
      when(availablePlugin.isPending()).thenReturn(true);
      when(pluginManager.getAvailable()).thenReturn(singletonList(availablePlugin));

      MockHttpRequest request = MockHttpRequest.get("/v2/plugins/pending");
      dispatcher.invoke(request, response);

      assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_OK);
      assertThat(response.getContentAsString()).contains("\"_embedded\":{\"available\":[{\"name\":\"available-plugin\"");
      assertThat(response.getContentAsString()).contains("\"install\":{\"href\":\"/v2/plugins/pending/install\"}");
      System.out.println(response.getContentAsString());
    }

    @Test
    void shouldInstallPendingPlugins() throws URISyntaxException {
      MockHttpRequest request = MockHttpRequest.post("/v2/plugins/pending/install");

      dispatcher.invoke(request, response);

      assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_OK);
      verify(pluginManager).installPendingAndRestart();
    }
  }

  @Nested
  class WithoutAuthorization {

    @BeforeEach
    void bindSubject() {
      ThreadContext.bind(subject);
      doThrow(new ShiroException()).when(subject).checkPermission("plugin:manage");
    }

    @AfterEach
    void unbindSubject() {
      ThreadContext.unbindSubject();
    }

    @Test
    void shouldNotListPendingPlugins() throws URISyntaxException {
      MockHttpRequest request = MockHttpRequest.get("/v2/plugins/pending");

      dispatcher.invoke(request, response);

      assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_UNAUTHORIZED);
      verify(pluginManager, never()).installPendingAndRestart();
    }

    @Test
    void shouldNotInstallPendingPlugins() throws URISyntaxException {
      MockHttpRequest request = MockHttpRequest.post("/v2/plugins/pending/install");

      dispatcher.invoke(request, response);

      assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_UNAUTHORIZED);
      verify(pluginManager, never()).installPendingAndRestart();
    }
  }

  static class PermissionExceptionMapper implements ExceptionMapper<ShiroException> {

    @Override
    public Response toResponse(ShiroException exception) {
      return Response.status(401).entity(exception.getMessage()).build();
    }
  }

  private AvailablePlugin createAvailablePlugin(String name) {
    PluginInformation pluginInformation = new PluginInformation();
    pluginInformation.setName(name);
    return createAvailablePlugin(pluginInformation);
  }

  private AvailablePlugin createAvailablePlugin(PluginInformation pluginInformation) {
    AvailablePluginDescriptor descriptor = new AvailablePluginDescriptor(
      pluginInformation, new PluginCondition(), Collections.emptySet(), "https://download.hitchhiker.com", null
    );
    AvailablePlugin availablePlugin = mock(AvailablePlugin.class);
    lenient().when(availablePlugin.getDescriptor()).thenReturn(descriptor);
    return availablePlugin;
  }
}
