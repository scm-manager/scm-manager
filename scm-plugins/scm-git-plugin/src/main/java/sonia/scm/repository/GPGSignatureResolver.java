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
