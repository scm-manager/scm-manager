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



package sonia.scm.installer;

//~--- non-JDK imports --------------------------------------------------------

import sonia.scm.repository.HgConfig;

//~--- JDK imports ------------------------------------------------------------

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author Sebastian Sdorra
 */
@XmlRootElement(name = "package")
@XmlAccessorType(XmlAccessType.FIELD)
public class HgPackage
{

  /**
   * Method description
   *
   *
   * @return
   */
  public String getArch()
  {
    return arch;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public HgConfig getHgConfigTemplate()
  {
    return hgConfigTemplate;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public String getHgVersion()
  {
    return hgVersion;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public String getId()
  {
    return id;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public String getPlatform()
  {
    return platform;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public String getPythonVersion()
  {
    return pythonVersion;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public long getSize()
  {
    return size;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public String getUrl()
  {
    return url;
  }

  //~--- set methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param arch
   */
  public void setArch(String arch)
  {
    this.arch = arch;
  }

  /**
   * Method description
   *
   *
   * @param hgConfigTemplate
   */
  public void setHgConfigTemplate(HgConfig hgConfigTemplate)
  {
    this.hgConfigTemplate = hgConfigTemplate;
  }

  /**
   * Method description
   *
   *
   * @param hgVersion
   */
  public void setHgVersion(String hgVersion)
  {
    this.hgVersion = hgVersion;
  }

  /**
   * Method description
   *
   *
   * @param id
   */
  public void setId(String id)
  {
    this.id = id;
  }

  /**
   * Method description
   *
   *
   * @param platform
   */
  public void setPlatform(String platform)
  {
    this.platform = platform;
  }

  /**
   * Method description
   *
   *
   * @param pythonVersion
   */
  public void setPythonVersion(String pythonVersion)
  {
    this.pythonVersion = pythonVersion;
  }

  /**
   * Method description
   *
   *
   * @param size
   */
  public void setSize(long size)
  {
    this.size = size;
  }

  /**
   * Method description
   *
   *
   * @param url
   */
  public void setUrl(String url)
  {
    this.url = url;
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private String arch;

  /** Field description */
  @XmlElement(name = "hg-config-template")
  private HgConfig hgConfigTemplate;

  /** Field description */
  @XmlElement(name = "hg-version")
  private String hgVersion;

  /** Field description */
  private String id;

  /** Field description */
  private String platform;

  /** Field description */
  @XmlElement(name = "python-version")
  private String pythonVersion;

  /** Field description */
  private long size;

  /** Field description */
  private String url;
}
