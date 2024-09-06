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
