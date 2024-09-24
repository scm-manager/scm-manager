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

import com.google.inject.util.Providers;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import static org.assertj.core.api.Assertions.assertThat;

class TrustManagerProviderTest {

  @Test
  void shouldReturnDefaultTrustManager() {
    TrustManager trustManager = new TrustManagerProvider().get();

    assertThat(trustManager).isInstanceOf(X509TrustManager.class);
  }

  @Nested
  class WithCustomTrustManagerProvider {

    @Test
    void shouldReturnCustomTrustManager() {
      TrustManagerProvider trustManagerProvider = new TrustManagerProvider();
      trustManagerProvider.setCustomTrustManagerProvider(Providers.of(new CustomTrustManager()));

      TrustManager trustManager = trustManagerProvider.get();

      assertThat(trustManager).isInstanceOf(CustomTrustManager.class);
    }
  }

  static class CustomTrustManager implements TrustManager {
  }
}
