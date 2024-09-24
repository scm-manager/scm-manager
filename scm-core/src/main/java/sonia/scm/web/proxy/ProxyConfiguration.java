/*
 * Copyright (c) 2020 - present Cloudogu GmbH
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

package sonia.scm.web.proxy;


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

/**
 *
 * @since 1.25
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "proxy-configuration")
public class ProxyConfiguration
{
  @XmlElement(name = "cache-enabled")
  private boolean cacheEnabled = true;

  @XmlElement(name = "copy-request-headers")
  private boolean copyRequestHeaders = true;

  @XmlElement(name = "copy-response-headers")
  private boolean copyResponseHeaders = true;

  @XmlElement(name = "exclude")
  @XmlElementWrapper(name = "request-header-excludes")
  private Set<String> requestHeaderExcludes;

  @XmlElement(name = "exclude")
  @XmlElementWrapper(name = "response-header-excludes")
  private Set<String> responseHeaderExcludes;

  private URL url;

  public ProxyConfiguration() {}

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

  
  @Override
  public int hashCode()
  {
    return Objects.hashCode(url, copyRequestHeaders, requestHeaderExcludes,
      copyResponseHeaders, responseHeaderExcludes, cacheEnabled);
  }

  
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


  
  @SuppressWarnings("unchecked")
  public Set<String> getRequestHeaderExcludes()
  {
    if (requestHeaderExcludes == null)
    {
      requestHeaderExcludes = Collections.EMPTY_SET;
    }

    return requestHeaderExcludes;
  }

  
  @SuppressWarnings("unchecked")
  public Set<String> getResponseHeaderExcludes()
  {
    if (responseHeaderExcludes == null)
    {
      responseHeaderExcludes = Collections.EMPTY_SET;
    }

    return responseHeaderExcludes;
  }

  
  public URL getUrl()
  {
    return url;
  }

  
  public boolean isCacheEnabled()
  {
    return cacheEnabled;
  }

  
  public boolean isCopyRequestHeaders()
  {
    return copyRequestHeaders;
  }

  
  public boolean isCopyResponseHeaders()
  {
    return copyResponseHeaders;
  }

}
