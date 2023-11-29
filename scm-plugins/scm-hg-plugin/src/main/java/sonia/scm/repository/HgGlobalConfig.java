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


import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlTransient;
import sonia.scm.auditlog.AuditEntry;
import sonia.scm.util.Util;


/**
 *
 * @author Sebastian Sdorra
 */
@XmlRootElement(name = "config")
@AuditEntry(labels = {"hg", "config"})
public class HgGlobalConfig extends RepositoryConfig {

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
  public boolean isShowRevisionInId()
  {
    return showRevisionInId;
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
    return super.isValid() && Util.isNotEmpty(hgBinary);
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
   * @param showRevisionInId
   */
  public void setShowRevisionInId(boolean showRevisionInId)
  {
    this.showRevisionInId = showRevisionInId;
  }

  public void setEnableHttpPostArgs(boolean enableHttpPostArgs) {
    this.enableHttpPostArgs = enableHttpPostArgs;
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private String encoding = "UTF-8";

  /** Field description */
  private String hgBinary;

  /** Field description */
  private boolean showRevisionInId = false;

  private boolean enableHttpPostArgs = false;

}
