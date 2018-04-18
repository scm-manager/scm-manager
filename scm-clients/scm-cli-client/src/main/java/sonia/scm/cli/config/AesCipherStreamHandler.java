/**
 * Copyright (c) 2010, Sebastian Sdorra
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 3. Neither the name of SCM-Manager; nor the names of its
 *    contributors may be used to endorse or promote products derived from this
 *    software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * http://bitbucket.org/sdorra/scm-manager
 *
 */


package sonia.scm.cli.config;

import com.google.common.base.Charsets;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.SecureRandom;

/**
 * Implementation of {@link CipherStreamHandler} which uses AES. This version is used since version 1.60 for the
 * cli client encryption.
 *
 * @author Sebastian Sdorra
 * @since 1.60
 */
public class AesCipherStreamHandler implements CipherStreamHandler {

  private static final String ALGORITHM = "AES/GCM/NoPadding";

  private final SecureRandom random = new SecureRandom();

  private final byte[] secretKey;

  AesCipherStreamHandler(String secretKey) {
    this.secretKey = secretKey.getBytes(Charsets.UTF_8);
  }

  @Override
  public OutputStream encrypt(OutputStream outputStream) throws IOException {
    Cipher cipher = createCipherForEncryption();
    outputStream.write(cipher.getIV());
    return new CipherOutputStream(outputStream, cipher);
  }

  @Override
  public InputStream decrypt(InputStream inputStream) throws IOException {
    Cipher cipher = createCipherForDecryption(inputStream);
    return new CipherInputStream(inputStream, cipher);
  }

  private Cipher createCipherForDecryption(InputStream inputStream) throws IOException {
    byte[] iv =new byte[12];
    inputStream.read(iv);
    return createCipher(Cipher.DECRYPT_MODE, iv);
  }

  private Cipher createCipherForEncryption() {
    byte[] iv = generateIV();
    return createCipher(Cipher.ENCRYPT_MODE, iv);
  }

  private byte[] generateIV() {
    // use 12 byte as described at nist
    // https://nvlpubs.nist.gov/nistpubs/Legacy/SP/nistspecialpublication800-38d.pdf
    byte[] iv = new byte[12];
    random.nextBytes(iv);
    return iv;
  }

  private Cipher createCipher(int mode, byte[] iv) {
    try {
      Cipher cipher = Cipher.getInstance(ALGORITHM);
      GCMParameterSpec parameterSpec = new GCMParameterSpec(128, iv);
      cipher.init(mode, new SecretKeySpec(secretKey, "AES"), parameterSpec);
      return cipher;
    } catch (Exception ex) {
      throw new ScmConfigException("failed to create cipher", ex);
    }
  }
}
