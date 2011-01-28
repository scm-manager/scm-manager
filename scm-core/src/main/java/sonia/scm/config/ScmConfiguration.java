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



package sonia.scm.config;

//~--- non-JDK imports --------------------------------------------------------

import com.google.inject.Singleton;

//~--- JDK imports ------------------------------------------------------------

import java.io.File;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author Sebastian Sdorra
 */
@Singleton
@XmlRootElement(name = "scm-config")
@XmlAccessorType(XmlAccessType.FIELD)
public class ScmConfiguration
{

  /** Field description */
  public static final String DEFAULT_PLUGINURL =
    "http://plugins.scm-manager.org/plugins.xml.gz";

  /** Field description */
  public static final String PATH =
    "config".concat(File.separator).concat("config.xml");

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param other
   */
  public void load(ScmConfiguration other)
  {
    this.servername = other.servername;
    this.pluginUrl = other.pluginUrl;
    this.sslPort = other.sslPort;
    this.enableSSL = other.enableSSL;
    this.anonymousAccessEnabled = other.anonymousAccessEnabled;
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  public String getPluginUrl()
  {
    return pluginUrl;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public String getServername()
  {
    return servername;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public int getSslPort()
  {
    return sslPort;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public boolean isAnonymousAccessEnabled()
  {
    return anonymousAccessEnabled;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public boolean isEnableSSL()
  {
    return enableSSL;
  }

  //~--- set methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param anonymousAccessEnabled
   */
  public void setAnonymousAccessEnabled(boolean anonymousAccessEnabled)
  {
    this.anonymousAccessEnabled = anonymousAccessEnabled;
  }

  /**
   * Method description
   *
   *
   * @param enableSSL
   */
  public void setEnableSSL(boolean enableSSL)
  {
    this.enableSSL = enableSSL;
  }

  /**
   * Method description
   *
   *
   * @param pluginUrl
   */
  public void setPluginUrl(String pluginUrl)
  {
    this.pluginUrl = pluginUrl;
  }

  /**
   * Method description
   *
   *
   * @param servername
   */
  public void setServername(String servername)
  {
    this.servername = servername;
  }

  /**
   * Method description
   *
   *
   * @param sslPort
   */
  public void setSslPort(int sslPort)
  {
    this.sslPort = sslPort;
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  @XmlElement(name = "plugin-url")
  private String pluginUrl = DEFAULT_PLUGINURL;

  /** Field description */
  private String servername = "localhost";

  /** Field description */
  private boolean enableSSL = false;

  /** Field description */
  private int sslPort = 8181;

  /** Field description */
  private boolean anonymousAccessEnabled = false;
}
