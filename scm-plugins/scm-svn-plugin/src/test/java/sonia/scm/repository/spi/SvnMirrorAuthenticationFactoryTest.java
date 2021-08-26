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

package sonia.scm.repository.spi;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.auth.BasicAuthenticationManager;
import org.tmatesoft.svn.core.auth.ISVNAuthenticationManager;
import org.tmatesoft.svn.core.auth.SVNAuthentication;
import org.tmatesoft.svn.core.auth.SVNPasswordAuthentication;
import sonia.scm.config.ScmConfiguration;
import sonia.scm.net.GlobalProxyConfiguration;
import sonia.scm.net.ProxyConfiguration;
import sonia.scm.repository.api.SimpleUsernamePasswordCredential;

import javax.net.ssl.X509TrustManager;

import static java.util.Collections.singleton;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SvnMirrorAuthenticationFactoryTest {

  @Mock
  private X509TrustManager trustManager;

  private final ScmConfiguration configuration = new ScmConfiguration();

  private SvnMirrorAuthenticationFactory authenticationFactory;

  @BeforeEach
  void setUp() {
    authenticationFactory = new SvnMirrorAuthenticationFactory(
      trustManager, new GlobalProxyConfiguration(configuration)
    );
  }


  @Test
  void shouldUseCredentials() throws SVNException {
    SVNURL url = SVNURL.parseURIEncoded("https://svn.hitchhiker.com");

    MirrorCommandRequest request = new MirrorCommandRequest();
    request.setCredentials(
      singletonList(new SimpleUsernamePasswordCredential("trillian", "secret".toCharArray()))
    );

    BasicAuthenticationManager authenticationManager = authenticationFactory.create(url, request);

    SVNAuthentication authentication = authenticationManager.getFirstAuthentication(
      ISVNAuthenticationManager.PASSWORD, "Hitchhiker Auth Gate", url
    );

    assertThat(authentication).isInstanceOfSatisfying(SVNPasswordAuthentication.class, passwordAuth -> {
      assertThat(passwordAuth.getUserName()).isEqualTo("trillian");
      assertThat(passwordAuth.getPasswordValue()).isEqualTo("secret".toCharArray());
    });
  }

  @Test
  void shouldUseTrustManager() throws SVNException {
    SVNURL url = SVNURL.parseURIEncoded("https://svn.hitchhiker.com");

    BasicAuthenticationManager authenticationManager = authenticationFactory.create(url, new MirrorCommandRequest());

    assertThat(authenticationManager.getTrustManager(url)).isSameAs(trustManager);
  }

  @Test
  void shouldApplySimpleProxySettings() throws SVNException {
    configuration.setEnableProxy(true);
    configuration.setProxyServer("proxy.hitchhiker.com");
    configuration.setProxyPort(3128);

    BasicAuthenticationManager authenticationManager = createAuthenticationManager();

    assertThat(authenticationManager.getProxyHost()).isEqualTo("proxy.hitchhiker.com");
    assertThat(authenticationManager.getProxyPort()).isEqualTo(3128);
    assertThat(authenticationManager.getProxyUserName()).isNull();
    assertThat(authenticationManager.getProxyPasswordValue()).isNull();
  }

  @Test
  void shouldApplyProxySettingsWithCredentials() throws SVNException {
    configuration.setEnableProxy(true);
    configuration.setProxyServer("proxy.hitchhiker.com");
    configuration.setProxyPort(3128);
    configuration.setProxyUser("trillian");
    configuration.setProxyPassword("secret");

    BasicAuthenticationManager authenticationManager = createAuthenticationManager();

    assertThat(authenticationManager.getProxyHost()).isEqualTo("proxy.hitchhiker.com");
    assertThat(authenticationManager.getProxyPort()).isEqualTo(3128);
    assertThat(authenticationManager.getProxyUserName()).isEqualTo("trillian");
    assertThat(authenticationManager.getProxyPasswordValue()).isEqualTo("secret".toCharArray());
  }

  @Test
  void shouldSkipProxySettingsIfDisabled() throws SVNException {
    configuration.setEnableProxy(false);
    configuration.setProxyServer("proxy.hitchhiker.com");
    configuration.setProxyPort(3128);

    BasicAuthenticationManager authenticationManager = createAuthenticationManager();

    assertThat(authenticationManager.getProxyHost()).isNull();
  }

  @Test
  void shouldSkipProxySettingsIfHostIsOnExcludeList() throws SVNException {
    configuration.setEnableProxy(true);
    configuration.setProxyServer("proxy.hitchhiker.com");
    configuration.setProxyPort(3128);
    configuration.setProxyExcludes(singleton("svn.hitchhiker.com"));

    BasicAuthenticationManager authenticationManager = createAuthenticationManager();

    assertThat(authenticationManager.getProxyHost()).isNull();
  }

  @Test
  void shouldApplyLocalProxySettings() throws SVNException {
    MirrorCommandRequest request = new MirrorCommandRequest();
    request.setProxyConfiguration(createProxyConfiguration());

    BasicAuthenticationManager authenticationManager = createAuthenticationManager(request);
    assertThat(authenticationManager.getProxyHost()).isEqualTo("proxy.hitchhiker.com");
    assertThat(authenticationManager.getProxyPort()).isEqualTo(3128);
    assertThat(authenticationManager.getProxyUserName()).isNull();
    assertThat(authenticationManager.getProxyPasswordValue()).isNull();
  }

  @Test
  void shouldNotApplyDisabledLocalProxySettings() throws SVNException {
    MirrorCommandRequest request = new MirrorCommandRequest();
    request.setProxyConfiguration(createDisabledProxyConfiguration());

    BasicAuthenticationManager authenticationManager = createAuthenticationManager(request);
    assertThat(authenticationManager.getProxyHost()).isNull();
    assertThat(authenticationManager.getProxyPort()).isZero();
    assertThat(authenticationManager.getProxyUserName()).isNull();
    assertThat(authenticationManager.getProxyPasswordValue()).isNull();
  }

  private ProxyConfiguration createProxyConfiguration() {
    ProxyConfiguration configuration = mock(ProxyConfiguration.class);
    when(configuration.isEnabled()).thenReturn(true);
    when(configuration.getHost()).thenReturn("proxy.hitchhiker.com");
    when(configuration.getPort()).thenReturn(3128);
    return configuration;
  }

  private ProxyConfiguration createDisabledProxyConfiguration() {
    ProxyConfiguration configuration = mock(ProxyConfiguration.class);
    when(configuration.isEnabled()).thenReturn(false);
    lenient().when(configuration.getHost()).thenReturn("proxy.hitchhiker.com");
    lenient().when(configuration.getPort()).thenReturn(3128);
    return configuration;
  }

  private BasicAuthenticationManager createAuthenticationManager() throws SVNException {
    return createAuthenticationManager(new MirrorCommandRequest());
  }

  private BasicAuthenticationManager createAuthenticationManager(MirrorCommandRequest request) throws SVNException {
    SVNURL sourceUrl = SVNURL.parseURIEncoded("https://svn.hitchhiker.com");

    return authenticationFactory.create(sourceUrl, request);
  }


}
