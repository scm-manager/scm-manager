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

import com.google.common.annotations.VisibleForTesting;
import com.google.inject.Inject;
import jakarta.inject.Named;
import jakarta.inject.Provider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;

public class TrustManagerProvider implements Provider<TrustManager> {

  private static final Logger LOG = LoggerFactory.getLogger(TrustManagerProvider.class);

  @Named("default")
  @Inject(optional = true)
  private Provider<TrustManager> customTrustManagerProvider;

  @VisibleForTesting
  void setCustomTrustManagerProvider(Provider<TrustManager> customTrustManagerProvider) {
    this.customTrustManagerProvider = customTrustManagerProvider;
  }

  @Override
  public TrustManager get() {
    if (customTrustManagerProvider != null) {
      LOG.trace("use custom trust manager from provider");
      return customTrustManagerProvider.get();
    } else {
      LOG.trace("could not find trust manager provider, use jvm default");
      return createDefaultTrustManager();
    }
  }
  private TrustManager createDefaultTrustManager() {
    try {
      TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
      trustManagerFactory.init((KeyStore) null);
      return trustManagerFactory.getTrustManagers()[0];
    } catch (NoSuchAlgorithmException | KeyStoreException ex) {
      throw new IllegalStateException(ex);
    }
  }
}
