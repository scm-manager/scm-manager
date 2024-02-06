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


import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;

import java.io.Serializable;
import java.text.MessageFormat;

/**
 * Single failure of a {@link HealthCheck}.
 *
 * @since 1.36
 */
@XmlRootElement(name = "healthCheckFailure")
@XmlAccessorType(XmlAccessType.FIELD)
public final class HealthCheckFailure implements Serializable
{

  private static final String URL_TEMPLATE = "https://scm-manager.org/docs/{0}/en/user/repo/health-checks/%s";
  private static final String LATEST_VERSION = "latest";

  private String description;

  private String id;

  private String summary;

  private String url;

  /** Flag whether the url is a template or not */
  private boolean urlTemplated = false;

  /**
   * This constructor is only for JAXB.
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
    this(id, summary, (String) null, description);
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
    this.urlTemplated = false;
    this.description = description;
  }

  /**
   * Constructs ...
   *
   * @param id id of the failure
   * @param summary summary of the failure
   * @param urlTemplate template for the url of the failure (use {@link #urlForTitle(String)} to create this)
   * @param description description of the failure
   * @since 2.17.0
   */
  public HealthCheckFailure(String id, String summary, UrlTemplate urlTemplate,
    String description)
  {
    this.id = id;
    this.summary = summary;
    this.url = urlTemplate.get();
    this.urlTemplated = true;
    this.description = description;
  }

  /**
   * Use this to create {@link HealthCheckFailure} instances with an url for core health check failures.
   * @param title The title of the failure matching a health check documentation page.
   * @since 2.17.0
   */
  public static UrlTemplate urlForTitle(String title) {
    return new UrlTemplate(String.format(URL_TEMPLATE, title));
  }

  /**
   * Use this to create {@link HealthCheckFailure} instances with a custom url for core health check
   * failures. If this url can be customized with a concrete version of SCM-Manager, you can use <code>{0}</code>
   * as a placeholder for the version. This will be replaced later on.
   * @param urlTemplate The url for this failure.
   * @since 2.17.0
   */
  public static UrlTemplate templated(String urlTemplate) {
    return new UrlTemplate(urlTemplate);
  }

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

  @Override
  public int hashCode()
  {
    return Objects.hashCode(id, summary, url, description);
  }

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

  /**
   * Returns the description of this failure.
   */
  public String getDescription()
  {
    return description;
  }

  /**
   * Returns the id of this failure.
   */
  public String getId()
  {
    return id;
  }

  /**
   * Returns the summary of the failure.
   */
  public String getSummary()
  {
    return summary;
  }

  /**
   * Return the url of the failure. The url may potentially be templated. In the case you can get a
   * special url for an explicit version of SCM-Manager using {@link #getUrl(String)} whereas this
   * function will return a generic url for the {@value LATEST_VERSION} version.
   *
   * @return url of the failure
   */
  public String getUrl()
  {
    return getUrl(LATEST_VERSION);
  }

  /**
   * Return the url of the failure for a concrete version of SCM-Manager (given the url is templated).
   *
   * @param version The version of SCM-Manager to create the url for.
   * @return url of the failure
   * @since 2.17.0
   */
  public String getUrl(String version) {
    if (urlTemplated) {
      return MessageFormat.format(url, version);
    } else {
      return url;
    }
  }

  public static final class UrlTemplate {
    private final String url;

    private UrlTemplate(String url) {
      this.url = url;
    }

    private String get() {
      return url;
    }
  }
}
