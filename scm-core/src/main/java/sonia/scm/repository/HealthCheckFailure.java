/**
 * Copyright (c) 2010, Sebastian Sdorra All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer. 2. Redistributions in
 * binary form must reproduce the above copyright notice, this list of
 * conditions and the following disclaimer in the documentation and/or other
 * materials provided with the distribution. 3. Neither the name of SCM-Manager;
 * nor the names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * http://bitbucket.org/sdorra/scm-manager
 *
 */



package sonia.scm.repository;

//~--- non-JDK imports --------------------------------------------------------

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;

//~--- JDK imports ------------------------------------------------------------

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Single failure of a {@link HealthCheck}.
 *
 * @author Sebastian Sdorra
 * @since 1.36
 */
@XmlRootElement(name = "healthCheckFailure")
@XmlAccessorType(XmlAccessType.FIELD)
public final class HealthCheckFailure
{

  /**
   * Constructs a new {@link HealthCheckFailure}. 
   * This constructor is only for JAXB.
   *
   */
  HealthCheckFailure() {}

  /**
   * Constructs a new {@link HealthCheckFailure}. 
   *
   * @param id id of the failure
   * @param summary summary of the failure
   * @param description description of the failure
   */
  public HealthCheckFailure(String id, String summary, String description)
  {
    this(id, summary, null, description);
  }

  /**
   * Constructs ...
   *
   * @param id id of the failure
   * @param summary summary of the failure
   * @param url url of the failure
   * @param description description of the failure
   */
  public HealthCheckFailure(String id, String summary, String url,
    String description)
  {
    this.id = id;
    this.summary = summary;
    this.url = url;
    this.description = description;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * {@inheritDoc}
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

    final HealthCheckFailure other = (HealthCheckFailure) obj;

    //J-
    return Objects.equal(id, other.id) 
      && Objects.equal(summary, other.summary)
      && Objects.equal(url, other.url)
      && Objects.equal(description, other.description);
    //J+
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int hashCode()
  {
    return Objects.hashCode(id, summary, url, description);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String toString()
  {
    //J-
    return MoreObjects.toStringHelper(this)
                      .add("id", id)
                      .add("summary", summary)
                      .add("url", url)
                      .add("description", description)
                      .toString();
    //J+
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Returns the description of this failure.
   *
   * @return description of this failure
   */
  public String getDescription()
  {
    return description;
  }

  /**
   * Returns the id of this failure.
   *
   * @return id of this failure
   */
  public String getId()
  {
    return id;
  }

  /**
   * Returns the summary of the failure.
   *
   * @return summary of the failure
   */
  public String getSummary()
  {
    return summary;
  }

  /**
   * Return the url of the failure.
   *
   * @return url of the failure
   */
  public String getUrl()
  {
    return url;
  }

  //~--- fields ---------------------------------------------------------------

  /** description of failure */
  private String description;

  /** id of failure */
  private String id;

  /** summary of failure */
  private String summary;

  /** url of failure */
  private String url;
}
