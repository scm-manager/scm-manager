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

import org.eclipse.jgit.transport.http.HttpConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.net.HttpConnectionOptions;
import sonia.scm.net.HttpURLConnectionFactory;
import sonia.scm.repository.api.Pkcs12ClientCertificateCredential;
import sonia.scm.repository.api.UsernamePasswordCredential;
import sonia.scm.web.ScmHttpConnectionFactory;

import javax.inject.Inject;
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
