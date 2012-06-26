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



package sonia.scm.repository;

//~--- non-JDK imports --------------------------------------------------------

import com.google.common.base.Objects;

//~--- JDK imports ------------------------------------------------------------

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @since 1.10
 * @author Sebastian Sdorra
 */
@XmlRootElement(name = "subrepository")
@XmlAccessorType(XmlAccessType.FIELD)
public class SubRepository
{

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
    return Objects.toStringHelper(this)
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
