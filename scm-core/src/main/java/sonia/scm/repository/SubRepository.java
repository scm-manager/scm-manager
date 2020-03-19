/**
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

//~--- non-JDK imports --------------------------------------------------------

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

//~--- JDK imports ------------------------------------------------------------

/**
 * @since 1.10
 * @author Sebastian Sdorra
 */
@XmlRootElement(name = "subrepository")
@XmlAccessorType(XmlAccessType.FIELD)
public class SubRepository implements Serializable
{

  /** Field description */
  private static final long serialVersionUID = 6960065820378492531L;

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   */
  public SubRepository() {}

  /**
   * Constructs ...
   *
   *
   * @param repositoryUrl
   */
  public SubRepository(String repositoryUrl)
  {
    this.repositoryUrl = repositoryUrl;
  }

  /**
   * Constructs ...
   *
   *
   * @param revision
   * @param repositoryUrl
   */
  public SubRepository(String repositoryUrl, String revision)
  {
    this.repositoryUrl = repositoryUrl;
    this.revision = revision;
  }

  /**
   * Constructs ...
   *
   *
   * @param revision
   * @param repositoryUrl
   * @param browserUrl
   */
  public SubRepository(String repositoryUrl, String browserUrl, String revision)
  {
    this.repositoryUrl = repositoryUrl;
    this.browserUrl = browserUrl;
    this.revision = revision;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * {@inheritDoc}
   *
   *
   * @param obj
   *
   * @return
   */
  @Override
  public boolean equals(Object obj)
  {
    if (obj == null)
    {
      return false;
    }

    if (getClass() != obj.getClass())
    {
      return false;
    }

    SubRepository other = (SubRepository) obj;

    return Objects.equal(repositoryUrl, other.repositoryUrl)
           && Objects.equal(browserUrl, other.browserUrl)
           && Objects.equal(revision, other.revision);
  }

  /**
   * {@inheritDoc}
   *
   *
   * @return
   */
  @Override
  public int hashCode()
  {
    return Objects.hashCode(repositoryUrl, browserUrl, revision);
  }

  /**
   * {@inheritDoc}
   *
   *
   * @return
   */
  @Override
  public String toString()
  {
    //J-
    return MoreObjects.toStringHelper(this)
            .add("repositoryUrl", repositoryUrl)
            .add("browserUrl", browserUrl)
            .add("revision", revision)
            .toString();
    //J+
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  public String getBrowserUrl()
  {
    return browserUrl;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public String getRepositoryUrl()
  {
    return repositoryUrl;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public String getRevision()
  {
    return revision;
  }

  //~--- set methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param browserUrl
   */
  public void setBrowserUrl(String browserUrl)
  {
    this.browserUrl = browserUrl;
  }

  /**
   * Method description
   *
   *
   * @param repositoryUrl
   */
  public void setRepositoryUrl(String repositoryUrl)
  {
    this.repositoryUrl = repositoryUrl;
  }

  /**
   * Method description
   *
   *
   * @param revision
   */
  public void setRevision(String revision)
  {
    this.revision = revision;
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  @XmlElement(name = "browser-url")
  private String browserUrl;

  /** Field description */
  @XmlElement(name = "repository-url")
  private String repositoryUrl;

  /** Field description */
  private String revision;
}
