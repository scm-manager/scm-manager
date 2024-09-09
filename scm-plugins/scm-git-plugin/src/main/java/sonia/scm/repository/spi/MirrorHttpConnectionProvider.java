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

import jakarta.inject.Inject;
import org.eclipse.jgit.transport.http.HttpConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.net.HttpConnectionOptions;
import sonia.scm.net.HttpURLConnectionFactory;
import sonia.scm.repository.api.Pkcs12ClientCertificateCredential;
import sonia.scm.repository.api.UsernamePasswordCredential;
import sonia.scm.util.HttpUtil;
import sonia.scm.web.ScmHttpConnectionFactory;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.util.Base64;
import java.util.List;

class MirrorHttpConnectionProvider {

  private static final Logger LOG = LoggerFactory.getLogger(MirrorHttpConnectionProvider.class);

  private final HttpURLConnectionFactory httpURLConnectionFactory;

  @Inject
  public MirrorHttpConnectionProvider(HttpURLConnectionFactory httpURLConnectionFactory) {
    this.httpURLConnectionFactory = httpURLConnectionFactory;
  }

  public HttpConnectionFactory createHttpConnectionFactory(MirrorCommandRequest mirrorCommandRequest, List<String> log) {
    HttpConnectionOptions options = new HttpConnectionOptions();

    mirrorCommandRequest.getCredential(Pkcs12ClientCertificateCredential.class)
      .ifPresent(c -> options.withKeyManagers(createKeyManagers(c, log)));
    mirrorCommandRequest.getProxyConfiguration()
      .ifPresent(options::withProxyConfiguration);
    mirrorCommandRequest.getCredential(UsernamePasswordCredential.class)
      .ifPresent(credential -> {
        String encodedAuth = Base64.getEncoder().encodeToString((credential.username() + ":" + new String(credential.password())).getBytes(StandardCharsets.UTF_8));
        String authHeaderValue = "Basic " + encodedAuth;
        options.addRequestProperty("Authorization", authHeaderValue);
      });
    options.addRequestProperty(HttpUtil.HEADER_USERAGENT, "git-lfs/2");

    return new ScmHttpConnectionFactory(httpURLConnectionFactory, options);
  }

  private KeyManager[] createKeyManagers(Pkcs12ClientCertificateCredential credential, List<String> log) {
    try {
      KeyStore pkcs12 = KeyStore.getInstance("PKCS12");
      pkcs12.load(new ByteArrayInputStream(credential.getCertificate()), credential.getPassword());
      KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
      keyManagerFactory.init(pkcs12, credential.getPassword());
      log.add("added pkcs12 certificate");
      return keyManagerFactory.getKeyManagers();
    } catch (IOException | GeneralSecurityException e) {
      LOG.info("could not create key store from pkcs12 credential", e);
      log.add("failed to add pkcs12 certificate: " + e.getMessage());
    }

    return new KeyManager[0];
  }
}
