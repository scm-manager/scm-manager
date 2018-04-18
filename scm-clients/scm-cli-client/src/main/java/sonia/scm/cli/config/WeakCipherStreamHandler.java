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

import javax.crypto.*;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

/**
 * Weak implementation of {@link CipherStreamHandler}. This is the old implementation, which was used in versions prior
 * 1.60.
 *
 * @see <a href="https://bitbucket.org/sdorra/scm-manager/issues/978/iteration-count-for-password-based">Issue 978</a>
 * @see <a href="https://bitbucket.org/sdorra/scm-manager/issues/979/constant-salts-for-pbe-are-insecure">Issue 979</a>
 */
public class WeakCipherStreamHandler implements CipherStreamHandler {

  private static final String SALT = "AE16347F";
  private static final int SPEC_ITERATION = 12;
  private static final String CIPHER_NAME = "PBEWithMD5AndDES";

  private final char[] secretKey;

  /**
   * Creates a new handler with the given secret key.
   *
   * @param secretKey secret key
   */
  public WeakCipherStreamHandler(char[] secretKey) {
    this.secretKey = secretKey;
  }

  @Override
  public InputStream decrypt(InputStream inputStream) {
    try {
      Cipher c = createCipher(Cipher.DECRYPT_MODE);
      return new CipherInputStream(inputStream, c);
    } catch (Exception ex) {
      throw new ScmConfigException("could not encrypt output stream", ex);
    }
  }

  @Override
  public OutputStream encrypt(OutputStream outputStream) {
    try {
      Cipher c = createCipher(Cipher.ENCRYPT_MODE);
      return new CipherOutputStream(outputStream, c);
    } catch (Exception ex) {
      throw new ScmConfigException("could not encrypt output stream", ex);
    }
  }

  private Cipher createCipher(int mode)
    throws NoSuchAlgorithmException, NoSuchPaddingException,
    InvalidKeySpecException, InvalidKeyException,
    InvalidAlgorithmParameterException
  {
    SecretKey sk = createSecretKey();
    Cipher cipher = Cipher.getInstance(CIPHER_NAME);
    PBEParameterSpec spec = new PBEParameterSpec(SALT.getBytes(), SPEC_ITERATION);

    cipher.init(mode, sk, spec);

    return cipher;
  }

  private SecretKey createSecretKey()
    throws NoSuchAlgorithmException, InvalidKeySpecException
  {
    PBEKeySpec keySpec = new PBEKeySpec(secretKey);
    SecretKeyFactory factory = SecretKeyFactory.getInstance(CIPHER_NAME);

    return factory.generateSecret(keySpec);
  }
}
