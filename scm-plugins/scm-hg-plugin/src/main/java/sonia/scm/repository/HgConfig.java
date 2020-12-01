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

package sonia.scm.repository;


import sonia.scm.util.Util;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;


/**
 *
 * @author Sebastian Sdorra
 */
@XmlRootElement(name = "config")
public class HgConfig extends RepositoryConfig {

  public static final String PERMISSION = "hg";

  @Override
  @XmlTransient // Only for permission checks, don't serialize to XML
  public String getId() {
    // Don't change this without migrating SCM permission configuration!
    return PERMISSION;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public String getEncoding()
  {
    return encoding;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public String getHgBinary()
  {
    return hgBinary;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public String getPythonBinary()
  {
    return pythonBinary;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public String getPythonPath()
  {
    return pythonPath;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public boolean isShowRevisionInId()
  {
    return showRevisionInId;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public boolean isUseOptimizedBytecode()
  {
    return useOptimizedBytecode;
  }

  public boolean isEnableHttpPostArgs() {
    return enableHttpPostArgs;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  public boolean isValid()
  {
    return super.isValid() && Util.isNotEmpty(hgBinary)
      && Util.isNotEmpty(pythonBinary);
  }

  //~--- set methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param encoding
   */
  public void setEncoding(String encoding)
  {
    this.encoding = encoding;
  }

  /**
   * Method description
   *
   *
   * @param hgBinary
   */
  public void setHgBinary(String hgBinary)
  {
    this.hgBinary = hgBinary;
  }

  /**
   * Method description
   *
   *
   * @param pythonBinary
   */
  public void setPythonBinary(String pythonBinary)
  {
    this.pythonBinary = pythonBinary;
  }

  /**
   * Method description
   *
   *
   * @param pythonPath
   */
  public void setPythonPath(String pythonPath)
  {
    this.pythonPath = pythonPath;
  }

  /**
   * Method description
   *
   *
   * @param showRevisionInId
   */
  public void setShowRevisionInId(boolean showRevisionInId)
  {
    this.showRevisionInId = showRevisionInId;
  }

  public void setEnableHttpPostArgs(boolean enableHttpPostArgs) {
    this.enableHttpPostArgs = enableHttpPostArgs;
  }

  /**
   * Method description
   *
   *
   * @param useOptimizedBytecode
   */
  public void setUseOptimizedBytecode(boolean useOptimizedBytecode)
  {
    this.useOptimizedBytecode = useOptimizedBytecode;
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private String encoding = "UTF-8";

  /** Field description */
  private String hgBinary;

  /** Field description */
  private String pythonBinary;

  /** Field description */
  private String pythonPath = "";

  /** Field description */
  private boolean useOptimizedBytecode = false;

  /** Field description */
  private boolean showRevisionInId = false;

  private boolean enableHttpPostArgs = false;

}
