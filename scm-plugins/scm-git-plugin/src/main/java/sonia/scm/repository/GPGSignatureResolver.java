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

package sonia.scm.repository;

import com.google.common.collect.ImmutableMap;
import sonia.scm.security.GPG;
import sonia.scm.security.PublicKey;

import java.util.Map;
import java.util.Optional;

class GPGSignatureResolver {

  private final GPG gpg;
  private final Map<String, PublicKey> additionalPublicKeys;

  GPGSignatureResolver(GPG gpg, Iterable<PublicKey> additionalPublicKeys) {
    this.gpg = gpg;
    this.additionalPublicKeys = createKeyMap(additionalPublicKeys);
  }

  private Map<String, PublicKey> createKeyMap(Iterable<PublicKey> additionalPublicKeys) {
    ImmutableMap.Builder<String, PublicKey> builder = ImmutableMap.builder();
    for (PublicKey key : additionalPublicKeys) {
      appendKey(builder, key);
    }
    return builder.build();
  }

  private void appendKey(ImmutableMap.Builder<String, PublicKey> builder, PublicKey key) {
    builder.put(key.getId(), key);
    for (String subkey : key.getSubkeys()) {
      builder.put(subkey, key);
    }
  }

  String findPublicKeyId(byte[] signature) {
    return gpg.findPublicKeyId(signature);
  }

  Optional<PublicKey> findPublicKey(String id) {
    PublicKey publicKey = additionalPublicKeys.get(id);
    if (publicKey != null) {
      return Optional.of(publicKey);
    }
    return gpg.findPublicKey(id);
  }
}
