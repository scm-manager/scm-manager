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

import com.github.sdorra.shiro.SubjectAware;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import de.otto.edison.hal.Embedded;
import de.otto.edison.hal.Links;
import org.apache.commons.lang.StringUtils;
import org.jboss.resteasy.mock.MockHttpRequest;
import org.jboss.resteasy.mock.MockHttpResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.lifecycle.PluginWizardStartupAction;
import sonia.scm.lifecycle.PrivilegedStartupAction;
import sonia.scm.plugin.AvailablePlugin;
import sonia.scm.plugin.PluginManager;
import sonia.scm.plugin.PluginSet;
import sonia.scm.security.AccessTokenCookieIssuer;
import sonia.scm.web.RestDispatcher;
import sonia.scm.web.security.AdministrationContext;

import java.net.URISyntaxException;
import java.util.List;
import java.util.Locale;

import static java.lang.String.format;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Collections.emptyList;
import static java.util.Collections.singleton;
import static org.assertj.core.api.Assertions.assertThat;
import static org.jboss.resteasy.mock.MockHttpRequest.post;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static sonia.scm.plugin.PluginTestHelper.createAvailable;

@SubjectAware(
  configuration = "classpath:sonia/scm/repository/shiro.ini"
)
@ExtendWith(MockitoExtension.class)
class PluginWizardStartupResourceTest {

  @Mock
  private PluginWizardStartupAction startupAction;

  @Mock(answer = Answers.RETURNS_DEEP_STUBS)
  private ResourceLinks resourceLinks;

  @Mock
  private PluginManager pluginManager;

  @Mock
  private AccessTokenCookieIssuer cookieIssuer;

  @Mock
  private PluginSetDtoMapper pluginSetDtoMapper;

  @Mock
  private AdministrationContext context;

  private final RestDispatcher dispatcher = new RestDispatcher();
  private final MockHttpResponse response = new MockHttpResponse();

  @InjectMocks
  private PluginWizardStartupResource resource;

  @BeforeEach
  void setUpMocks() {
    dispatcher.addSingletonResource(new InitializationResource(singleton(resource)));
    lenient().when(startupAction.name()).thenReturn("pluginWizard");
  }

  @Test
  void shouldFailWhenActionIsDone() throws URISyntaxException {
    when(startupAction.done()).thenReturn(true);

    MockHttpRequest request =
      post("/v2/initialization/pluginWizard")
        .contentType("application/json")
        .content(createInput("my-plugin-set"));
    dispatcher.invoke(request, response);

    assertThat(response.getStatus()).isEqualTo(400);
  }

  @Test
  void shouldInstallPluginSets() throws URISyntaxException {
    when(startupAction.done()).thenReturn(false);

    MockHttpRequest request =
      post("/v2/initialization/pluginWizard")
        .contentType("application/json")
        .content(createInput("my-plugin-set", "my-other-plugin-set"));
    dispatcher.invoke(request, response);

    verify(cookieIssuer).invalidate(any(), any());
    verify(pluginManager).installPluginSets(ImmutableSet.of("my-plugin-set", "my-other-plugin-set"), true);
  }

  @Test
  void shouldSetupIndex() {
    AvailablePlugin git = createAvailable("scm-git-plugin");
    AvailablePlugin svn = createAvailable("scm-svn-plugin");
    AvailablePlugin hg = createAvailable("scm-hg-plugin");
    List<AvailablePlugin> availablePlugins = List.of(git, svn, hg);

    when(pluginManager.getAvailable()).thenReturn(availablePlugins);

    doAnswer(invocation -> {
      invocation.getArgument(0, PrivilegedStartupAction.class).run();
      return null;
    }).when(context).runAsAdmin(any(PrivilegedStartupAction.class));

    PluginSet pluginSet = new PluginSet(
      "my-plugin-set",
      0,
      ImmutableSet.of("scm-git-plugin", "scm-hg-plugin"),
      ImmutableMap.of("en", new PluginSet.Description("My Plugin Set", List.of("this is awesome!"))),
      ImmutableMap.of("standard", "base64image")
    );

    PluginSet pluginSet2 = new PluginSet(
      "my-other-plugin-set",
      0,
      ImmutableSet.of("scm-svn-plugin", "scm-hg-plugin"),
      ImmutableMap.of("en", new PluginSet.Description("My Plugin Set", List.of("this is awesome!"))),
      ImmutableMap.of("standard", "base64image")
    );
    ImmutableSet<PluginSet> pluginSets = ImmutableSet.of(pluginSet, pluginSet2);
    when(pluginManager.getPluginSets()).thenReturn(pluginSets);
    when(pluginSetDtoMapper.map(pluginSets, availablePlugins, Locale.ENGLISH)).thenReturn(emptyList());

    when(resourceLinks.pluginWizard().indexLink("pluginWizard")).thenReturn("http://index.link");

    Embedded.Builder embeddedBuilder = new Embedded.Builder();
    Links.Builder linksBuilder = new Links.Builder();

    resource.setupIndex(linksBuilder, embeddedBuilder, Locale.ENGLISH);
    Embedded embedded = embeddedBuilder.build();
    Links links = linksBuilder.build();

    assertThat(links.getLinkBy("installPluginSets")).isPresent();
    assertThat(embedded.hasItem("pluginSets")).isTrue();
  }

  private byte[] createInput(String... pluginSetIds) {
    String format = pluginSetIds.length > 0 ? "'%s'" : "%s";
    return json(format("{'pluginSetIds': [" + format + "]}", StringUtils.join(pluginSetIds, "','")));
  }

  private byte[] json(String s) {
    return s.replaceAll("'", "\"").getBytes(UTF_8);
  }

}
