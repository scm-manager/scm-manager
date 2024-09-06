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

package sonia.scm.plugin;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.event.ScmEventBus;
import sonia.scm.net.ahc.AdvancedHttpClient;
import sonia.scm.net.ahc.AdvancedHttpRequest;
import sonia.scm.net.ahc.AdvancedHttpResponse;

import java.io.IOException;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PluginCenterLoaderTest {

  private static final String PLUGIN_URL = "https://plugins.hitchhiker.com";

  @Mock
  private AdvancedHttpClient client;

  @Mock
  private PluginCenterDtoMapper mapper;

  @Mock
  private ScmEventBus eventBus;

  @Mock
  private PluginCenterAuthenticator authenticator;

  @InjectMocks
  private PluginCenterLoader loader;

  @Mock(answer = Answers.RETURNS_SELF)
  private AdvancedHttpRequest request;

  @Test
  void shouldFetch() throws IOException {
    Set<AvailablePlugin> plugins = Collections.emptySet();
    Set<PluginSet> pluginSets = Collections.emptySet();
    PluginCenterDto dto = new PluginCenterDto();
    PluginCenterResult pluginCenterResult = new PluginCenterResult(plugins, pluginSets);
    when(request().contentFromJson(PluginCenterDto.class)).thenReturn(dto);
    when(mapper.map(dto)).thenReturn(pluginCenterResult);

    PluginCenterResult fetched = loader.load(PLUGIN_URL);
    assertThat(fetched.getPlugins()).isSameAs(plugins);
    assertThat(fetched.getPluginSets()).isSameAs(pluginSets);
    assertThat(fetched.getStatus()).isEqualTo(PluginCenterStatus.OK);
  }

  private AdvancedHttpResponse request() throws IOException {
    when(client.get(PLUGIN_URL)).thenReturn(request);
    AdvancedHttpResponse response = mock(AdvancedHttpResponse.class);
    when(request.request()).thenReturn(response);
    return response;
  }

  @Test
  void shouldReturnEmptySetIfPluginCenterIsDeactivated() {
    PluginCenterResult fetch = loader.load("");
    assertThat(fetch.getPlugins()).isEmpty();
    assertThat(fetch.getPluginSets()).isEmpty();
    assertThat(fetch.getStatus()).isSameAs(PluginCenterStatus.DEACTIVATED);
  }

  @Test
  void shouldReturnEmptySetIfPluginCenterNotBeReached() throws IOException {
    when(client.get(PLUGIN_URL)).thenReturn(request);
    when(request.request()).thenThrow(new IOException("failed to fetch"));

    PluginCenterResult fetch = loader.load(PLUGIN_URL);
    assertThat(fetch.getPlugins()).isEmpty();
    assertThat(fetch.getPluginSets()).isEmpty();
    assertThat(fetch.getStatus()).isSameAs(PluginCenterStatus.ERROR);
  }

  @Test
  void shouldFirePluginCenterErrorEvent() throws IOException {
    when(client.get(PLUGIN_URL)).thenReturn(request);
    when(request.request()).thenThrow(new IOException("failed to fetch"));

    loader.load(PLUGIN_URL);

    verify(eventBus).post(any(PluginCenterErrorEvent.class));
  }

  @Test
  void shouldAppendAccessToken() throws IOException {
    when(authenticator.isAuthenticated()).thenReturn(true);
    when(authenticator.fetchAccessToken()).thenReturn(Optional.of("mega-cool-at"));

    mockResponse();
    loader.load(PLUGIN_URL);

    verify(request).bearerAuth("mega-cool-at");
  }

  private Set<AvailablePlugin> mockResponse() throws IOException {
    PluginCenterDto dto = new PluginCenterDto();
    Set<AvailablePlugin> plugins = Collections.emptySet();
    Set<PluginSet> pluginSets = Collections.emptySet();
    when(request().contentFromJson(PluginCenterDto.class)).thenReturn(dto);
    when(mapper.map(dto)).thenReturn(new PluginCenterResult(plugins, pluginSets));
    return plugins;
  }

}
