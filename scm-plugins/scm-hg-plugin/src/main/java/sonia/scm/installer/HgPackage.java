/*
 * MIT License
 *
 * Copyright (c) 2020-present Cloudogu GmbH and Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
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
