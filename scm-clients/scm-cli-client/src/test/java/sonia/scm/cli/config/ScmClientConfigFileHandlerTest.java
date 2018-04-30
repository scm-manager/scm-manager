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

import com.google.common.io.Files;
import com.google.common.io.Resources;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import sonia.scm.security.UUIDKeyGenerator;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import static org.junit.Assert.*;

public class ScmClientConfigFileHandlerTest {

  @Rule
  public TemporaryFolder temporaryFolder = new TemporaryFolder();

  @Test
  public void testClientConfigFileHandler() throws IOException {
    File configFile = temporaryFolder.newFile();

    ScmClientConfigFileHandler handler = new ScmClientConfigFileHandler(
      new EncryptionKeyStoreWrapper(new InMemoryKeyStore()), configFile
    );

    ScmClientConfig config = new ScmClientConfig();
    ServerConfig defaultConfig = config.getDefaultConfig();
    defaultConfig.setServerUrl("http://localhost:8080/scm");
    defaultConfig.setUsername("scmadmin");
    defaultConfig.setPassword("admin123");
    handler.write(config);

    assertTrue(configFile.exists());

    config = handler.read();
    defaultConfig = config.getDefaultConfig();
    assertEquals("http://localhost:8080/scm", defaultConfig.getServerUrl());
    assertEquals("scmadmin", defaultConfig.getUsername());
    assertEquals("admin123", defaultConfig.getPassword());

    handler.delete();

    assertFalse(configFile.exists());
  }

  @Test
  public void testClientConfigFileHandlerWithOldConfiguration() throws IOException {
    File configFile = temporaryFolder.newFile();

    // old implementation has used uuids as keys
    String key = new UUIDKeyGenerator().createKey();

    WeakCipherStreamHandler weakCipherStreamHandler = new WeakCipherStreamHandler(key);
    ScmClientConfig clientConfig = ClientConfigurationTests.createSampleConfig();
    ClientConfigurationTests.encrypt(weakCipherStreamHandler, clientConfig, configFile);

    assertFalse(ConfigFiles.isFormatV2(configFile));

    KeyStore keyStore = new EncryptionKeyStoreWrapper(new InMemoryKeyStore());
    keyStore.set(key);

    ScmClientConfigFileHandler handler = new ScmClientConfigFileHandler(
      keyStore, configFile
    );

    ScmClientConfig config = handler.read();
    ClientConfigurationTests.assertSampleConfig(config);

    // ensure key has changed
    assertNotEquals(key, keyStore.get());

    // ensure config rewritten with v2
    assertTrue(ConfigFiles.isFormatV2(configFile));
  }

  @Test
  public void testClientConfigFileHandlerWithRealMigration() throws IOException {
    URL resource = Resources.getResource("sonia/scm/cli/config/scm-cli-config.enc.xml");
    byte[] bytes = Resources.toByteArray(resource);

    File configFile = temporaryFolder.newFile();
    Files.write(bytes, configFile);

    String key = "358e018a-0c3c-4339-8266-3874e597305f";
    KeyStore keyStore = new EncryptionKeyStoreWrapper(new InMemoryKeyStore());
    keyStore.set(key);

    ScmClientConfigFileHandler handler = new ScmClientConfigFileHandler(
      keyStore, configFile
    );

    ScmClientConfig config = handler.read();
    ServerConfig defaultConfig = config.getDefaultConfig();
    assertEquals("http://hitchhicker.com/scm", defaultConfig.getServerUrl());
    assertEquals("tricia", defaultConfig.getUsername());
    assertEquals("trillian123", defaultConfig.getPassword());

    // ensure key has changed
    assertNotEquals(key, keyStore.get());

    // ensure config rewritten with v2
    assertTrue(ConfigFiles.isFormatV2(configFile));
  }
}
