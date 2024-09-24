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
