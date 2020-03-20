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

//~--- non-JDK imports --------------------------------------------------------

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

//~--- JDK imports ------------------------------------------------------------

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
