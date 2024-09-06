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

import lombok.Value;
import org.bouncycastle.openpgp.PGPException;
import org.bouncycastle.openpgp.PGPPrivateKey;
import org.bouncycastle.openpgp.PGPPublicKey;
import org.bouncycastle.openpgp.PGPPublicKeyRing;
import org.bouncycastle.openpgp.PGPPublicKeyRingCollection;
import org.bouncycastle.openpgp.PGPUtil;
import org.bouncycastle.openpgp.operator.KeyFingerPrintCalculator;
import org.bouncycastle.openpgp.operator.jcajce.JcaKeyFingerprintCalculator;
import sonia.scm.ScmConstraintViolationException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.function.Function;

import static sonia.scm.ScmConstraintViolationException.Builder.doThrow;

@Value
final class Keys {

  private static final KeyFingerPrintCalculator calculator = new JcaKeyFingerprintCalculator();

  private final String master;
  private final Set<String> subs;

  private Keys(String master, Set<String> subs) {
    this.master = master;
    this.subs = subs;
  }

  static Keys resolve(String raw) {
    return resolve(raw, Keys::collectKeys);
  }

  static Keys resolve(String raw, Function<String, List<PGPPublicKey>> parser) {
    List<PGPPublicKey> parsedKeys = parser.apply(raw);

    String master = null;
    Set<String> subs = new HashSet<>();

    for (PGPPublicKey key : parsedKeys) {
      if (key.isMasterKey()) {
        doThrow()
          .violation("Found more than one master key")
          .when(master != null);
        master = createId(key);
      } else {
        subs.add(createId(key));
      }
    }

    doThrow()
      .violation("No master key found")
      .when(master == null);

    return new Keys(master, Collections.unmodifiableSet(subs));
  }

  static String createId(PGPPublicKey pgpPublicKey) {
    return formatKey(pgpPublicKey.getKeyID());
  }

  static String createId(PGPPrivateKey pgpPrivateKey) {
    return formatKey(pgpPrivateKey.getKeyID());
  }

  static String formatKey(long keyId) {
    return "0x" + Long.toHexString(keyId).toUpperCase(Locale.ENGLISH);
  }

  private static List<PGPPublicKey> collectKeys(String rawKey) {
    try {
      List<PGPPublicKey> publicKeys = new ArrayList<>();
      InputStream decoderStream = PGPUtil.getDecoderStream(new ByteArrayInputStream(rawKey.getBytes(StandardCharsets.UTF_8)));
      PGPPublicKeyRingCollection collection = new PGPPublicKeyRingCollection(decoderStream, calculator);
      for (PGPPublicKeyRing pgpPublicKeys : collection) {
        for (PGPPublicKey pgpPublicKey : pgpPublicKeys) {
          publicKeys.add(pgpPublicKey);
        }
      }
      return publicKeys;
    } catch (IOException | PGPException ex) {
      throw new GPGException("Failed to collect public keys", ex);
    }
  }
}
