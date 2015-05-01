/**
 * Copyright (c) 2014, Sebastian Sdorra All rights reserved.
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



package sonia.scm.net.ahc;

//~--- non-JDK imports --------------------------------------------------------

import com.google.common.base.Charsets;
import com.google.common.base.Strings;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;

import org.apache.shiro.codec.Base64;

import sonia.scm.util.HttpUtil;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;

/**
 * Base class for http requests.
 *
 * @author Sebastian Sdorra
 * @param <T> request implementation
 * 
 * @since 1.46
 */
public abstract class BaseHttpRequest<T extends BaseHttpRequest>
{

  /**
   * Constructs a new {@link BaseHttpRequest}.
   *
   *
   * @param client http client
   * @param method http method
   * @param url url
   */
  public BaseHttpRequest(AdvancedHttpClient client, String method, String url)
  {
    this.client = client;
    this.method = method;
    this.url = url;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Executes the request and returns the http response.
   *
   *
   * @return http response
   *
   * @throws IOException
   */
  public AdvancedHttpResponse request() throws IOException 
  {
    return client.request(this);
  }

  /**
   * Implementing classes should return {@code this}.
   *
   *
   * @return request instance
   */
  protected abstract T self();

  /**
   * Enabled http basic authentication.
   *
   *
   * @param username username for http basic authentication
   * @param password password for http basic authentication
   *
   * @return http request instance
   */
  public T basicAuth(String username, String password)
  {
    String auth = Strings.nullToEmpty(username).concat(":").concat(
                    Strings.nullToEmpty(password));

    auth = Base64.encodeToString(auth.getBytes(Charsets.ISO_8859_1));
    headers.put("Authorization", "Basic ".concat(auth));

    return self();
  }

  /**
   * Enable or disabled gzip decoding. The default value is false.
   *
   *
   * @param decodeGZip true to enable gzip decoding
   *
   * @return request instance
   */
  public T decodeGZip(boolean decodeGZip)
  {
    this.decodeGZip = decodeGZip;

    return self();
  }

  /**
   * Enable or disable certificate validation of ssl certificates. The default
   * value is false.
   *
   *
   * @param disableCertificateValidation true to disable certificate validation
   * 
   * @return request instance
   */
  public T disableCertificateValidation(boolean disableCertificateValidation)
  {
    this.disableCertificateValidation = disableCertificateValidation;

    return self();
  }

  /**
   * Enable or disable the validation of ssl hostnames. The default value is
   * false.
   *
   *
   * @param disableHostnameValidation true to disable ssl hostname validation
   *
   * @return request instance
   */
  public T disableHostnameValidation(boolean disableHostnameValidation)
  {
    this.disableHostnameValidation = disableHostnameValidation;

    return self();
  }

  /**
   * Add http headers to request.
   *
   *
   * @param name header name
   * @param values header values
   *
   * @return request instance
   */
  public T header(String name, Object... values)
  {
    for (Object v : values)
    {
      headers.put(name, toString(v));
    }

    return self();
  }

  /**
   * Add http headers to request.
   *
   *
   * @param name header name
   * @param values header values
   *
   * @return request instance
   */
  public T headers(String name, Iterable<? extends Object> values)
  {
    for (Object v : values)
    {
      headers.put(name, toString(v));
    }

    return self();
  }

  /**
   * Ignore proxy settings. The default value is false.
   *
   *
   * @param ignoreProxySettings true to ignore proxy settings.
   *
   * @return request instance
   */
  public T ignoreProxySettings(boolean ignoreProxySettings)
  {
    this.ignoreProxySettings = ignoreProxySettings;

    return self();
  }

  /**
   * Appends a query parameter to the request.
   *
   *
   * @param name name of query parameter
   * @param values query parameter values
   *
   * @return request instance
   */
  public T queryString(String name, Object... values)
  {
    for (Object v : values)
    {
      appendQueryString(name, v);
    }

    return self();
  }

  /**
   * Appends a query parameter to the request.
   *
   *
   * @param name name of query parameter
   * @param values query parameter values
   *
   * @return request instance
   */
  public T queryStrings(String name, Iterable<? extends Object> values)
  {
    for (Object v : values)
    {
      appendQueryString(name, v);
    }

    return self();
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Return a map with http headers used for the request.
   *
   *
   * @return map with http headers
   */
  public Multimap<String, String> getHeaders()
  {
    return headers;
  }

  /**
   * Returns the http method for the request.
   *
   *
   * @return http method of request
   */
  public String getMethod()
  {
    return method;
  }

  /**
   * Returns the url for the request.
   *
   *
   * @return url of the request
   */
  public String getUrl()
  {
    return url;
  }

  /**
   * Returns true if the request decodes gzip compression.
   *
   *
   * @return true if the request decodes gzip compression
   */
  public boolean isDecodeGZip()
  {
    return decodeGZip;
  }

  /**
   * Returns true if the verification of ssl certificates is disabled.
   *
   *
   * @return true if certificate verification is disabled
   */
  public boolean isDisableCertificateValidation()
  {
    return disableCertificateValidation;
  }

  /**
   * Returns true if the ssl hostname validation is disabled.
   *
   *
   * @return true if the ssl hostname validation is disabled
   */
  public boolean isDisableHostnameValidation()
  {
    return disableHostnameValidation;
  }

  /**
   * Returns true if the proxy settings are ignored.
   *
   * 
   * @return true if the proxy settings are ignored
   */
  public boolean isIgnoreProxySettings()
  {
    return ignoreProxySettings;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Returns the value url encoded.
   *
   *
   * @param value value to encode
   *
   * @return encoded value
   */
  protected String encoded(Object value)
  {
    return HttpUtil.encode(Strings.nullToEmpty(toString(value)));
  }

  /**
   * Returns string representation of the given object or {@code null}, if the 
   * object is {@code null}.
   *
   *
   * @param object object
   *
   * @return string representation or {@code null}
   */
  protected String toString(Object object)
  {
    return (object != null)
      ? object.toString()
      : null;
  }

  private void appendQueryString(String name, Object value)
  {
    StringBuilder buffer = new StringBuilder();

    if (url.contains("?"))
    {
      buffer.append("&");
    }
    else
    {
      buffer.append("?");
    }

    buffer.append(encoded(name)).append("=").append(encoded(value));
    url = url.concat(buffer.toString());
  }

  //~--- fields ---------------------------------------------------------------

  /** http client */
  protected final AdvancedHttpClient client;

  /** http header */
  private final Multimap<String, String> headers = LinkedHashMultimap.create();

  /** http method */
  private final String method;

  /** ignore proxy settings */
  private boolean ignoreProxySettings = false;

  /** disable ssl hostname validation */
  private boolean disableHostnameValidation = false;

  /** disable ssl certificate validation */
  private boolean disableCertificateValidation = false;

  /** decode gzip */
  private boolean decodeGZip = false;

  /** url of request */
  private String url;
}
