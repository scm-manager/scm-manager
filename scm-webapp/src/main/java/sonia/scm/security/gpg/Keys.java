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

import org.bouncycastle.openpgp.PGPException;
import org.bouncycastle.openpgp.PGPPublicKey;
import org.bouncycastle.openpgp.PGPPublicKeyRing;
import org.bouncycastle.openpgp.PGPPublicKeyRingCollection;
import org.bouncycastle.openpgp.PGPUtil;
import org.bouncycastle.openpgp.operator.KeyFingerPrintCalculator;
import org.bouncycastle.openpgp.operator.jcajce.JcaKeyFingerprintCalculator;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

final class Keys {

  private static final KeyFingerPrintCalculator calculator = new JcaKeyFingerprintCalculator();

  private Keys() {}

  static String resolveIdFromKey(String rawKey) throws IOException, PGPException {
    List<PGPPublicKey> keys = collectKeys(rawKey);
    if (keys.size() > 1) {
      keys = keys.stream().filter(PGPPublicKey::isMasterKey).collect(Collectors.toList());
    }
    if (keys.isEmpty()) {
      throw new IllegalArgumentException("found multiple keys, but no master keys");
    }
    if (keys.size() > 1) {
      throw new IllegalArgumentException("found multiple master keys");
    }

    PGPPublicKey pgpPublicKey = keys.get(0);
    return createId(pgpPublicKey);
  }

  private static String createId(PGPPublicKey pgpPublicKey) {
    return "0x" + Long.toHexString(pgpPublicKey.getKeyID()).toUpperCase(Locale.ENGLISH);
  }

  private static List<PGPPublicKey> collectKeys(String rawKey) throws IOException, PGPException {
    List<PGPPublicKey> publicKeys = new ArrayList<>();
    InputStream decoderStream = PGPUtil.getDecoderStream(new ByteArrayInputStream(rawKey.getBytes(StandardCharsets.UTF_8)));
    PGPPublicKeyRingCollection collection = new PGPPublicKeyRingCollection(decoderStream, calculator);
    for (PGPPublicKeyRing pgpPublicKeys : collection) {
      for (PGPPublicKey pgpPublicKey : pgpPublicKeys) {
        publicKeys.add(pgpPublicKey);
      }
    }
    return publicKeys;
  }
}
