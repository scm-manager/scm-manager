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
import java.util.Optional;

class DefaultPrivateKey implements PrivateKey {

  final Optional<PGPPrivateKey> privateKey;

  DefaultPrivateKey(String rawPrivateKey) {
    privateKey = KeysExtractor.extractPrivateKey(rawPrivateKey);
  }

  @Override
  public String getId() {
    return privateKey.map(Keys::createId).orElse(null);
  }

  @Override
  public byte[] sign(InputStream stream) {

    PGPSignatureGenerator signatureGenerator = new PGPSignatureGenerator(
      new JcaPGPContentSignerBuilder(
        PublicKeyAlgorithmTags.RSA_GENERAL,
        HashAlgorithmTags.SHA1).setProvider(BouncyCastleProvider.PROVIDER_NAME)
    );

    if (privateKey.isPresent()) {
      try {
        signatureGenerator.init(PGPSignature.BINARY_DOCUMENT, privateKey.get());
      } catch (PGPException e) {
        throw new GPGException("Could not initialize signature generator", e);
      }
    } else {
      throw new GPGException("Missing private key");
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
