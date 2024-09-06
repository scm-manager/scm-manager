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

import org.bouncycastle.bcpg.ArmoredInputStream;
import org.bouncycastle.openpgp.PGPObjectFactory;
import org.bouncycastle.openpgp.PGPPrivateKey;
import org.bouncycastle.openpgp.PGPPublicKey;
import org.bouncycastle.openpgp.PGPPublicKeyRing;
import org.bouncycastle.openpgp.PGPUtil;
import org.bouncycastle.openpgp.jcajce.JcaPGPSecretKeyRingCollection;
import org.bouncycastle.openpgp.operator.jcajce.JcaKeyFingerprintCalculator;
import org.bouncycastle.openpgp.operator.jcajce.JcePBESecretKeyDecryptorBuilder;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

final class KeysExtractor {

  private KeysExtractor() {}

  static PGPPrivateKey extractPrivateKey(String rawKey) {
    try (final InputStream decoderStream = PGPUtil.getDecoderStream(new ByteArrayInputStream(rawKey.getBytes()))) {
      JcaPGPSecretKeyRingCollection secretKeyRingCollection = new JcaPGPSecretKeyRingCollection(decoderStream);
      return secretKeyRingCollection.getKeyRings().next().getSecretKey().extractPrivateKey(new JcePBESecretKeyDecryptorBuilder().build(new char[]{}));
    } catch (Exception e) {
      throw new GPGException("Invalid PGP key", e);
    }
  }

  static PGPPublicKey extractPublicKey(String rawKey) {
    try {
      ArmoredInputStream armoredInputStream = new ArmoredInputStream(new ByteArrayInputStream(rawKey.getBytes()));
      PGPObjectFactory pgpObjectFactory = new PGPObjectFactory(armoredInputStream, new JcaKeyFingerprintCalculator());
      return ((PGPPublicKeyRing) pgpObjectFactory.nextObject()).getPublicKey();
    } catch (IOException e) {
      throw new GPGException("Invalid PGP key", e);
    }
  }
}
