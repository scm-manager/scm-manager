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

package sonia.scm.security.gpg;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import sonia.scm.security.CipherUtil;
import sonia.scm.store.DataStore;
import sonia.scm.store.DataStoreFactory;
import sonia.scm.xml.XmlInstantAdapter;

import java.time.Instant;
import java.util.Optional;

@Singleton
class PrivateKeyStore {

  private static final String STORE_NAME = "gpg_private_keys";

  private final DataStore<RawPrivateKey> store;

  @Inject
  PrivateKeyStore(DataStoreFactory dataStoreFactory) {
    this.store = dataStoreFactory.withType(RawPrivateKey.class).withName(STORE_NAME).build();
  }

  Optional<String> getForUserId(String userId) {
    return store.getOptional(userId).map(rawPrivateKey -> CipherUtil.getInstance().decode(rawPrivateKey.key));
  }

  void setForUserId(String userId, String rawKey) {
    final String encodedRawKey = CipherUtil.getInstance().encode(rawKey);
    store.put(userId, new RawPrivateKey(encodedRawKey, Instant.now()));
  }

  @XmlRootElement
  @XmlAccessorType(XmlAccessType.FIELD)
  @AllArgsConstructor
  @NoArgsConstructor
  @Getter
  static class RawPrivateKey {
    private String key;

    @XmlJavaTypeAdapter(XmlInstantAdapter.class)
    private Instant date;
  }

}
