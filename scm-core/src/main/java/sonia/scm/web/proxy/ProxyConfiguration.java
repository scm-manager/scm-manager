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
    
package sonia.scm.web.proxy;

//~--- non-JDK imports --------------------------------------------------------

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElementWrapper;
import jakarta.xml.bind.annotation.XmlRootElement;

import java.net.URL;
import java.util.Collections;
import java.util.Set;

//~--- JDK imports ------------------------------------------------------------

/**
 *
 * @author Sebastian Sdorra
 * @since 1.25
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "proxy-configuration")
public class ProxyConfiguration
{

  /**
   * Constructs ...
   *
   */
  public ProxyConfiguration() {}

  /**
   * Constructs ...
   *
   *
   * @param url
   * @param copyRequestHeaders
   * @param requestHeaderExcludes
   * @param copyResponseHeaders
   * @param responseHeaderExcludes
   * @param cacheEnabled
   */
  public ProxyConfiguration(URL url, boolean copyRequestHeaders,
    Set<String> requestHeaderExcludes, boolean copyResponseHeaders,
    Set<String> responseHeaderExcludes, boolean cacheEnabled)
  {
    this.url = url;
    this.copyResponseHeaders = copyResponseHeaders;
    this.requestHeaderExcludes = requestHeaderExcludes;
    this.copyResponseHeaders = copyResponseHeaders;
    this.responseHeaderExcludes = responseHeaderExcludes;
    this.cacheEnabled = cacheEnabled;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
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

    final ProxyConfiguration other = (ProxyConfiguration) obj;

    return Objects.equal(url, other.url)
      && Objects.equal(copyRequestHeaders, other.copyRequestHeaders)
      && Objects.equal(requestHeaderExcludes, other.requestHeaderExcludes)
      && Objects.equal(copyResponseHeaders, other.copyResponseHeaders)
      && Objects.equal(responseHeaderExcludes, other.responseHeaderExcludes)
      && Objects.equal(cacheEnabled, other.cacheEnabled);
  }

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  public int hashCode()
  {
    return Objects.hashCode(url, copyRequestHeaders, requestHeaderExcludes,
      copyResponseHeaders, responseHeaderExcludes, cacheEnabled);
  }

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  public String toString()
  {
    //J-
    return MoreObjects.toStringHelper(this)
                  .add("url", url)
                  .add("copyRequestHeaders", copyRequestHeaders)
                  .add("requestHeaderExcludes", requestHeaderExcludes)
                  .add("copyResponseHeaders", copyResponseHeaders)
                  .add("responseHeaderExcludes", responseHeaderExcludes)
                  .add("cacheEnabled", cacheEnabled)
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
  @SuppressWarnings("unchecked")
  public Set<String> getRequestHeaderExcludes()
  {
    if (requestHeaderExcludes == null)
    {
      requestHeaderExcludes = Collections.EMPTY_SET;
    }

    return requestHeaderExcludes;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  @SuppressWarnings("unchecked")
  public Set<String> getResponseHeaderExcludes()
  {
    if (responseHeaderExcludes == null)
    {
      responseHeaderExcludes = Collections.EMPTY_SET;
    }

    return responseHeaderExcludes;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public URL getUrl()
  {
    return url;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public boolean isCacheEnabled()
  {
    return cacheEnabled;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public boolean isCopyRequestHeaders()
  {
    return copyRequestHeaders;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public boolean isCopyResponseHeaders()
  {
    return copyResponseHeaders;
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  @XmlElement(name = "cache-enabled")
  private boolean cacheEnabled = true;

  /** Field description */
  @XmlElement(name = "copy-request-headers")
  private boolean copyRequestHeaders = true;

  /** Field description */
  @XmlElement(name = "copy-response-headers")
  private boolean copyResponseHeaders = true;

  /** Field description */
  @XmlElement(name = "exclude")
  @XmlElementWrapper(name = "request-header-excludes")
  private Set<String> requestHeaderExcludes;

  /** Field description */
  @XmlElement(name = "exclude")
  @XmlElementWrapper(name = "response-header-excludes")
  private Set<String> responseHeaderExcludes;

  /** Field description */
  private URL url;
}
