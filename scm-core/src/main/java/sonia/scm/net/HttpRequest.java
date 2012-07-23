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

import sonia.scm.util.AssertUtil;

//~--- JDK imports ------------------------------------------------------------

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Sebastian Sdorra
 * @since 1.9
 */
public class HttpRequest
{

  /**
   * Constructs ...
   *
   *
   * @param url
   */
  public HttpRequest(String url)
  {
    this.url = url;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param name
   * @param values
   *
   * @return
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
   * Method description
   *
   *
   * @param name
   * @param values
   *
   * @return
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
   * Method description
   *
   *
   * @return
   */
  public Map<String, List<String>> getHeaders()
  {
    return headers;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public Map<String, List<String>> getParameters()
  {
    return parameters;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public String getPassword()
  {
    return password;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public String getUrl()
  {
    return url;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public String getUsername()
  {
    return username;
  }

  /**
   * Method description
   *
   *
   * @return
   *
   * @since 1.14
   */
  public boolean isDecodeGZip()
  {
    return decodeGZip;
  }

  /**
   * Method description
   *
   *
   * @return
   * @since 1.17
   */
  public boolean isDisableCertificateValidation()
  {
    return disableCertificateValidation;
  }

  /**
   * Method description
   *
   *
   * @return
   * @since 1.17
   */
  public boolean isDisableHostnameValidation()
  {
    return disableHostnameValidation;
  }

  /**
   * Method description
   *
   *
   * @return
   * @since 1.17
   */
  public boolean isIgnoreProxySettings()
  {
    return ignoreProxySettings;
  }

  //~--- set methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param username
   * @param password
   *
   * @return
   */
  public HttpRequest setBasicAuthentication(String username, String password)
  {
    this.username = username;
    this.password = password;

    return this;
  }

  /**
   * Method description
   *
   *
   * @param decodeGZip
   *
   * @return
   *
   * @since 1.14
   */
  public HttpRequest setDecodeGZip(boolean decodeGZip)
  {
    this.decodeGZip = decodeGZip;

    return this;
  }

  /**
   * Method description
   *
   *
   * @param disableCertificateValidation
   * @since 1.17
   */
  public void setDisableCertificateValidation(
          boolean disableCertificateValidation)
  {
    this.disableCertificateValidation = disableCertificateValidation;
  }

  /**
   * Method description
   *
   *
   * @param disableHostnameValidation
   * @since 1.17
   */
  public void setDisableHostnameValidation(boolean disableHostnameValidation)
  {
    this.disableHostnameValidation = disableHostnameValidation;
  }

  /**
   * Method description
   *
   *
   * @param headers
   *
   * @return
   */
  public HttpRequest setHeaders(Map<String, List<String>> headers)
  {
    this.headers = headers;

    return this;
  }

  /**
   * Method description
   *
   *
   * @param ignoreProxySettings
   * @since 1.17
   */
  public void setIgnoreProxySettings(boolean ignoreProxySettings)
  {
    this.ignoreProxySettings = ignoreProxySettings;
  }

  /**
   * Method description
   *
   *
   * @param parameters
   *
   * @return
   */
  public HttpRequest setParameters(Map<String, List<String>> parameters)
  {
    this.parameters = parameters;

    return this;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param map
   * @param name
   * @param values
   */
  private void appendValues(Map<String, List<String>> map, String name,
                            String[] values)
  {
    List<String> valueList = map.get(name);

    if (valueList == null)
    {
      valueList = new ArrayList<String>();
      map.put(name, valueList);
    }

    if (values != null)
    {
      valueList.addAll(Arrays.asList(values));
    }
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private Map<String, List<String>> headers;

  /** Field description */
  private boolean ignoreProxySettings = false;

  /** Field description */
  private boolean disableHostnameValidation = false;

  /** Field description */
  private boolean disableCertificateValidation = false;

  /** Field description */
  private boolean decodeGZip = false;

  /** Field description */
  private Map<String, List<String>> parameters;

  /** Field description */
  private String password;

  /** Field description */
  private String url;

  /** Field description */
  private String username;
}
