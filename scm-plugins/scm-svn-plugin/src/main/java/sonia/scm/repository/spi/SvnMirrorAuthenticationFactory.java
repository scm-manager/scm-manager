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

import com.google.common.base.Strings;
import jakarta.annotation.Nonnull;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.auth.BasicAuthenticationManager;
import org.tmatesoft.svn.core.auth.SVNAuthentication;
import org.tmatesoft.svn.core.auth.SVNPasswordAuthentication;
import org.tmatesoft.svn.core.auth.SVNSSLAuthentication;
import sonia.scm.net.GlobalProxyConfiguration;
import sonia.scm.net.ProxyConfiguration;
import sonia.scm.repository.api.Pkcs12ClientCertificateCredential;
import sonia.scm.repository.api.UsernamePasswordCredential;

import javax.net.ssl.TrustManager;
import java.util.ArrayList;
import java.util.Collection;

class SvnMirrorAuthenticationFactory {

  private final TrustManager trustManager;
  private final GlobalProxyConfiguration globalProxyConfiguration;

  SvnMirrorAuthenticationFactory(TrustManager trustManager, GlobalProxyConfiguration globalProxyConfiguration) {
    this.trustManager = trustManager;
    this.globalProxyConfiguration = globalProxyConfiguration;
  }

  BasicAuthenticationManager create(SVNURL url, MirrorCommandRequest request) {
    SVNAuthentication[] authentications = createAuthentications(url, request);

    BasicAuthenticationManager authManager = new BasicAuthenticationManager(authentications) {
      @Override
      public TrustManager getTrustManager(SVNURL url) {
        return trustManager;
      }
    };
    checkAndApplyProxyConfiguration(
      authManager, request.getProxyConfiguration().filter(ProxyConfiguration::isEnabled).orElse(globalProxyConfiguration), url
    );
    return authManager;
  }

  @Nonnull
  private SVNAuthentication[] createAuthentications(SVNURL url, MirrorCommandRequest mirrorCommandRequest) {
    Collection<SVNAuthentication> authentications = new ArrayList<>();
    mirrorCommandRequest.getCredential(Pkcs12ClientCertificateCredential.class)
      .map(c -> createTlsAuth(url, c))
      .ifPresent(authentications::add);
    mirrorCommandRequest.getCredential(UsernamePasswordCredential.class)
      .map(c -> SVNPasswordAuthentication.newInstance(c.username(), c.password(), false, url, false))
      .ifPresent(authentications::add);
    return authentications.toArray(new SVNAuthentication[0]);
  }

  private void checkAndApplyProxyConfiguration(BasicAuthenticationManager authManager, ProxyConfiguration proxyConfiguration, SVNURL url) {
    if (proxyConfiguration.isEnabled() && !proxyConfiguration.getExcludes().contains(url.getHost())) {
      applyProxyConfiguration(authManager, proxyConfiguration);
    }
  }

  private void applyProxyConfiguration(BasicAuthenticationManager authManager, ProxyConfiguration proxyConfiguration) {
    char[] password = null;
    if (!Strings.isNullOrEmpty(proxyConfiguration.getPassword())){
      password = proxyConfiguration.getPassword().toCharArray();
    }
    authManager.setProxy(
      proxyConfiguration.getHost(),
      proxyConfiguration.getPort(),
      Strings.emptyToNull(proxyConfiguration.getUsername()),
      password
    );
  }

  private SVNSSLAuthentication createTlsAuth(SVNURL url, Pkcs12ClientCertificateCredential c) {
    return SVNSSLAuthentication.newInstance(
      c.getCertificate(),
      c.getPassword(),
      false,
      url,
      true);
  }


}
