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

import org.apache.shiro.subject.Subject;
import org.apache.shiro.subject.support.SubjectThreadState;
import org.apache.shiro.util.ThreadContext;
import org.apache.shiro.util.ThreadState;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.internal.util.collections.Sets;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.config.ScmConfiguration;
import sonia.scm.security.AnonymousMode;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ScmConfigurationToConfigDtoMapperTest {

  private final URI baseUri = URI.create("http://example.com/base/");

  private final String[] expectedExcludes = {"ex", "clude"};
  private final String[] expectedUsers = {"trillian", "arthur"};

  @SuppressWarnings("unused") // Is injected
  private ResourceLinks resourceLinks = ResourceLinksMock.createMock(baseUri);

  @InjectMocks
  private ScmConfigurationToConfigDtoMapperImpl mapper;

  private final Subject subject = mock(Subject.class);
  private final ThreadState subjectThreadState = new SubjectThreadState(subject);

  private URI expectedBaseUri;

  @BeforeEach
  void init() {
    expectedBaseUri = baseUri.resolve(ConfigResource.CONFIG_PATH_V2);
    subjectThreadState.bind();
    ThreadContext.bind(subject);
  }

  @AfterEach
  public void unbindSubject() {
    ThreadContext.unbindSubject();
  }

  @Test
  void shouldMapFields() {
    ScmConfiguration config = createConfiguration();

    when(subject.isPermitted("configuration:write:global")).thenReturn(true);
    ConfigDto dto = mapper.map(config);

    assertThat(dto.getProxyPassword()).isEqualTo("heartOfGold");
    assertThat(dto.getProxyPort()).isEqualTo(1234);
    assertThat(dto.getProxyServer()).isEqualTo("proxyserver");
    assertThat(dto.getProxyUser()).isEqualTo("trillian");
    assertThat(dto.isEnableProxy()).isTrue();
    assertThat(dto.getRealmDescription()).isEqualTo("description");
    assertThat(dto.isDisableGroupingGrid()).isTrue();
    assertThat(dto.getDateFormat()).isEqualTo("dd");
    assertThat(dto.getAnonymousMode()).isSameAs(AnonymousMode.FULL);
    assertThat(dto.getBaseUrl()).isEqualTo("baseurl");
    assertThat(dto.isForceBaseUrl()).isTrue();
    assertThat(dto.getLoginAttemptLimit()).isOne();
    assertThat(dto.getProxyExcludes()).contains(expectedExcludes);
    assertThat(dto.isSkipFailedAuthenticators()).isTrue();
    assertThat(dto.getPluginUrl()).isEqualTo("https://plug.ins");
    assertThat(dto.getPluginAuthUrl()).isEqualTo("https://plug.ins/oidc");
    assertThat(dto.getLoginAttemptLimitTimeout()).isEqualTo(2);
    assertThat(dto.isEnabledXsrfProtection()).isTrue();
    assertThat(dto.getNamespaceStrategy()).isEqualTo("username");
    assertThat(dto.getLoginInfoUrl()).isEqualTo("https://scm-manager.org/login-info");
    assertThat(dto.getAlertsUrl()).isEqualTo("https://alerts.scm-manager.org/api/v1/alerts");
    assertThat(dto.getReleaseFeedUrl()).isEqualTo("https://www.scm-manager.org/download/rss.xml");
    assertThat(dto.getMailDomainName()).isEqualTo("scm-manager.local");
    assertThat(dto.getEmergencyContacts()).contains(expectedUsers);
    assertLinks(dto);
  }

  private void assertLinks(ConfigDto dto) {
    assertThat(dto.getLinks().getLinkBy("self"))
      .hasValueSatisfying(link -> assertThat(link.getHref()).isEqualTo(expectedBaseUri.toString()));
    assertThat(dto.getLinks().getLinkBy("update"))
      .hasValueSatisfying(link -> assertThat(link.getHref()).isEqualTo(expectedBaseUri.toString()));
  }

  @Test
  void shouldMapFieldsWithoutUpdate() {
    ScmConfiguration config = createConfiguration();

    when(subject.hasRole("configuration:write:global")).thenReturn(false);
    ConfigDto dto = mapper.map(config);

    assertThat(dto.getBaseUrl()).isEqualTo("baseurl");
    assertThat(dto.getLinks().getLinkBy("self"))
      .hasValueSatisfying(link -> assertThat(link.getHref()).isEqualTo(expectedBaseUri.toString()));
    assertThat(dto.getLinks().hasLink("update")).isFalse();
  }

  @Test
  void shouldMapAnonymousAccessField() {
    ScmConfiguration config = createConfiguration();

    when(subject.hasRole("configuration:write:global")).thenReturn(false);
    ConfigDto dto = mapper.map(config);

    assertThat(dto.isAnonymousAccessEnabled()).isTrue();

    config.setAnonymousMode(AnonymousMode.OFF);
    ConfigDto secondDto = mapper.map(config);

    assertThat(secondDto.isAnonymousAccessEnabled()).isFalse();
  }

  private ScmConfiguration createConfiguration() {
    ScmConfiguration config = new ScmConfiguration();
    config.setProxyPassword("heartOfGold");
    config.setProxyPort(1234);
    config.setProxyServer("proxyserver");
    config.setProxyUser("trillian");
    config.setEnableProxy(true);
    config.setRealmDescription("description");
    config.setDisableGroupingGrid(true);
    config.setDateFormat("dd");
    config.setAnonymousMode(AnonymousMode.FULL);
    config.setBaseUrl("baseurl");
    config.setForceBaseUrl(true);
    config.setLoginAttemptLimit(1);
    config.setProxyExcludes(Sets.newSet(expectedExcludes));
    config.setSkipFailedAuthenticators(true);
    config.setPluginUrl("https://plug.ins");
    config.setPluginAuthUrl("https://plug.ins/oidc");
    config.setLoginAttemptLimitTimeout(2);
    config.setEnabledXsrfProtection(true);
    config.setNamespaceStrategy("username");
    config.setLoginInfoUrl("https://scm-manager.org/login-info");
    config.setAlertsUrl("https://alerts.scm-manager.org/api/v1/alerts");
    config.setReleaseFeedUrl("https://www.scm-manager.org/download/rss.xml");
    config.setEmergencyContacts(Sets.newSet(expectedUsers));
    return config;
  }

}
