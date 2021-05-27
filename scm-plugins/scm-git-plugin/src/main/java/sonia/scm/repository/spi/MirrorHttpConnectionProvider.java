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

import org.eclipse.jgit.transport.http.HttpConnectionFactory2;
import sonia.scm.repository.api.Pkcs12ClientCertificateCredential;
import sonia.scm.web.ScmHttpConnectionFactory;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManager;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyStore;

class MirrorHttpConnectionProvider {

  private final Provider<TrustManager> trustManagerProvider;

  @Inject
  public MirrorHttpConnectionProvider(Provider<TrustManager> trustManagerProvider) {
    this.trustManagerProvider = trustManagerProvider;
  }

  public HttpConnectionFactory2 createHttpConnectionFactory(Pkcs12ClientCertificateCredential credential) {
    return new ScmHttpConnectionFactory(trustManagerProvider, createKeyManagers(credential));
  }

  private KeyManager[] createKeyManagers(Pkcs12ClientCertificateCredential credential) {
    try {
      KeyStore pkcs12 = KeyStore.getInstance("PKCS12");
      pkcs12.load(new ByteArrayInputStream(credential.getCertificate()), credential.getPassword());
      KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
      keyManagerFactory.init(pkcs12, credential.getPassword());
      return keyManagerFactory.getKeyManagers();
    } catch (IOException | GeneralSecurityException e) {
      e.printStackTrace();
    }

    return new KeyManager[0];
  }
}
