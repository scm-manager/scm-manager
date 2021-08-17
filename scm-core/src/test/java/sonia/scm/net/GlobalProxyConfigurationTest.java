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
