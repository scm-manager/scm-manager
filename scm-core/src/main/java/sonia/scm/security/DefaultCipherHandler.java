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



package sonia.scm.security;

//~--- non-JDK imports --------------------------------------------------------

import com.google.common.annotations.VisibleForTesting;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sonia.scm.SCMContextProvider;
import sonia.scm.util.IOUtil;

//~--- JDK imports ------------------------------------------------------------

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.security.GeneralSecurityException;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import java.util.Arrays;
import java.util.Base64;

import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * Default implementation of the {@link CipherHandler}, which uses AES for 
 * encryption and decryption.
 * 
 * @author Sebastian Sdorra
 * @since 1.7
 */
public class DefaultCipherHandler implements CipherHandler {

  /** used cipher type */
  public static final String CIPHER_TYPE = "AES/CTR/PKCS5PADDING";

  /** digest type for key generation */
  public static final String DIGEST_TYPE = "SHA-512";

  /** string encoding */
  public static final String ENCODING = "UTF-8";

  /** default key length */
  public static final int KEY_LENGTH = 16;

  /** default salt length */
  public static final int SALT_LENGTH = 16;

  @VisibleForTesting
  static final String CIPHERKEY_FILENAME = ".cipherkey";

  private static final char[] KEY_BASE = new char[]
  {
    '1', '4', '7', '3', 'F', '2', '1', 'E', '-', 'C', '4', 'C', '4', '-', '4',
    '6', 'C', 'C', '-', '8', '7', 'F', '6', '-', '7', 'B', '4', 'F', '0', '5',
    'E', 'C', '7', '7', '2', 'E'
  };

  private static final String KEY_TYPE = "AES";

  /** the logger for DefaultCipherHandler */
  private static final Logger logger = LoggerFactory.getLogger(DefaultCipherHandler.class);
  
  private final SecureRandom random = new SecureRandom();
  
  private final char[] key;
  
  /**
   * Constructs a new DefaultCipherHandler. Note this constructor is only for
   * unit tests.
   *
   * @param key default encryption key
   *
   * @since 1.38
   */
  @VisibleForTesting
  protected DefaultCipherHandler(String key) {
    this.key = key.toCharArray();
  }

  /**
   * Constructs a new instance and reads the default key from the scm home directory, 
   * if the key file does not exists it will be generated with the {@link KeyGenerator}.
   *
   * @param context SCM-Manager context provider
   * @param keyGenerator key generator for default key generation
   */
  public DefaultCipherHandler(SCMContextProvider context, KeyGenerator keyGenerator) {
    File configDirectory = new File(context.getBaseDirectory(), "config");

    IOUtil.mkdirs(configDirectory);
    File cipherKeyFile = new File(configDirectory, CIPHERKEY_FILENAME);

    try {
      if (cipherKeyFile.exists()) {
        key = loadKey(cipherKeyFile);
      } else {
        key = keyGenerator.createKey().toCharArray();
        storeKey(cipherKeyFile);
      }
    } catch (IOException ex) {
      throw new CipherException("could not create CipherHandler", ex);
    }
  }

  //~--- methods --------------------------------------------------------------

  @Override
  public String decode(String value) {
    return decode(key, value);
  }

  /**
   * Decodes the given value with the provided key.
   *
   * @param plainKey key which is used for decoding
   * @param value encrypted value
   *
   * @return decrypted value
   */
  public String decode(char[] plainKey, String value) {
    String result = null;

    try {
      byte[] encodedInput = Base64.getDecoder().decode(value);
      byte[] salt = new byte[SALT_LENGTH];
      byte[] encoded = new byte[encodedInput.length - SALT_LENGTH];

      System.arraycopy(encodedInput, 0, salt, 0, SALT_LENGTH);
      System.arraycopy(encodedInput, SALT_LENGTH, encoded, 0,
        encodedInput.length - SALT_LENGTH);

      IvParameterSpec iv = new IvParameterSpec(salt);
      SecretKey secretKey = buildSecretKey(plainKey);
      javax.crypto.Cipher cipher = javax.crypto.Cipher.getInstance(CIPHER_TYPE);

      cipher.init(javax.crypto.Cipher.DECRYPT_MODE, secretKey, iv);

      byte[] decoded = cipher.doFinal(encoded);

      result = new String(decoded, ENCODING);
    } catch (IOException | GeneralSecurityException ex) {
      throw new CipherException("could not decode string", ex);
    }

    return result;
  }

  @Override
  public String encode(String value) {
    return encode(key, value);
  }

  /**
   * Encrypts the given value with the provided key.
   *
   * @param plainKey key which is used for encoding
   * @param value plain text value to encrypt
   *
   * @return encrypted value
   */
  public String encode(char[] plainKey, String value) {
    String res = null;
    try {
      byte[] salt = new byte[SALT_LENGTH];

      random.nextBytes(salt);

      IvParameterSpec iv = new IvParameterSpec(salt);
      SecretKey secretKey = buildSecretKey(plainKey);
      javax.crypto.Cipher cipher = javax.crypto.Cipher.getInstance(CIPHER_TYPE);

      cipher.init(javax.crypto.Cipher.ENCRYPT_MODE, secretKey, iv);

      byte[] inputBytes = value.getBytes(ENCODING);
      byte[] encodedInput = cipher.doFinal(inputBytes);
      byte[] result = new byte[salt.length + encodedInput.length];

      System.arraycopy(salt, 0, result, 0, SALT_LENGTH);
      System.arraycopy(encodedInput, 0, result, SALT_LENGTH,
        result.length - SALT_LENGTH);
      res = new String(Base64.getEncoder().encode(result), ENCODING);
    } catch (IOException | GeneralSecurityException ex) {
      throw new CipherException("could not encode string", ex);
    }

    return res;
  }

  private SecretKey buildSecretKey(char[] plainKey) throws IOException, NoSuchAlgorithmException {
    byte[] raw = new String(plainKey).getBytes(ENCODING);
    MessageDigest digest = MessageDigest.getInstance(DIGEST_TYPE);

    raw = digest.digest(raw);
    raw = Arrays.copyOf(raw, KEY_LENGTH);

    return new SecretKeySpec(raw, KEY_TYPE);
  }

  private char[] loadKey(File cipherKeyFile) throws IOException {
    try (BufferedReader reader =  new BufferedReader(new FileReader(cipherKeyFile))) {
      String line = reader.readLine();

      return decode(KEY_BASE, line).toCharArray();
    }
  }

  private void storeKey(File cipherKeyFile) throws FileNotFoundException {
    String storeKey = encode(KEY_BASE, new String(key));
    try (PrintWriter output = new PrintWriter(cipherKeyFile)) {
      output.write(storeKey);
    }
  }
}
