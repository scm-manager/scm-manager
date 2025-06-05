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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.internal.util.collections.Sets;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.config.ScmConfiguration;
import sonia.scm.security.AnonymousMode;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class ConfigDtoToScmConfigurationMapperTest {

  @InjectMocks
  private ConfigDtoToScmConfigurationMapperImpl mapper;

  private final String[] expectedExcludes = {"ex", "clude"};
  private final String[] expectedUsers = {"trillian", "arthur"};

  @Test
  void shouldMapFields() {
    ConfigDto dto = createDefaultDto();
    ScmConfiguration config = mapper.map(dto);

    assertThat(config.getProxyPassword()).isEqualTo("prPw");
    assertThat(config.getProxyPort()).isEqualTo(42);
    assertThat(config.getProxyServer()).isEqualTo("srvr");
    assertThat(config.getProxyUser()).isEqualTo("user");
    assertThat(config.isEnableProxy()).isTrue();
    assertThat(config.getRealmDescription()).isEqualTo("realm");
    assertThat(config.isDisableGroupingGrid()).isTrue();
    assertThat(config.getDateFormat()).isEqualTo("yyyy");
    assertThat(config.getAnonymousMode()).isSameAs(AnonymousMode.PROTOCOL_ONLY);
    assertThat(config.getBaseUrl()).isEqualTo("baseurl");
    assertThat(config.isForceBaseUrl()).isTrue();
    assertThat(config.getLoginAttemptLimit()).isEqualTo(41);
    assertThat(config.getProxyExcludes()).contains(expectedExcludes);
    assertThat(config.isSkipFailedAuthenticators()).isTrue();
    assertThat(config.getPluginUrl()).isEqualTo("https://plug.ins");
    assertThat(config.getLoginAttemptLimitTimeout()).isEqualTo(40);
    assertThat(config.isEnabledXsrfProtection()).isTrue();
    assertThat(config.isEnabledUserConverter()).isFalse();
    assertThat(config.getNamespaceStrategy()).isEqualTo("username");
    assertThat(config.getLoginInfoUrl()).isEqualTo("https://scm-manager.org/login-info");
    assertThat(config.getMailDomainName()).isEqualTo("hitchhiker.mail");
    assertThat(config.getEmergencyContacts()).contains(expectedUsers);
    assertThat(config.getJwtExpirationInH()).isEqualTo(10);
    assertThat(config.isJwtEndless()).isFalse();
  }

  @Test
  void shouldMapAnonymousAccessFieldToAnonymousMode() {
    ConfigDto dto = createDefaultDto();

    ScmConfiguration config = mapper.map(dto);

    assertThat(config.getAnonymousMode()).isSameAs(AnonymousMode.PROTOCOL_ONLY);

    dto.setAnonymousMode(null);
    dto.setAnonymousAccessEnabled(false);
    ScmConfiguration config2 = mapper.map(dto);

    assertThat(config2.getAnonymousMode()).isSameAs(AnonymousMode.OFF);
  }

  private ConfigDto createDefaultDto() {
    ConfigDto configDto = new ConfigDto();
    configDto.setProxyPassword("prPw");
    configDto.setProxyPort(42);
    configDto.setProxyServer("srvr");
    configDto.setProxyUser("user");
    configDto.setEnableProxy(true);
    configDto.setRealmDescription("realm");
    configDto.setDisableGroupingGrid(true);
    configDto.setDateFormat("yyyy");
    configDto.setAnonymousMode(AnonymousMode.PROTOCOL_ONLY);
    configDto.setBaseUrl("baseurl");
    configDto.setForceBaseUrl(true);
    configDto.setLoginAttemptLimit(41);
    configDto.setProxyExcludes(Sets.newSet(expectedExcludes));
    configDto.setSkipFailedAuthenticators(true);
    configDto.setPluginUrl("https://plug.ins");
    configDto.setLoginAttemptLimitTimeout(40);
    configDto.setEnabledXsrfProtection(true);
    configDto.setNamespaceStrategy("username");
    configDto.setLoginInfoUrl("https://scm-manager.org/login-info");
    configDto.setMailDomainName("hitchhiker.mail");
    configDto.setEmergencyContacts(Sets.newSet(expectedUsers));
    configDto.setEnabledUserConverter(false);
    configDto.setJwtExpirationInH(10);
    configDto.setEnabledJwtEndless(false);

    return configDto;
  }
}
