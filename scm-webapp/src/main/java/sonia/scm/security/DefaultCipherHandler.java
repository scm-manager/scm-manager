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

import com.google.inject.Inject;

import com.google.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sonia.scm.SCMContextProvider;
import sonia.scm.util.IOUtil;

//~--- JDK imports ------------------------------------------------------------

import com.sun.jersey.core.util.Base64;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 *
 * @author Sebastian Sdorra
 */
@Singleton
public class DefaultCipherHandler implements CipherHandler
{

  /** Field description */
  public static final String CIPHER_TYPE = "AES/CTR/PKCS5PADDING";

  /** Field description */
  public static final String DIGEST_TYPE = "SHA-512";

  /** Field description */
  public static final String ENCODING = "UTF-8";

  /** Field description */
  private static final String CIPHERKEY_FILENAME = ".cipherkey";

  /** Field description */
  private static final char[] KEY_BASE = new char[]
  {
    '1', '4', '7', '3', 'F', '2', '1', 'E', '-', 'C', '4', 'C', '4', '-', '4',
    '6', 'C', 'C', '-', '8', '7', 'F', '6', '-', '7', 'B', '4', 'F', '0', '5',
    'E', 'C', '7', '7', '2', 'E'
  };

  /** Field description */
  private static final String KEY_TYPE = "AES";

  /** the logger for DefaultCipherHandler */
  private static final Logger logger =
    LoggerFactory.getLogger(DefaultCipherHandler.class);

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   *
   * @param context
   * @param keyGenerator
   *
   *
   * @throws IOException
   */
  @Inject
  public DefaultCipherHandler(SCMContextProvider context,
                              KeyGenerator keyGenerator)
          throws IOException
  {
    File configDirectory = new File(context.getBaseDirectory(), "config");

    IOUtil.mkdirs(configDirectory);
    cipherKeyFile = new File(configDirectory, CIPHERKEY_FILENAME);

    if (cipherKeyFile.exists())
    {
      loadKey();
    }
    else
    {
      key = keyGenerator.createKey().toCharArray();
      storeKey();
    }
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param value
   *
   * @return
   */
  @Override
  public String decode(String value)
  {
    return decode(key, value);
  }

  /**
   * Method description
   *
   *
   * @param plainKey
   * @param value
   *
   * @return
   */
  public String decode(char[] plainKey, String value)
  {
    String result = null;

    try
    {
      byte[] encodedInput = Base64.decode(value);
      byte[] salt = new byte[8];
      byte[] encoded = new byte[encodedInput.length - 8];

      System.arraycopy(encodedInput, 0, salt, 0, 8);
      System.arraycopy(encodedInput, 8, encoded, 0, encodedInput.length - 8);

      PBEParameterSpec parameterSpec = new PBEParameterSpec(salt, 20);
      SecretKey secretKey = buildSecretKey(plainKey);
      javax.crypto.Cipher cipher = javax.crypto.Cipher.getInstance(CIPHER_TYPE);

      cipher.init(javax.crypto.Cipher.DECRYPT_MODE, secretKey, parameterSpec);

      byte[] decoded = cipher.doFinal(encoded);

      result = new String(decoded, ENCODING);
    }
    catch (Exception ex)
    {
      logger.error("could not decode string", ex);

      throw new CipherException(ex);
    }

    return result;
  }

  /**
   * Method description
   *
   *
   * @param value
   *
   * @return
   */
  @Override
  public String encode(String value)
  {
    return encode(key, value);
  }

  /**
   * Method description
   *
   *
   * @param plainKey
   * @param value
   *
   * @return
   */
  public String encode(char[] plainKey, String value)
  {
    String res = null;

    try
    {
      byte[] salt = new byte[8];

      random.nextBytes(salt);

      IvParameterSpec iv = new IvParameterSpec(salt);
      SecretKey secretKey = buildSecretKey(key);
      javax.crypto.Cipher cipher = javax.crypto.Cipher.getInstance(CIPHER_TYPE);

      cipher.init(javax.crypto.Cipher.ENCRYPT_MODE, secretKey, iv);

      byte[] inputBytes = value.getBytes(ENCODING);
      byte[] encodedInput = cipher.doFinal(inputBytes);
      byte[] result = new byte[salt.length + encodedInput.length];

      System.arraycopy(salt, 0, result, 0, 8);
      System.arraycopy(encodedInput, 0, result, 8, result.length - 8);
      res = new String(Base64.encode(result), ENCODING);
    }
    catch (Exception ex)
    {
      logger.error("could not encode string", ex);

      throw new CipherException(ex);
    }

    return res;
  }

  /**
   * Method description
   *
   *
   * @param plainKey
   *
   * @return
   *
   * @throws NoSuchAlgorithmException
   * @throws UnsupportedEncodingException
   */
  private SecretKey buildSecretKey(char[] plainKey)
          throws UnsupportedEncodingException, NoSuchAlgorithmException
  {
    byte[] raw = new String(plainKey).getBytes(ENCODING);
    MessageDigest digest = MessageDigest.getInstance(DIGEST_TYPE);

    raw = digest.digest(raw);

    return new SecretKeySpec(raw, KEY_TYPE);
  }

  /**
   * Method description
   *
   *
   * @throws IOException
   */
  private void loadKey() throws IOException
  {
    BufferedReader reader = null;

    try
    {
      reader = new BufferedReader(new FileReader(cipherKeyFile));

      String line = reader.readLine();

      key = decode(KEY_BASE, line).toCharArray();
    }
    finally
    {
      IOUtil.close(reader);
    }
  }

  /**
   * Method description
   *
   *
   * @throws FileNotFoundException
   */
  private void storeKey() throws FileNotFoundException
  {
    String storeKey = encode(KEY_BASE, new String(key));
    PrintWriter output = null;

    try
    {
      output = new PrintWriter(cipherKeyFile);
      output.write(storeKey);
    }
    finally
    {
      IOUtil.close(output);
    }
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private File cipherKeyFile;

  /** Field description */
  private char[] key = null;

  /** Field description */
  private SecureRandom random = new SecureRandom();
}
