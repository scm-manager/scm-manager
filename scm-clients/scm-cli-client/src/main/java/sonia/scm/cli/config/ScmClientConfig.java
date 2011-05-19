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

//~--- JDK imports ------------------------------------------------------------

import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 *
 * @author Sebastian Sdorra
 */
@XmlRootElement(name = "client-config")
@XmlAccessorType(XmlAccessType.FIELD)
public class ScmClientConfig
{

  /** Field description */
  public static final String DEFAULT_NAME = "default";

  /** Field description */
  private static volatile ScmClientConfig instance;

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   */
  private ScmClientConfig()
  {
    this.serverConfigMap = new HashMap<String, ServerConfig>();
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  public static ScmClientConfig getInstance()
  {
    if (instance == null)
    {
      synchronized (ScmClientConfig.class)
      {
        if (instance == null)
        {
          instance = load();
        }
      }
    }

    return instance;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  private static ScmClientConfig load()
  {
    ScmClientConfigFileHandler fileHandler = new ScmClientConfigFileHandler();
    ScmClientConfig config = fileHandler.read();

    if (config == null)
    {
      config = new ScmClientConfig();
    }

    config.setFileHandler(fileHandler);

    return config;
  }

  /**
   * Method description
   *
   */
  public void delete()
  {
    fileHandler.delete();
  }

  /**
   * Method description
   *
   */
  public void store()
  {
    fileHandler.write(this);
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param name
   *
   * @return
   */
  public ServerConfig getConfig(String name)
  {
    ServerConfig config = serverConfigMap.get(name);

    if (config == null)
    {
      config = new ServerConfig();
      serverConfigMap.put(name, config);
    }

    return config;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public ServerConfig getDefaultConfig()
  {
    return getConfig(DEFAULT_NAME);
  }

  //~--- set methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param fileHandler
   */
  private void setFileHandler(ScmClientConfigFileHandler fileHandler)
  {
    this.fileHandler = fileHandler;
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  @XmlTransient
  private ScmClientConfigFileHandler fileHandler;

  /** Field description */
  @XmlElement(name = "server-config")
  @XmlJavaTypeAdapter(XmlConfigAdapter.class)
  private Map<String, ServerConfig> serverConfigMap;
}
