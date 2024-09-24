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

package sonia.scm.net;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import lombok.Value;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import sonia.scm.config.ScmConfiguration;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalProxyConfigurationTest {

  @Test
  void shouldDelegateProxyConfigurationMethods() {
    ScmConfiguration scmConfig = createScmConfiguration("marvin", "brainLikeAPlanet");

    GlobalProxyConfiguration configuration = new GlobalProxyConfiguration(scmConfig);
    assertThat(configuration.isEnabled()).isEqualTo(scmConfig.isEnableProxy());
    assertThat(configuration.getHost()).isEqualTo(scmConfig.getProxyServer());
    assertThat(configuration.getPort()).isEqualTo(scmConfig.getProxyPort());
    assertThat(configuration.getUsername()).isEqualTo(scmConfig.getProxyUser());
    assertThat(configuration.getPassword()).isEqualTo(scmConfig.getProxyPassword());
    assertThat(configuration.getExcludes()).isSameAs(scmConfig.getProxyExcludes());
  }

  @MethodSource("createInvalidCredentials")
  @ParameterizedTest(name = "shouldReturnFalseForInvalidCredentials[{index}]")
  void shouldReturnFalseForInvalidCredentials(Credentials credentials) {
    GlobalProxyConfiguration configuration = new GlobalProxyConfiguration(createScmConfiguration(credentials));
    assertThat(configuration.isAuthenticationRequired()).isFalse();
  }

  @Test
  void shouldReturnTrueForValidCredentials() {
    GlobalProxyConfiguration configuration = new GlobalProxyConfiguration(createScmConfiguration("marvin", "secret"));
    assertThat(configuration.isAuthenticationRequired()).isTrue();
  }

  private ScmConfiguration createScmConfiguration(Credentials credentials) {
    return createScmConfiguration(credentials.getUsername(), credentials.getPassword());
  }

  private ScmConfiguration createScmConfiguration(String username, String password) {
    ScmConfiguration scmConfig = new ScmConfiguration();
    scmConfig.setEnableProxy(true);
    scmConfig.setProxyServer("proxy.hitchhiker.com");
    scmConfig.setProxyPort(3128);
    scmConfig.setProxyUser(username);
    scmConfig.setProxyPassword(password);
    scmConfig.setProxyExcludes(ImmutableSet.of("localhost", "127.0.0.1"));
    return scmConfig;
  }

  private static List<Credentials> createInvalidCredentials() {
    return ImmutableList.of(
      new Credentials(null, null),
      new Credentials("", ""),
      new Credentials("trillian", null),
      new Credentials("trillian", ""),
      new Credentials(null, "secret"),
      new Credentials("", "secret")
    );
  }

  @Value
  private static class Credentials {

    String username;
    String password;

  }

}
