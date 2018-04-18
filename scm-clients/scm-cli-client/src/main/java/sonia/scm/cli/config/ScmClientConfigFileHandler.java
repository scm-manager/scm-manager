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

import sonia.scm.util.Util;

import java.io.File;
import java.io.IOException;

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
  private final File file;


  /**
   * Constructs a new ScmClientConfigFileHandler
   *
   */
  public ScmClientConfigFileHandler() {
    this(new PrefsKeyStore(), getDefaultConfigFile());
  }

  ScmClientConfigFileHandler(KeyStore keyStore,File file) {
    this.keyStore = keyStore;
    this.file = file;
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
      config = readFromFile();
    }

    return config;
  }

  private ScmClientConfig readFromFile() {
    ScmClientConfig config;
    try {
      if (ConfigFiles.isFormatV2(file)) {
        config = ConfigFiles.parseV2(keyStore, file);
      } else {
        config = ConfigFiles.parseV1(keyStore, file);
        ConfigFiles.store(keyStore, config, file);
      }
    } catch (IOException ex) {
      throw new ScmConfigException("could not read config file", ex);
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
    try {
      ConfigFiles.store(keyStore, config, file);
    } catch (IOException ex) {
      throw new ScmConfigException("could not write config file", ex);
    }
  }
}
