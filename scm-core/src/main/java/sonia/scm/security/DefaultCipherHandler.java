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

package sonia.scm.security;


import com.google.common.annotations.VisibleForTesting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.SCMContextProvider;
import sonia.scm.util.IOUtil;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;

/**
 * Default implementation of the {@link CipherHandler}, which uses AES for
 * encryption and decryption.
 *
 * @since 1.7
 */
public class DefaultCipherHandler implements CipherHandler {

  /**
   * Cipher type used before v2.
   * @see <a href="https://github.com/scm-manager/scm-manager/issues/1110">Issue 1110</a>
   */
  public static final String OLD_CIPHER_TYPE = "AES/CTR/NoPadding";

  /** used cipher type for format v2 */
  public static final String CIPHER_TYPE = "AES/GCM/NoPadding";

  /** prefix to detect new format */
  public static final String PREFIX_FORMAT_V2 = "v2:";

  public static final String DIGEST_TYPE = "SHA-512";

  public static final Charset ENCODING = StandardCharsets.UTF_8;

  public static final int KEY_LENGTH = 16;

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

  private static final Logger LOG = LoggerFactory.getLogger(DefaultCipherHandler.class);

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


  @Override
  public String decode(String value) {
    return decode(key, value);
  }

  /**
   * Decodes the given value with the provided key.
   *
   * @param plainKey key which is used for decoding
   * @param value encrypted value
   */
  public String decode(char[] plainKey, String value) {
    Base64.Decoder decoder = Base64.getUrlDecoder();
    try {
      return decode(plainKey, value, decoder);
    } catch (IllegalArgumentException e) {
      return decode(plainKey, value, Base64.getDecoder());
    }
  }

  private String decode(char[] plainKey, String value, Base64.Decoder decoder) {
    CipherFactory cipherFactory = oldCipherFactor;
    if (value.startsWith(PREFIX_FORMAT_V2)) {
      cipherFactory = v2CipherFactor;
      value = value.substring(PREFIX_FORMAT_V2.length());
    } else {
      LOG.warn("found encrypted data in old format, the data should be stored again to ensure the new format is used");
    }

    try {
      byte[] encodedInput = decoder.decode(value);
      byte[] salt = new byte[SALT_LENGTH];
      byte[] encoded = new byte[encodedInput.length - SALT_LENGTH];

      System.arraycopy(encodedInput, 0, salt, 0, SALT_LENGTH);
      System.arraycopy(encodedInput, SALT_LENGTH, encoded, 0, encodedInput.length - SALT_LENGTH);

      Cipher cipher = cipherFactory.create(plainKey, salt, Cipher.DECRYPT_MODE);
      byte[] decoded = cipher.doFinal(encoded);
      return new String(decoded, ENCODING);
    } catch (GeneralSecurityException ex) {
      throw new CipherException("could not decode string", ex);
    }
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
   */
  public String encode(char[] plainKey, String value) {
    String res = null;
    try {
      byte[] salt = new byte[SALT_LENGTH];

      random.nextBytes(salt);

      Cipher cipher = v2CipherFactor.create(plainKey, salt, Cipher.ENCRYPT_MODE);

      byte[] inputBytes = value.getBytes(ENCODING);
      byte[] encodedInput = cipher.doFinal(inputBytes);
      byte[] result = new byte[salt.length + encodedInput.length];

      System.arraycopy(salt, 0, result, 0, SALT_LENGTH);
      System.arraycopy(encodedInput, 0, result, SALT_LENGTH,
        result.length - SALT_LENGTH);
      res = PREFIX_FORMAT_V2  + new String(Base64.getUrlEncoder().encode(result), ENCODING);
    } catch (GeneralSecurityException ex) {
      throw new CipherException("could not encode string", ex);
    }

    return res;
  }

  private SecretKey buildSecretKey(char[] plainKey) throws NoSuchAlgorithmException {
    byte[] raw = new String(plainKey).getBytes(ENCODING);
    MessageDigest digest = MessageDigest.getInstance(DIGEST_TYPE);

    raw = digest.digest(raw);
    raw = Arrays.copyOf(raw, KEY_LENGTH);

    return new SecretKeySpec(raw, KEY_TYPE);
  }

  private char[] loadKey(File cipherKeyFile) throws IOException {
    try (BufferedReader reader =  new BufferedReader(new FileReader(cipherKeyFile))) {
      String encodedKey = reader.readLine();

      char[] decodedKey = decode(KEY_BASE, encodedKey).toCharArray();

      // rewrite key in new format, if the stored key uses the old format
      if (!encodedKey.startsWith(PREFIX_FORMAT_V2)) {
        LOG.info("found default key in old format, rewrite with new format");
        storeKey(cipherKeyFile, decodedKey);
      }

      return decodedKey;
    }
  }

  private void storeKey(File cipherKeyFile) throws FileNotFoundException {
    storeKey(cipherKeyFile, key);
  }

  private void storeKey(File cipherKeyFile, char[] key) throws FileNotFoundException {
    String storeKey = encode(KEY_BASE, new String(key));
    try (PrintWriter output = new PrintWriter(cipherKeyFile)) {
      output.write(storeKey);
    }
  }

  @FunctionalInterface
  private interface CipherFactory {
    Cipher create(char[] plainKey, byte[] salt, int mode) throws GeneralSecurityException;

  }

  private final CipherFactory v2CipherFactor = (char[] plainKey, byte[] salt, int mode) -> {
    Cipher cipher = Cipher.getInstance(CIPHER_TYPE);
    SecretKey secretKey = buildSecretKey(plainKey);
    GCMParameterSpec parameterSpec = new GCMParameterSpec(128, salt);
    cipher.init(mode, secretKey, parameterSpec);
    return cipher;
  };

  private final CipherFactory oldCipherFactor = (char[] plainKey, byte[] salt, int mode) -> {
    Cipher cipher = Cipher.getInstance(OLD_CIPHER_TYPE);
    SecretKey secretKey = buildSecretKey(plainKey);
    IvParameterSpec iv = new IvParameterSpec(salt);
    cipher.init(Cipher.DECRYPT_MODE, secretKey, iv);
    return cipher;
  };
}
