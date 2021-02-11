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

package sonia.scm.importexport;


import com.google.common.base.Strings;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Arrays;

public class RepositoryImportExportEncryption {

  private static final SecureRandom SECURE_RANDOM = new SecureRandom();
  private static final String SALTED_HEADER = "Salted__";

  static {
    SECURE_RANDOM.setSeed(System.currentTimeMillis());
  }

  public OutputStream encrypt(OutputStream origin, String secret) throws IOException {
    if (!Strings.isNullOrEmpty(secret)) {
      byte[] salt = createSalt();
      writeSaltHeader(origin, salt);
      Cipher cipher = createCipher(Cipher.ENCRYPT_MODE, secret, salt);
      return new CipherOutputStream(origin, cipher);
    } else {
      return origin;
    }
  }

  public InputStream decrypt(InputStream encryptedStream, String secret) throws IOException {
    if (!Strings.isNullOrEmpty(secret)) {
      byte[] salt = readSaltHeader(encryptedStream);
      Cipher cipher = createCipher(Cipher.DECRYPT_MODE, secret, salt);
      return new CipherInputStream(encryptedStream, cipher);
    } else {
      return encryptedStream;
    }
  }

  private void writeSaltHeader(OutputStream origin, byte[] salt) throws IOException {
    origin.write(SALTED_HEADER.getBytes(StandardCharsets.ISO_8859_1));
    origin.write(salt);
  }

  private byte[] readSaltHeader(InputStream encryptedStream) throws IOException {
    byte[] header = new byte[8];
    int headerBytesRead = encryptedStream.read(header);
    if (headerBytesRead != 8 || !SALTED_HEADER.equals(new String(header, StandardCharsets.ISO_8859_1))) {
      throw new IOException("Expected header with salt not found (\"Salted__\")");
    }
    byte[] salt = new byte[8];
    int lengthRead = encryptedStream.read(salt);
    if (lengthRead != 8) {
      throw new IOException("Failed to read salt from input");
    }
    return salt;
  }

  private Cipher createCipher(int encryptMode, String key, byte[] salt) {
    Cipher cipher = getCipher();
    try {
      cipher.init(encryptMode, getSecretKeySpec(key.toCharArray(), salt), getIvSpec(key.toCharArray(), salt));
    } catch (InvalidKeyException | InvalidAlgorithmParameterException e) {
      throw new IllegalStateException("Could not initialize cipher", e);
    }
    return cipher;
  }

  private Cipher getCipher() {
    try {
      return Cipher.getInstance("AES/CBC/PKCS5Padding");
    } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
      throw new IllegalStateException("Could not initialize cipher", e);
    }
  }

  private byte[] createSalt() {
    byte[] salt = new byte[8];
    SECURE_RANDOM.nextBytes(salt);
    return salt;
  }

  private SecretKeySpec getSecretKeySpec(char[] key, byte[] salt) {
    SecretKey secretKey = computeSecretKey(key, salt);
    return new SecretKeySpec(secretKey.getEncoded(), "AES");
  }

  private SecretKey computeSecretKey(char[] password, byte[] salt) {
    KeySpec spec = getKeySpec(password, salt);
    try {
      return new SecretKeySpec(getSecretKeyFactory().generateSecret(spec).getEncoded(), "AES");
    } catch (InvalidKeySpecException e) {
      throw new IllegalStateException("could not create key spec", e);
    }
  }

  private PBEKeySpec getKeySpec(char[] password, byte[] salt) {
    return new PBEKeySpec(password, salt, 10000, 256);
  }

  @SuppressWarnings("java:S3329") // we generate the IV by deriving it from the password; this should be pseudo random enough
  private IvParameterSpec getIvSpec(char[] password, byte[] salt) {
    PBEKeySpec spec = new PBEKeySpec(password, salt, 1000, 256);
    try {
      byte[] bytes = getSecretKeyFactory().generateSecret(spec).getEncoded();
      byte[] iv = Arrays.copyOfRange(bytes, 16, 16 + 16);
      return new IvParameterSpec(iv);
    } catch (InvalidKeySpecException e) {
      throw new IllegalStateException("Could not derive from key", e);
    }
  }

  private SecretKeyFactory getSecretKeyFactory() {
    try {
      return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException("Could not instantiate secret key factory", e);
    }
  }
}
