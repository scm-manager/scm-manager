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
  }

  private AdvancedHttpResponse request() throws IOException {
    when(client.get(PLUGIN_URL)).thenReturn(request);
    AdvancedHttpResponse response = mock(AdvancedHttpResponse.class);
    when(request.request()).thenReturn(response);
    return response;
  }

  @Test
  void shouldReturnEmptySetIfPluginCenterNotBeReached() throws IOException {
    when(client.get(PLUGIN_URL)).thenReturn(request);
    when(request.request()).thenThrow(new IOException("failed to fetch"));

    PluginCenterResult fetch = loader.load(PLUGIN_URL);
    assertThat(fetch.getPlugins()).isEmpty();
    assertThat(fetch.getPluginSets()).isEmpty();
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
