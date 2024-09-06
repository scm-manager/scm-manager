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

import jakarta.validation.constraints.NotNull;
import org.apache.commons.io.IOUtils;
import org.bouncycastle.bcpg.ArmoredOutputStream;
import org.bouncycastle.bcpg.BCPGOutputStream;
import org.bouncycastle.bcpg.HashAlgorithmTags;
import org.bouncycastle.bcpg.PublicKeyAlgorithmTags;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openpgp.PGPException;
import org.bouncycastle.openpgp.PGPPrivateKey;
import org.bouncycastle.openpgp.PGPSignature;
import org.bouncycastle.openpgp.PGPSignatureGenerator;
import org.bouncycastle.openpgp.operator.jcajce.JcaPGPContentSignerBuilder;
import sonia.scm.security.PrivateKey;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.Security;

class DefaultPrivateKey implements PrivateKey {

  static {
    if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
      Security.addProvider(new BouncyCastleProvider());
    }
  }

  static DefaultPrivateKey parseRaw(String rawPrivateKey) {
    return new DefaultPrivateKey(KeysExtractor.extractPrivateKey(rawPrivateKey));
  }

  private final PGPPrivateKey privateKey;

  private DefaultPrivateKey(@NotNull PGPPrivateKey privateKey) {
    this.privateKey = privateKey;
  }

  @Override
  public String getId() {
    return Keys.createId(privateKey);
  }

  @Override
  public byte[] sign(InputStream stream) {

    PGPSignatureGenerator signatureGenerator = new PGPSignatureGenerator(
      new JcaPGPContentSignerBuilder(
        PublicKeyAlgorithmTags.RSA_GENERAL,
        HashAlgorithmTags.SHA1).setProvider(BouncyCastleProvider.PROVIDER_NAME)
    );

    try {
      signatureGenerator.init(PGPSignature.BINARY_DOCUMENT, privateKey);
    } catch (PGPException e) {
      throw new GPGException("Could not initialize signature generator", e);
    }

    ByteArrayOutputStream buffer = new ByteArrayOutputStream();
    try (BCPGOutputStream out = new BCPGOutputStream(new ArmoredOutputStream(buffer))) {
      signatureGenerator.update(IOUtils.toByteArray(stream));
      signatureGenerator.generate().encode(out);
    } catch (PGPException | IOException e) {
      throw new GPGException("Could not create signature", e);
    }

    return buffer.toByteArray();
  }
}
