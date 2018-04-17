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

//~--- non-JDK imports --------------------------------------------------------

import sonia.scm.security.KeyGenerator;
import sonia.scm.security.UUIDKeyGenerator;
import sonia.scm.util.Util;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.*;

//~--- JDK imports ------------------------------------------------------------

/**
 *
 * @author Sebastian Sdorra
 */
public class ScmClientConfigFileHandler
{

  /** Field description */
  public static final String DEFAULT_CONFIG_NAME = ".scm-cli-config.enc.xml";

  /** Field description */
  public static final String ENV_CONFIG_FILE = "SCM_CLI_CONFIG";

  //~--- constructors ---------------------------------------------------------

  private final KeyStore keyStore;
  private final KeyGenerator keyGenerator;
  private final File file;
  private final JAXBContext context;

  private final CipherStreamHandler cipherStreamHandler;


  /**
   * Constructs ...
   *
   */
  public ScmClientConfigFileHandler() {
    this(new PrefsKeyStore(), new UUIDKeyGenerator(), getDefaultConfigFile());
  }

  ScmClientConfigFileHandler(KeyStore keyStore, KeyGenerator keyGenerator, File file) {
    this.keyStore = keyStore;
    this.keyGenerator = keyGenerator;
    this.file = file;

    String key = keyStore.get();

    if (Util.isEmpty(key)) {
      key = keyGenerator.createKey();
      keyStore.set(key);
    }

    cipherStreamHandler = new WeakCipherStreamHandler(key.toCharArray());

    try {
      context = JAXBContext.newInstance(ScmClientConfig.class);
    } catch (JAXBException ex) {
      throw new ScmConfigException("could not create JAXBContext for ScmClientConfig", ex);
    }
  }

  //~--- methods --------------------------------------------------------------

  private static File getDefaultConfigFile() {
    String configPath = System.getenv(ENV_CONFIG_FILE);

    if (Util.isNotEmpty(configPath)){
      return new File(configPath);
    }
    return new File(System.getProperty("user.home"), DEFAULT_CONFIG_NAME);
  }

  /**
   * Method description
   *
   */
  public void delete() {
    if (file.exists() &&!file.delete()) {
      throw new ScmConfigException("could not delete config file");
    }

    keyStore.remove();
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public ScmClientConfig read() {
    ScmClientConfig config = null;

    if (file.exists()) {
      try (InputStream input = cipherStreamHandler.decrypt(new FileInputStream(file))) {
        Unmarshaller um = context.createUnmarshaller();
        config = (ScmClientConfig) um.unmarshal(input);
      } catch (IOException | JAXBException ex) {
        throw new ScmConfigException("could not read config file", ex);
      }
    }

    return config;
  }

  /**
   * Method description
   *
   *
   * @param config
   */
  public void write(ScmClientConfig config) {
    try (OutputStream output =  cipherStreamHandler.encrypt(new FileOutputStream(file))) {
      Marshaller m = context.createMarshaller();
      m.marshal(config, output);
    } catch (IOException | JAXBException ex) {
      throw new ScmConfigException("could not write config file", ex);
    }
  }
}
