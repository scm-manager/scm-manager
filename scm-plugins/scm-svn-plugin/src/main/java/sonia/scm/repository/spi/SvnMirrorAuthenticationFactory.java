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
