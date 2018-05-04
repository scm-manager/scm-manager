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
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import sonia.scm.security.KeyGenerator;

import javax.xml.bind.JAXB;
import java.io.*;
import java.util.Arrays;

/**
 * Util methods for configuration files.
 *
 * @author Sebastian Sdorra
 * @since 1.60
 */
final class ConfigFiles {

  private static final KeyGenerator keyGenerator = new SecureRandomKeyGenerator();

  // SCM Config Version 2
  @VisibleForTesting
  static final byte[] VERSION_IDENTIFIER = "SCV2".getBytes(Charsets.US_ASCII);

  private ConfigFiles() {
  }

  /**
   * Returns {@code true} if the file is encrypted with the v2 format.
   *
   * @param file configuration file
   *
   * @return {@code true} for format v2
   *
   * @throws IOException
   */
  static boolean isFormatV2(File file) throws IOException {
    try (InputStream input = new FileInputStream(file)) {
      byte[] bytes = new byte[VERSION_IDENTIFIER.length];
      input.read(bytes);
      return Arrays.equals(VERSION_IDENTIFIER, bytes);
    }
  }

  /**
   * Decrypt and parse v1 configuration file.
   *
   * @param secretKeyStore key store
   * @param file configuration file
   *
   * @return client configuration
   *
   * @throws IOException
   */
  static ScmClientConfig parseV1(SecretKeyStore secretKeyStore, File file) throws IOException {
    String secretKey = secretKey(secretKeyStore);
    CipherStreamHandler cipherStreamHandler = new WeakCipherStreamHandler(secretKey);
    return decrypt(cipherStreamHandler, new FileInputStream(file));
  }

   /**
   * Decrypt and parse v12configuration file.
   *
   * @param secretKeyStore key store
   * @param file configuration file
   *
   * @return client configuration
   *
   * @throws IOException
   */
  static ScmClientConfig parseV2(SecretKeyStore secretKeyStore, File file) throws IOException {
    String secretKey = secretKey(secretKeyStore);
    CipherStreamHandler cipherStreamHandler = new AesCipherStreamHandler(secretKey);
    try (InputStream input = new FileInputStream(file)) {
      input.skip(VERSION_IDENTIFIER.length);
      return decrypt(cipherStreamHandler, input);
    }
  }

  /**
   * Store encrypt and write the configuration to the given file.
   * Note the method uses always the latest available format.
   *
   * @param secretKeyStore key store
   * @param config configuration
   * @param file configuration file
   *
   * @throws IOException
   */
  static void store(SecretKeyStore secretKeyStore, ScmClientConfig config, File file) throws IOException {
    String secretKey = keyGenerator.createKey();
    CipherStreamHandler cipherStreamHandler = new AesCipherStreamHandler(secretKey);
    try (OutputStream output = new FileOutputStream(file)) {
      output.write(VERSION_IDENTIFIER);
      encrypt(cipherStreamHandler, output, config);
    }
    secretKeyStore.set(secretKey);
  }

  private static String secretKey(SecretKeyStore secretKeyStore) {
    String secretKey = secretKeyStore.get();
    Preconditions.checkState(!Strings.isNullOrEmpty(secretKey), "no stored secret key found");
    return secretKey;
  }

  private static ScmClientConfig decrypt(CipherStreamHandler cipherStreamHandler, InputStream input) throws IOException {
    try ( InputStream decryptedInputStream = cipherStreamHandler.decrypt(input) ) {
      return JAXB.unmarshal(decryptedInputStream, ScmClientConfig.class);
    }
  }

  private static void encrypt(CipherStreamHandler cipherStreamHandler, OutputStream output, ScmClientConfig clientConfig) throws IOException {
    try ( OutputStream encryptedOutputStream = cipherStreamHandler.encrypt(output) ) {
      JAXB.marshal(clientConfig, encryptedOutputStream);
    }
  }

}
