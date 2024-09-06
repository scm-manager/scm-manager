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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jboss.resteasy.mock.MockHttpRequest;
import org.jboss.resteasy.mock.MockHttpResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.SCMContextProvider;
import sonia.scm.api.v2.resources.AlertsResource.AlertsRequest;
import sonia.scm.api.v2.resources.AlertsResource.AlertsRequestBody;
import sonia.scm.config.ScmConfiguration;
import sonia.scm.plugin.InstalledPlugin;
import sonia.scm.plugin.InstalledPluginDescriptor;
import sonia.scm.plugin.PluginInformation;
import sonia.scm.plugin.PluginLoader;
import sonia.scm.util.SystemUtil;
import sonia.scm.web.RestDispatcher;
import sonia.scm.web.VndMediaType;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AlertsResourceTest {

  private final ObjectMapper mapper = new ObjectMapper();

  @Mock
  private PluginLoader pluginLoader;

  @Mock
  private SCMContextProvider scmContextProvider;

  private RestDispatcher restDispatcher;
  private ScmConfiguration scmConfiguration;

  @BeforeEach
  void setUp() {
    restDispatcher = new RestDispatcher();
    scmConfiguration = new ScmConfiguration();
  }

  @Test
  void shouldFailWithConflictIfAlertsUrlIsNull() throws Exception {
    scmConfiguration.setAlertsUrl(null);

    MockHttpResponse response = invoke();

    assertThat(response.getStatus()).isEqualTo(409);
  }

  @Test
  void shouldReturnSelfUrl() throws Exception {
    MockHttpResponse response = invoke();
    JsonNode node = mapper.readTree(response.getContentAsString());
    assertThat(node.get("_links").get("self").get("href").asText()).isEqualTo("/v2/alerts");
  }

  @Test
  void shouldReturnAlertsUrl() throws Exception {
    MockHttpResponse response = invoke();
    JsonNode node = mapper.readTree(response.getContentAsString());
    assertThat(node.get("_links").get("alerts").get("href").asText()).isEqualTo(ScmConfiguration.DEFAULT_ALERTS_URL);
  }

  @Test
  void shouldReturnVndMediaType() throws Exception {
    MockHttpResponse response = invoke();
    assertThat(response.getOutputHeaders().getFirst("Content-Type"))
      .asString()
      .isEqualToIgnoringCase(VndMediaType.ALERTS_REQUEST);
  }

  @Test
  void shouldReturnCustomAlertsUrl() throws Exception {
    scmConfiguration.setAlertsUrl("https://mycustom.alerts.io");

    MockHttpResponse response = invoke();
    JsonNode node = mapper.readTree(response.getContentAsString());

    assertThat(node.get("_links").get("alerts").get("href").asText()).isEqualTo("https://mycustom.alerts.io");
  }

  @Test
  void shouldReturnAlertsRequest() throws Exception {
    String instanceId = UUID.randomUUID().toString();
    when(scmContextProvider.getInstanceId()).thenReturn(instanceId);
    when(scmContextProvider.getVersion()).thenReturn("2.28.0");

    InstalledPlugin pluginA = createInstalledPlugin("some-scm-plugin", "1.0.0");
    InstalledPlugin pluginB = createInstalledPlugin("other-scm-plugin", "2.1.1");
    when(pluginLoader.getInstalledPlugins()).thenReturn(Arrays.asList(pluginA, pluginB));

    MockHttpResponse response = invoke();

    assertThat(response.getStatus()).isEqualTo(200);

    AlertsRequest alertsRequest = unmarshal(response);
    AlertsRequestBody body = alertsRequest.getBody();
    assertThat(body.getInstanceId()).isEqualTo(instanceId);
    assertThat(body.getVersion()).isEqualTo("2.28.0");
    assertThat(body.getOs()).isEqualTo(SystemUtil.getOS());
    assertThat(body.getArch()).isEqualTo(SystemUtil.getArch());
    assertThat(body.getJre()).isEqualTo(SystemUtil.getJre());

    List<AlertsResource.Plugin> plugins = body.getPlugins();
    assertThat(plugins.size()).isEqualTo(2);
    AlertsResource.Plugin somePlugin = findPlugin(plugins, "some-scm-plugin");
    assertThat(somePlugin.getVersion()).isEqualTo("1.0.0");
    AlertsResource.Plugin otherPlugin = findPlugin(plugins, "other-scm-plugin");
    assertThat(otherPlugin.getVersion()).isEqualTo("2.1.1");
  }

  @Test
  void shouldReturnSameChecksumIfNothingChanged() throws Exception {
    String instanceId = UUID.randomUUID().toString();
    when(scmContextProvider.getInstanceId()).thenReturn(instanceId);
    when(scmContextProvider.getVersion()).thenReturn("2.28.0");

    InstalledPlugin plugin = createInstalledPlugin("some-scm-plugin", "1.0.0");
    when(pluginLoader.getInstalledPlugins()).thenReturn(Collections.singletonList(plugin));

    MockHttpResponse response = invoke();
    String checksum = unmarshal(response).getChecksum();

    MockHttpResponse secondResponse = invoke();

    assertThat(unmarshal(secondResponse).getChecksum()).isEqualTo(checksum);
  }

  @Test
  void shouldReturnDifferentChecksumIfCoreVersionChanges() throws Exception {
    when(scmContextProvider.getVersion()).thenReturn("2.28.0");

    MockHttpResponse response = invoke();
    String checksum = unmarshal(response).getChecksum();

    when(scmContextProvider.getVersion()).thenReturn("2.28.1");

    MockHttpResponse secondResponse = invoke();

    assertThat(unmarshal(secondResponse).getChecksum()).isNotEqualTo(checksum);
  }

  @Test
  void shouldReturnDifferentChecksumIfPluginVersionChanges() throws Exception {
    InstalledPlugin plugin1_0_0 = createInstalledPlugin("some-scm-plugin", "1.0.0");
    when(pluginLoader.getInstalledPlugins()).thenReturn(Collections.singletonList(plugin1_0_0));

    MockHttpResponse response = invoke();
    String checksum = unmarshal(response).getChecksum();

    InstalledPlugin plugin1_0_1 = createInstalledPlugin("some-scm-plugin", "1.0.1");
    when(pluginLoader.getInstalledPlugins()).thenReturn(Collections.singletonList(plugin1_0_1));

    MockHttpResponse secondResponse = invoke();

    assertThat(unmarshal(secondResponse).getChecksum()).isNotEqualTo(checksum);
  }

  @Test
  void shouldReturnDifferentChecksumIfDateChanges() throws Exception {
    MockHttpResponse response = invoke();
    String checksum = unmarshal(response).getChecksum();

    InstalledPlugin plugin1_0_1 = createInstalledPlugin("some-scm-plugin", "1.0.1");
    when(pluginLoader.getInstalledPlugins()).thenReturn(Collections.singletonList(plugin1_0_1));

    MockHttpResponse secondResponse = invoke("1979-10-12");

    assertThat(unmarshal(secondResponse).getChecksum()).isNotEqualTo(checksum);
  }

  private InstalledPlugin createInstalledPlugin(String name, String version) {
    PluginInformation pluginInformation = new PluginInformation();
    pluginInformation.setName(name);
    pluginInformation.setVersion(version);
    return createInstalledPlugin(pluginInformation);
  }

  private InstalledPlugin createInstalledPlugin(PluginInformation pluginInformation) {
    InstalledPluginDescriptor descriptor = mock(InstalledPluginDescriptor.class);
    lenient().when(descriptor.getInformation()).thenReturn(pluginInformation);
    return new InstalledPlugin(descriptor, null, null, null, false);
  }

  private MockHttpResponse invoke() throws Exception {
    return invoke(null);
  }

  private MockHttpResponse invoke(String date) throws Exception {
    AlertsResource alertsResource;
    if (date != null) {
      alertsResource = new AlertsResource(scmContextProvider, scmConfiguration, pluginLoader, () -> date);
    } else {
      alertsResource = new AlertsResource(scmContextProvider, scmConfiguration, pluginLoader);
    }

    restDispatcher.addSingletonResource(alertsResource);

    MockHttpRequest request = MockHttpRequest.get("/v2/alerts");
    MockHttpResponse response = new MockHttpResponse();
    restDispatcher.invoke(request, response);

    return response;
  }

  private AlertsRequest unmarshal(MockHttpResponse response) throws JsonProcessingException, UnsupportedEncodingException {
    return mapper.readValue(response.getContentAsString(), AlertsRequest.class);
  }

  private AlertsResource.Plugin findPlugin(List<AlertsResource.Plugin> plugins, String name) {
    return plugins.stream()
      .filter(p -> name.equals(p.getName()))
      .findFirst()
      .orElseThrow(() -> new IllegalStateException("plugin " + name + " not found in request"));
  }

}
