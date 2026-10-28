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

import de.otto.edison.hal.Embedded;
import de.otto.edison.hal.Links;
import jakarta.ws.rs.core.MediaType;
import org.jboss.resteasy.mock.MockHttpResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.admin.ScmConfigurationStore;
import sonia.scm.api.v2.resources.Scm4xCompatabilityStartupResource.ResultDto;
import sonia.scm.api.v2.resources.Scm4xCompatabilityStartupResource.SimplePluginDto;
import sonia.scm.config.ScmConfiguration;
import sonia.scm.lifecycle.PrivilegedStartupAction;
import sonia.scm.lifecycle.Restarter;
import sonia.scm.lifecycle.Scm4xCompatibilityStartupAction;
import sonia.scm.lifecycle.Scm4xCompatibilityStartupAction.Result;
import sonia.scm.plugin.InstalledPlugin;
import sonia.scm.plugin.PluginInformation;
import sonia.scm.plugin.PluginManager;
import sonia.scm.plugin.PluginTestHelper;
import sonia.scm.web.JsonMockHttpRequest;
import sonia.scm.web.RestDispatcher;
import sonia.scm.web.security.AdministrationContext;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Set;

import static de.otto.edison.hal.Link.link;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static sonia.scm.lifecycle.Scm4xCompatibilityStartupAction.Status.NO_ACTION_NEEDED;
import static sonia.scm.lifecycle.Scm4xCompatibilityStartupAction.Status.PLUGIN_CENTER_DEACTIVATED;
import static sonia.scm.lifecycle.Scm4xCompatibilityStartupAction.Status.UPDATED;
import static sonia.scm.lifecycle.Scm4xCompatibilityStartupAction.Status.UPDATE_NOT_POSSIBLE;

@ExtendWith(MockitoExtension.class)
class Scm4xCompatabilityStartupResourceTest {

  @Mock
  private Embedded.Builder embeddedBuilder;

  @Mock
  private Scm4xCompatibilityStartupAction startupAction;
  @Mock
  private PluginManager pluginManager;
  @Mock
  private Restarter restarter;
  @Mock
  private ScmConfigurationStore scmConfigurationStore;
  @Mock
  private AdministrationContext adminContext;

  private Scm4xCompatabilityStartupResource startupResource;

  private RestDispatcher dispatcher;

  @BeforeEach
  void setup() {
    ScmPathInfoStore pathInfoStore = new ScmPathInfoStore();
    pathInfoStore.set(() -> URI.create("https://localhost:8081/scm/api/"));
    lenient().when(startupAction.name()).thenReturn("scm4xCompatibility");

    lenient().doAnswer(invocation -> {
      invocation.getArgument(0, PrivilegedStartupAction.class).run();
      return null;
    }).when(adminContext).runAsAdmin(any(PrivilegedStartupAction.class));

    startupResource = new Scm4xCompatabilityStartupResource(
      startupAction,
      pathInfoStore,
      scmConfigurationStore,
      pluginManager,
      restarter,
      adminContext
    );

    dispatcher = new RestDispatcher();
    dispatcher.addSingletonResource(new InitializationResource(Set.of(startupResource)));
  }

  @Nested
  class UninstallConflictingPluginsTests {

    @Test
    void shouldThrowUnauthenticated() throws URISyntaxException {
      when(startupAction.getToken()).thenReturn("token");

      JsonMockHttpRequest request = JsonMockHttpRequest.post("/v2/initialization/scm4xCompatibility/uninstall")
        .contentType(MediaType.APPLICATION_JSON)
        .json("{'token': 'different'}");
      MockHttpResponse response = new MockHttpResponse();
      dispatcher.invoke(request, response);

      assertThat(response.getStatus()).isEqualTo(403);
      verifyNoInteractions(pluginManager);
      verifyNoInteractions(restarter);
    }

    @Test
    void shouldUninstallConflictingPluginsAndRestartServer() throws URISyntaxException {
      when(startupAction.getToken()).thenReturn("expected");
      InstalledPlugin conflictingPlugin = PluginTestHelper.createInstalled("conflicting");
      InstalledPlugin dependentPlugin = PluginTestHelper.createInstalled("dependent");
      when(startupAction.getCompatibilityResult()).thenReturn(new Result(UPDATE_NOT_POSSIBLE, List.of(conflictingPlugin, dependentPlugin)));
      when(restarter.isSupported()).thenReturn(true);

      JsonMockHttpRequest request = JsonMockHttpRequest.post("/v2/initialization/scm4xCompatibility/uninstall")
        .contentType(MediaType.APPLICATION_JSON)
        .json("{'token': 'expected'}");
      MockHttpResponse response = new MockHttpResponse();
      dispatcher.invoke(request, response);

      assertThat(response.getStatus()).isEqualTo(204);
      verify(pluginManager).uninstall(List.of("conflicting", "dependent"), false);
      verify(restarter).restart(Scm4xCompatabilityStartupResource.class, "Finish removal of SCM 4.x incompatible plugins");
    }

    @Test
    void shouldNotTryUninstallingPluginThatIsAlreadyMarkedAsUninstalled() throws URISyntaxException {
      when(startupAction.getToken()).thenReturn("expected");
      InstalledPlugin conflictingPlugin = PluginTestHelper.createInstalled("conflicting");
      when(startupAction.getCompatibilityResult()).thenReturn(new Result(UPDATE_NOT_POSSIBLE, List.of(conflictingPlugin)));
      when(restarter.isSupported()).thenReturn(true);
      when(conflictingPlugin.isMarkedForUninstall()).thenReturn(true);

      JsonMockHttpRequest request = JsonMockHttpRequest.post("/v2/initialization/scm4xCompatibility/uninstall")
        .contentType(MediaType.APPLICATION_JSON)
        .json("{'token': 'expected'}");
      MockHttpResponse response = new MockHttpResponse();
      dispatcher.invoke(request, response);

      assertThat(response.getStatus()).isEqualTo(204);
      verify(pluginManager).uninstall(List.of(), false);
      verify(restarter).restart(Scm4xCompatabilityStartupResource.class, "Finish removal of SCM 4.x incompatible plugins");
    }

    @Test
    void shouldNotUninstallConflictingPluginsAndRestartServerBecauseOtherActionIsRequired() throws URISyntaxException {
      when(startupAction.getToken()).thenReturn("expected");
      when(startupAction.getCompatibilityResult()).thenReturn(new Result(NO_ACTION_NEEDED, List.of()));

      JsonMockHttpRequest request = JsonMockHttpRequest.post("/v2/initialization/scm4xCompatibility/uninstall")
        .contentType(MediaType.APPLICATION_JSON)
        .json("{'token': 'expected'}");
      MockHttpResponse response = new MockHttpResponse();
      dispatcher.invoke(request, response);

      assertThat(response.getStatus()).isEqualTo(400);
      verifyNoInteractions(pluginManager);
      verifyNoInteractions(restarter);
    }

    @Test
    void shouldUninstallConflictingPluginsButNotRestartServerBecauseRestartIsNotSupported() throws URISyntaxException {
      when(startupAction.getToken()).thenReturn("expected");
      InstalledPlugin conflictingPlugin = PluginTestHelper.createInstalled("conflicting");
      when(startupAction.getCompatibilityResult()).thenReturn(new Result(UPDATE_NOT_POSSIBLE, List.of(conflictingPlugin)));
      when(restarter.isSupported()).thenReturn(false);

      JsonMockHttpRequest request = JsonMockHttpRequest.post("/v2/initialization/scm4xCompatibility/uninstall")
        .contentType(MediaType.APPLICATION_JSON)
        .json("{'token': 'expected'}");
      MockHttpResponse response = new MockHttpResponse();
      dispatcher.invoke(request, response);

      assertThat(response.getStatus()).isEqualTo(400);
      verify(pluginManager).uninstall(List.of("conflicting"), false);
      verify(restarter, never()).restart(any(), any());
    }
  }

  @Nested
  class UpdatePluginUrlTests {

    @Test
    void shouldThrowUnauthenticated() throws URISyntaxException {
      when(startupAction.getToken()).thenReturn("token");

      JsonMockHttpRequest request = JsonMockHttpRequest.post("/v2/initialization/scm4xCompatibility/plugin-center")
        .contentType(MediaType.APPLICATION_JSON)
        .json("{'pluginCenterUrl': 'https://plugin-center-api.scm-manager.org/api/v1/{version}', 'token': 'wrong'}");
      MockHttpResponse response = new MockHttpResponse();
      dispatcher.invoke(request, response);

      assertThat(response.getStatus()).isEqualTo(403);
      verifyNoInteractions(scmConfigurationStore);
      verifyNoInteractions(restarter);
    }

    @Test
    void shouldUpdatePluginUrlAndResetCompatibilityResult() throws URISyntaxException {
      ScmConfiguration configuration = mock(ScmConfiguration.class);

      when(startupAction.getToken()).thenReturn("expected");
      when(startupAction.getCompatibilityResult()).thenReturn(new Result(PLUGIN_CENTER_DEACTIVATED, List.of()));
      when(scmConfigurationStore.get()).thenReturn(configuration);
      when(restarter.isSupported()).thenReturn(true);

      JsonMockHttpRequest request = JsonMockHttpRequest.post("/v2/initialization/scm4xCompatibility/plugin-center")
        .contentType(MediaType.APPLICATION_JSON)
        .json("{'pluginCenterUrl': 'https://plugin-center-api.scm-manager.org/api/v1/{version}', 'token': 'expected'}");
      MockHttpResponse response = new MockHttpResponse();
      dispatcher.invoke(request, response);

      assertThat(response.getStatus()).isEqualTo(204);
      verify(configuration).setPluginUrl("https://plugin-center-api.scm-manager.org/api/v1/{version}");
      verify(scmConfigurationStore).store(configuration);
      verify(restarter).restart(Scm4xCompatabilityStartupResource.class, "Retrigger SCM 4.x compatibility check after setting plugin center url");
    }

    @Test
    void shouldUpdatePluginUrlButCannotRestartServer() throws URISyntaxException {
      ScmConfiguration configuration = mock(ScmConfiguration.class);

      when(startupAction.getToken()).thenReturn("expected");
      when(startupAction.getCompatibilityResult()).thenReturn(new Result(PLUGIN_CENTER_DEACTIVATED, List.of()));
      when(scmConfigurationStore.get()).thenReturn(configuration);
      when(restarter.isSupported()).thenReturn(false);

      JsonMockHttpRequest request = JsonMockHttpRequest.post("/v2/initialization/scm4xCompatibility/plugin-center")
        .contentType(MediaType.APPLICATION_JSON)
        .json("{'pluginCenterUrl': 'https://plugin-center-api.scm-manager.org/api/v1/{version}', 'token': 'expected'}");
      MockHttpResponse response = new MockHttpResponse();
      dispatcher.invoke(request, response);

      assertThat(response.getStatus()).isEqualTo(400);
      verify(configuration).setPluginUrl("https://plugin-center-api.scm-manager.org/api/v1/{version}");
      verify(scmConfigurationStore).store(configuration);
      verify(restarter, never()).restart(any(), any());
    }

    @Test
    void shouldNotUpdatePluginUrlAndRestartServerBecauseOtherActionIsRequired() throws URISyntaxException {
      when(startupAction.getToken()).thenReturn("expected");
      when(startupAction.getCompatibilityResult()).thenReturn(new Result(NO_ACTION_NEEDED, List.of()));

      JsonMockHttpRequest request = JsonMockHttpRequest.post("/v2/initialization/scm4xCompatibility/plugin-center")
        .contentType(MediaType.APPLICATION_JSON)
        .json("{'pluginCenterUrl': 'https://plugin-center-api.scm-manager.org/api/v1/{version}', 'token': 'expected'}");
      MockHttpResponse response = new MockHttpResponse();
      dispatcher.invoke(request, response);

      assertThat(response.getStatus()).isEqualTo(400);
      verifyNoInteractions(scmConfigurationStore);
      verifyNoInteractions(restarter);
    }

    @ParameterizedTest
    @ValueSource(strings = {
      "{'pluginCenterUrl': null, 'token': 'expected'}",
      "{'pluginCenterUrl': undefined, 'token': 'expected'}",
      "{'pluginCenterUrl': '', 'token': 'expected'}",
      "{'pluginCenterUrl': '\t', 'token': 'expected'}",
      "{}",
      "{'pluginCenterUrl': 'test.de', 'token': 'expected'}",
      "{'pluginCenterUrl': 'https://url.de', 'token': ''}",
      "{'pluginCenterUrl': 'https://url.de', 'token': null}",
      "{'pluginCenterUrl': 'https://url.de', 'token': undefined}",
    })
    void shouldReturnBadRequest(String requestBody) throws URISyntaxException {
      JsonMockHttpRequest request = JsonMockHttpRequest.post("/v2/initialization/scm4xCompatibility/plugin-center")
        .contentType(MediaType.APPLICATION_JSON)
        .json(requestBody);
      MockHttpResponse response = new MockHttpResponse();
      dispatcher.invoke(request, response);

      assertThat(response.getStatus()).isEqualTo(400);
      verifyNoInteractions(scmConfigurationStore);
      verifyNoInteractions(restarter);
    }
  }

  @Nested
  class SetupIndexTests {

    private void mockConflictingPlugin() {
      PluginInformation conflictingPluginInformation = new PluginInformation();
      conflictingPluginInformation.setVersion("3.0.0");
      conflictingPluginInformation.setDisplayName("conflicting");
      InstalledPlugin conflictingPlugin = PluginTestHelper.createInstalled(conflictingPluginInformation);

      when(startupAction.getCompatibilityResult()).thenReturn(new Result(UPDATE_NOT_POSSIBLE, List.of(conflictingPlugin)));
    }

    @BeforeEach
    void setup() {
      ScmConfiguration scmConfiguration = new ScmConfiguration();
      lenient().when(scmConfigurationStore.get()).thenReturn(scmConfiguration);
    }

    @Test
    void appendNoEmbeddedBecauseNoActionIsNeeded() {
      when(startupAction.getCompatibilityResult()).thenReturn(new Result(NO_ACTION_NEEDED, List.of()));
      startupResource.setupIndex(null, embeddedBuilder);
      verifyNoInteractions(embeddedBuilder);
    }

    @Test
    void appendEmbeddedWithUpdatedStatus() {
      when(startupAction.getCompatibilityResult()).thenReturn(new Result(UPDATED, List.of()));

      Links.Builder links = new Links.Builder();
      ResultDto resultDto = new ResultDto(UPDATED, List.of(), ScmConfiguration.DEFAULT_PLUGIN_URL);
      resultDto.add(links.build());

      startupResource.setupIndex(null, embeddedBuilder);

      verify(embeddedBuilder).with("scm4xCompatibility", resultDto);
    }

    @Test
    void appendEmbeddedForUpdateNotPossibleStatus() {
      mockConflictingPlugin();

      ResultDto resultDto = new ResultDto(
        UPDATE_NOT_POSSIBLE,
        List.of(new SimplePluginDto("conflicting", "3.0.0")),
        ScmConfiguration.DEFAULT_PLUGIN_URL
      );
      Links.Builder linkBuilder = new Links.Builder();
      linkBuilder.single(link(
        "uninstall",
        "https://localhost:8081/scm/api/v2/initialization/scm4xCompatibility/uninstall"
      ));
      linkBuilder.single(link(
        "update-plugin-center",
        "https://localhost:8081/scm/api/v2/initialization/scm4xCompatibility/plugin-center"
      ));
      resultDto.add(linkBuilder.build());

      startupResource.setupIndex(null, embeddedBuilder);

      verify(embeddedBuilder).with("scm4xCompatibility", resultDto);
    }

    @Test
    void appendEmbeddedWithPluginCenterDeactivatedStatus() {
      when(startupAction.getCompatibilityResult()).thenReturn(new Result(PLUGIN_CENTER_DEACTIVATED, List.of()));

      ResultDto resultDto = new ResultDto(PLUGIN_CENTER_DEACTIVATED, List.of(), ScmConfiguration.DEFAULT_PLUGIN_URL);
      Links.Builder linkBuilder = new Links.Builder();
      linkBuilder.single(link(
        "update-plugin-center",
        "https://localhost:8081/scm/api/v2/initialization/scm4xCompatibility/plugin-center"
      ));
      resultDto.add(linkBuilder.build());

      startupResource.setupIndex(null, embeddedBuilder);

      verify(embeddedBuilder).with("scm4xCompatibility", resultDto);
    }
  }
}
