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
    assertThat(config.getPluginAuthUrl()).isEqualTo("https://plug.ins/oidc");
    assertThat(config.getLoginAttemptLimitTimeout()).isEqualTo(40);
    assertThat(config.isEnabledXsrfProtection()).isTrue();
    assertThat(config.isEnabledUserConverter()).isFalse();
    assertThat(config.getNamespaceStrategy()).isEqualTo("username");
    assertThat(config.getLoginInfoUrl()).isEqualTo("https://scm-manager.org/login-info");
    assertThat(config.getMailDomainName()).isEqualTo("hitchhiker.mail");
    assertThat(config.getEmergencyContacts()).contains(expectedUsers);
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
    configDto.setPluginAuthUrl("https://plug.ins/oidc");
    configDto.setLoginAttemptLimitTimeout(40);
    configDto.setEnabledXsrfProtection(true);
    configDto.setNamespaceStrategy("username");
    configDto.setLoginInfoUrl("https://scm-manager.org/login-info");
    configDto.setMailDomainName("hitchhiker.mail");
    configDto.setEmergencyContacts(Sets.newSet(expectedUsers));
    configDto.setEnabledUserConverter(false);

    return configDto;
  }
}
