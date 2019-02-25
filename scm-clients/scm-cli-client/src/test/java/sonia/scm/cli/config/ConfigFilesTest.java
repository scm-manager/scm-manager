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
import com.google.common.io.Files;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import static org.junit.Assert.*;

public class ConfigFilesTest {

  @Rule
  public TemporaryFolder temporaryFolder = new TemporaryFolder();

  @Test
  public void testIsFormatV2() throws IOException {
    byte[] content = "The door was the way to... to... The Door was The Way".getBytes(Charsets.UTF_8);

    File fileV1 = temporaryFolder.newFile();
    Files.write(content, fileV1);

    assertFalse(ConfigFiles.isFormatV2(fileV1));

    File fileV2 = temporaryFolder.newFile();

    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    baos.write(ConfigFiles.VERSION_IDENTIFIER);
    baos.write(content);
    Files.write(baos.toByteArray(), fileV2);

    assertTrue(ConfigFiles.isFormatV2(fileV2));
  }

  @Test
  public void testParseV1() throws IOException {
    InMemorySecretKeyStore keyStore = createKeyStore();
    WeakCipherStreamHandler handler = new WeakCipherStreamHandler(keyStore.get());

    ScmClientConfig config = ClientConfigurationTests.createSampleConfig();
    File file = temporaryFolder.newFile();
    ClientConfigurationTests.encrypt(handler, config, file);

    config = ConfigFiles.parseV1(keyStore, file);
    ClientConfigurationTests.assertSampleConfig(config);
  }

  @Test
  public void storeAndParseV2() throws IOException {
    InMemorySecretKeyStore keyStore = new InMemorySecretKeyStore();
    ScmClientConfig config = ClientConfigurationTests.createSampleConfig();
    File file = temporaryFolder.newFile();

    ConfigFiles.store(keyStore, config, file);

    String key = keyStore.get();
    assertNotNull(key);

    config = ConfigFiles.parseV2(keyStore, file);
    ClientConfigurationTests.assertSampleConfig(config);
  }

  private InMemorySecretKeyStore createKeyStore() {
    String secretKey = new SecureRandomKeyGenerator().createKey();
    InMemorySecretKeyStore keyStore = new InMemorySecretKeyStore();
    keyStore.set(secretKey);
    return keyStore;
  }

}
