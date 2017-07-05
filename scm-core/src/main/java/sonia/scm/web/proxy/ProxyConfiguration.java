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


package sonia.scm.web.proxy;

//~--- non-JDK imports --------------------------------------------------------

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;

import javax.xml.bind.annotation.*;
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
