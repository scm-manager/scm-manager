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
import org.bouncycastle.openpgp.PGPException;
import org.bouncycastle.openpgp.PGPObjectFactory;
import org.bouncycastle.openpgp.PGPPublicKey;
import org.bouncycastle.openpgp.PGPPublicKeyRing;
import org.bouncycastle.openpgp.PGPSignature;
import org.bouncycastle.openpgp.operator.PGPContentVerifierBuilderProvider;
import org.bouncycastle.openpgp.operator.jcajce.JcaKeyFingerprintCalculator;
import org.bouncycastle.openpgp.operator.jcajce.JcaPGPContentVerifierBuilderProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.security.PublicKey;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

public class GpgKey implements PublicKey {

  private static final Logger LOG = LoggerFactory.getLogger(GpgKey.class);

  private final String id;
  private final String owner;

  public GpgKey(String id, String owner) {
    this.id = id;
    this.owner = owner;
  }

  @Override
  public String getId() {
    return id;
  }

  @Override
  public Optional<String> getOwner() {
    if (owner == null) {
      return Optional.empty();
    }
    return Optional.of(owner);
  }

  @Override
  public boolean verify(InputStream stream, byte[] signature) {
    boolean verified = false;
    try {
      ArmoredInputStream armoredInputStream = new ArmoredInputStream(new ByteArrayInputStream(signature));
      PGPObjectFactory pgpObjectFactory = new PGPObjectFactory(armoredInputStream, new JcaKeyFingerprintCalculator());
      PGPPublicKey publicKey = ((PGPPublicKeyRing) pgpObjectFactory.nextObject()).getPublicKey();
      PGPSignature pgpSignature = ((PGPSignature) publicKey.getSignatures().next());

      PGPContentVerifierBuilderProvider provider = new JcaPGPContentVerifierBuilderProvider();
      pgpSignature.init(provider, publicKey);

      char[] buffer = new char[1024];
      int bytesRead = 0;
      BufferedReader in = new BufferedReader(new InputStreamReader(stream));

      while (bytesRead != -1) {
        bytesRead = in.read(buffer, 0, 1024);
        pgpSignature.update(new String(buffer).getBytes(StandardCharsets.UTF_8));
      }

      verified = pgpSignature.verify();
    } catch (IOException | PGPException e) {
      LOG.error("Could not verify GPG key", e);
    }
    return verified;
  }
}


