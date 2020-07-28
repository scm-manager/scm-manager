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

import org.bouncycastle.bcpg.ArmoredInputStream;
import org.bouncycastle.openpgp.PGPObjectFactory;
import org.bouncycastle.openpgp.PGPSignatureList;
import org.bouncycastle.openpgp.operator.jcajce.JcaKeyFingerprintCalculator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.security.GPG;
import sonia.scm.security.PrivateKey;
import sonia.scm.security.PublicKey;

import javax.inject.Inject;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class DefaultGPG implements GPG {

  private static final Logger LOG = LoggerFactory.getLogger(DefaultGPG.class);
  private final PublicKeyStore store;

  @Inject
  public DefaultGPG(PublicKeyStore store) {
    this.store = store;
  }

  @Override
  public String findPublicKeyId(byte[] signature) {
    try {
      ArmoredInputStream armoredInputStream = new ArmoredInputStream(new ByteArrayInputStream(signature));
      PGPObjectFactory pgpObjectFactory = new PGPObjectFactory(armoredInputStream, new JcaKeyFingerprintCalculator());
      PGPSignatureList signatures = (PGPSignatureList) pgpObjectFactory.nextObject();
      return "0x" + Long.toHexString(signatures.get(0).getKeyID()).toUpperCase();
    } catch (IOException e) {
      LOG.error("Could not find public key id in signature");
    }
    return "";
  }

  @Override
  public Optional<PublicKey> findPublicKey(String id) {
    Optional<RawGpgKey> key = store.findById(id);

    return key.map(rawGpgKey -> new GpgKey(id, rawGpgKey.getOwner(), rawGpgKey.getRaw().getBytes()));
  }

  @Override
  public Iterable<PublicKey> findPublicKeysByUsername(String username) {
    List<RawGpgKey> keys = store.findByUsername(username);

    if (!keys.isEmpty()) {
      return keys
        .stream()
        .map(rawGpgKey -> new GpgKey(rawGpgKey.getId(), rawGpgKey.getOwner(), rawGpgKey.getRaw().getBytes()))
        .collect(Collectors.toSet());
    }

    return Collections.emptySet();
  }

  @Override
  public PrivateKey getPrivateKey() {
    throw new UnsupportedOperationException("getPrivateKey is not yet implemented");
  }
}
