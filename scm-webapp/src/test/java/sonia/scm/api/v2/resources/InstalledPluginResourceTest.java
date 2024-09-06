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

import jakarta.inject.Provider;
import jakarta.servlet.http.HttpServletResponse;
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
import sonia.scm.plugin.InstalledPlugin;
import sonia.scm.plugin.PluginInformation;
import sonia.scm.plugin.PluginManager;
import sonia.scm.web.RestDispatcher;
import sonia.scm.web.VndMediaType;

import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.Optional;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static sonia.scm.plugin.PluginTestHelper.createInstalled;

@ExtendWith(MockitoExtension.class)
class InstalledPluginResourceTest {

  private RestDispatcher dispatcher = new RestDispatcher();

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
    pluginRootResource = new PluginRootResource(installedPluginResourceProvider, null, null, null);
    when(installedPluginResourceProvider.get()).thenReturn(installedPluginResource);
    dispatcher.addSingletonResource(pluginRootResource);
  }

  @Nested
  class withAuthorization {

    @BeforeEach
    void bindSubject() {
      ThreadContext.bind(subject);
    }

    @AfterEach
    public void unbindSubject() {
      ThreadContext.unbindSubject();
    }

    @Test
    void getInstalledPlugins() throws URISyntaxException, UnsupportedEncodingException {
      InstalledPlugin installedPlugin = createInstalled("");
      PluginManager.PluginResult pluginResult = new PluginManager.PluginResult(Collections.singletonList(installedPlugin), emptyList());
      when(pluginManager.getPlugins()).thenReturn(pluginResult);
      when(collectionMapper.mapInstalled(pluginResult)).thenReturn(new MockedResultDto());

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
      when(mapper.mapInstalled(installedPlugin, emptyList())).thenReturn(pluginDto);

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
    void bindSubject() {
      ThreadContext.bind(subject);
      doThrow(new UnauthorizedException()).when(subject).checkPermission(any(String.class));
    }

    @AfterEach
    public void unbindSubject() {
      ThreadContext.unbindSubject();
    }

    @Test
    void shouldNotGetInstalledPluginsIfMissingPermission() throws URISyntaxException {
      MockHttpRequest request = MockHttpRequest.get("/v2/plugins/installed");
      request.accept(VndMediaType.PLUGIN_COLLECTION);
      MockHttpResponse response = new MockHttpResponse();

      dispatcher.invoke(request, response);

      assertEquals(HttpServletResponse.SC_FORBIDDEN, response.getStatus());
    }

    @Test
    void shouldNotGetInstalledPluginIfMissingPermission() throws URISyntaxException {
      MockHttpRequest request = MockHttpRequest.get("/v2/plugins/installed/pluginName");
      request.accept(VndMediaType.PLUGIN);
      MockHttpResponse response = new MockHttpResponse();

      dispatcher.invoke(request, response);

      assertEquals(HttpServletResponse.SC_FORBIDDEN, response.getStatus());
    }
  }

  public class MockedResultDto extends PluginCollectionDto {

    public String getMarker() {
      return "x";
    }
  }
}
