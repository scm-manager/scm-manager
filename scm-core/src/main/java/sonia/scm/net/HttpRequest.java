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



package sonia.scm.net;

//~--- non-JDK imports --------------------------------------------------------

import com.google.common.collect.Lists;

import sonia.scm.util.AssertUtil;

//~--- JDK imports ------------------------------------------------------------

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Http request which can be executed by the {@link HttpClient}.
 *
 * @author Sebastian Sdorra
 * @since 1.9
 */
public class HttpRequest
{

  /**
   * Constructs a new HttpRequest.
   *
   *
   * @param url url for the request
   */
  public HttpRequest(String url)
  {
    this.url = url;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Add a http header for the request.
   *
   *
   * @param name name of the request header
   * @param values values of the request header
   *
   * @return {@code this}
   */
  public HttpRequest addHeader(String name, String... values)
  {
    AssertUtil.assertIsNotNull(name);

    if (headers == null)
    {
      headers = new HashMap<String, List<String>>();
    }

    appendValues(headers, name, values);

    return this;
  }

  /**
   * Add a parameter to the request.
   *
   *
   * @param name name of the parameter
   * @param values values of the parameter
   *
   * @return {@code this}
   */
  public HttpRequest addParameters(String name, String... values)
  {
    AssertUtil.assertIsNotNull(name);

    if (parameters == null)
    {
      parameters = new HashMap<String, List<String>>();
    }

    appendValues(parameters, name, values);

    return this;
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Return a map with http headers used for the request.
   *
   *
   * @return map with http headers
   */
  public Map<String, List<String>> getHeaders()
  {
    return headers;
  }

  /**
   * Return a map with http parameters for the request.
   *
   *
   * @return map with http parameters
   */
  public Map<String, List<String>> getParameters()
  {
    return parameters;
  }

  /**
   * Returns the password for http basic authentication.
   *
   *
   * @return password for http basic authentication
   */
  public String getPassword()
  {
    return password;
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
   * Returns the username for http basic authentication.
   *
   *
   * @return username for http basic authentication
   */
  public String getUsername()
  {
    return username;
  }

  /**
   * Returns true if the request decodes gzip compression.
   *
   *
   * @return true if the request decodes gzip compression
   *
   * @since 1.14
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
   * @since 1.17
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
   * @since 1.17
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
   * @since 1.17
   */
  public boolean isIgnoreProxySettings()
  {
    return ignoreProxySettings;
  }

  //~--- set methods ----------------------------------------------------------

  /**
   * Enabled http basic authentication.
   *
   *
   * @param username username for http basic authentication
   * @param password password for http basic authentication
   *
   * @return {@code this}
   */
  public HttpRequest setBasicAuthentication(String username, String password)
  {
    this.username = username;
    this.password = password;

    return this;
  }

  /**
   * Enable or disabled gzip decoding. The default value is false.
   *
   *
   * @param decodeGZip true to enable gzip decoding
   *
   * @return {@code this}
   *
   * @since 1.14
   */
  public HttpRequest setDecodeGZip(boolean decodeGZip)
  {
    this.decodeGZip = decodeGZip;

    return this;
  }

  /**
   * Enable or disable certificate validation of ssl certificates. The default
   * value is false.
   *
   *
   * @param disableCertificateValidation true to disable certificate validation
   * @since 1.17
   */
  public void setDisableCertificateValidation(
    boolean disableCertificateValidation)
  {
    this.disableCertificateValidation = disableCertificateValidation;
  }

  /**
   * Enable or disable the validation of ssl hostnames. The default value is
   * false.
   *
   *
   * @param disableHostnameValidation true to disable ssl hostname validation
   * @since 1.17
   */
  public void setDisableHostnameValidation(boolean disableHostnameValidation)
  {
    this.disableHostnameValidation = disableHostnameValidation;
  }

  /**
   * Set http headers for the request.
   *
   *
   * @param headers headers for the request
   *
   * @return {@code this}
   */
  public HttpRequest setHeaders(Map<String, List<String>> headers)
  {
    this.headers = headers;

    return this;
  }

  /**
   * Ignore proxy settings. The default value is false.
   *
   *
   * @param ignoreProxySettings true to ignore proxy settings.
   * @since 1.17
   */
  public void setIgnoreProxySettings(boolean ignoreProxySettings)
  {
    this.ignoreProxySettings = ignoreProxySettings;
  }

  /**
   * Set http parameters for the request.
   *
   *
   * @param parameters parameters for the request
   *
   * @return {@code this}
   */
  public HttpRequest setParameters(Map<String, List<String>> parameters)
  {
    this.parameters = parameters;

    return this;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Append values to map.
   *
   *
   * @param map map to append values
   * @param name name of the key
   * @param values values to append
   */
  private void appendValues(Map<String, List<String>> map, String name,
    String[] values)
  {
    List<String> valueList = map.get(name);

    if (valueList == null)
    {
      valueList = Lists.newArrayList();
      map.put(name, valueList);
    }

    if (values != null)
    {
      valueList.addAll(Arrays.asList(values));
    }
  }

  //~--- fields ---------------------------------------------------------------

  /** map of request headers */
  private Map<String, List<String>> headers;

  /** ignore proxy settings */
  private boolean ignoreProxySettings = false;

  /** disable ssl hostname validation */
  private boolean disableHostnameValidation = false;

  /** disable ssl certificate validation */
  private boolean disableCertificateValidation = false;

  /** decode gzip */
  private boolean decodeGZip = false;

  /** map of parameters */
  private Map<String, List<String>> parameters;

  /** password for http basic authentication */
  private String password;

  /** url for the request */
  private String url;

  /** username for http basic authentication */
  private String username;
}
