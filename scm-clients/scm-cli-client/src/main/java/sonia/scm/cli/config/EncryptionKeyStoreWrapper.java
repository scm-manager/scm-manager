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

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Charsets;
import com.google.common.base.Strings;
import com.google.common.io.BaseEncoding;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

/**
 * The EncryptionKeyStoreWrapper is a wrapper around the {@link KeyStore} interface. The wrapper will encrypt the passed
 * keys, before they are written to the underlying {@link KeyStore} implementation. The wrapper will also honor old
 * unencrypted keys.
 *
 * @author Sebastian Sdorra
 * @since 1.60
 */
public class EncryptionKeyStoreWrapper implements KeyStore {

  private static final String ALGORITHM = "AES";

  private static final SecureRandom random = new SecureRandom();

  // i know storing the key directly in the class is far away from a best practice, but this is a chicken egg type
  // of problem. We need a key to encrypt the stored keys, however encrypting the keys with a static defined key
  // is better as storing them as plain text.
  private static final byte[] SECRET_KEY = new byte[]{  0x50, 0x61, 0x41, 0x67, 0x55, 0x43, 0x48, 0x7a, 0x48, 0x59,
    0x7a, 0x57, 0x6b, 0x34, 0x54, 0x62
  };

  @VisibleForTesting
  static final String ENCRYPTED_PREFIX = "enc:";

  private KeyStore wrappedKeyStore;

  EncryptionKeyStoreWrapper(KeyStore wrappedKeyStore) {
    this.wrappedKeyStore = wrappedKeyStore;
  }

  @Override
  public void set(String secretKey) {
    String encrypted = encrypt(secretKey);
    wrappedKeyStore.set(ENCRYPTED_PREFIX.concat(encrypted));
  }

  private String encrypt(String value) {
    try {
      Cipher cipher = createCipher(Cipher.ENCRYPT_MODE);
      byte[] raw = cipher.doFinal(value.getBytes(Charsets.UTF_8));
      return encode(raw);
    } catch (IllegalBlockSizeException | BadPaddingException ex) {
      throw new ScmConfigException("failed to encrypt key", ex);
    }
  }

  private String encode(byte[] raw) {
    return BaseEncoding.base64().encode(raw);
  }

  @Override
  public String get() {
    String value = wrappedKeyStore.get();
    if (Strings.nullToEmpty(value).startsWith(ENCRYPTED_PREFIX)) {
      String encrypted = value.substring(ENCRYPTED_PREFIX.length());
      return decrypt(encrypted);
    }
    return value;
  }

  private String decrypt(String encoded) {
    try {
      Cipher cipher = createCipher(Cipher.DECRYPT_MODE);
      byte[] raw = decode(encoded);
      return new String(cipher.doFinal(raw), Charsets.UTF_8);
    } catch (IllegalBlockSizeException | BadPaddingException ex) {
      throw new ScmConfigException("failed to decrypt key", ex);
    }
  }

  private byte[] decode(String encoded) {
    return BaseEncoding.base64().decode(encoded);
  }

  private Cipher createCipher(int mode) {
    try {
      Cipher cipher = Cipher.getInstance(ALGORITHM);
      SecretKeySpec secretKeySpec = new SecretKeySpec(SECRET_KEY, "AES");
      cipher.init(mode, secretKeySpec, random);
      return cipher;
    } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException ex) {
      throw new ScmConfigException("failed to create key", ex);
    }
  }

  @Override
  public void remove() {
    wrappedKeyStore.remove();
  }
}
