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
import com.google.common.io.ByteStreams;

import javax.xml.bind.JAXB;
import java.io.*;

import static org.junit.Assert.assertEquals;

final class ClientConfigurationTests {

  private ClientConfigurationTests() {
  }

  static void testCipherStream(CipherStreamHandler cipherStreamHandler, String content) throws IOException {
    byte[] encrypted = encrypt(cipherStreamHandler, content);
    String decrypted = decrypt(cipherStreamHandler, encrypted);
    assertEquals(content, decrypted);
  }


  static byte[] encrypt(CipherStreamHandler cipherStreamHandler, String content) throws IOException {
    ByteArrayOutputStream output = new ByteArrayOutputStream();
    OutputStream encryptedOutput = cipherStreamHandler.encrypt(output);
    encryptedOutput.write(content.getBytes(Charsets.UTF_8));
    encryptedOutput.close();
    return output.toByteArray();
  }

  static String decrypt(CipherStreamHandler cipherStreamHandler, byte[] encrypted) throws IOException {
    InputStream input = new ByteArrayInputStream(encrypted);
    input = cipherStreamHandler.decrypt(input);
    byte[] decrypted = ByteStreams.toByteArray(input);
    input.close();

    return new String(decrypted, Charsets.UTF_8);
  }

  static void assertSampleConfig(ScmClientConfig config) {
    ServerConfig defaultConfig;
    defaultConfig = config.getDefaultConfig();

    assertEquals("http://localhost:8080/scm", defaultConfig.getServerUrl());
    assertEquals("admin", defaultConfig.getUsername());
    assertEquals("admin123", defaultConfig.getPassword());
  }

  static ScmClientConfig createSampleConfig() {
    ScmClientConfig config = new ScmClientConfig();
    ServerConfig defaultConfig = config.getDefaultConfig();
    defaultConfig.setServerUrl("http://localhost:8080/scm");
    defaultConfig.setUsername("admin");
    defaultConfig.setPassword("admin123");
    return config;
  }

  static void encrypt(CipherStreamHandler cipherStreamHandler, ScmClientConfig config, File file) throws IOException {
    try (OutputStream output = cipherStreamHandler.encrypt(new FileOutputStream(file))) {
      JAXB.marshal(config, output);
    }
  }

}
