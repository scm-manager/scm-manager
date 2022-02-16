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

package sonia.scm.net.ahc;

//~--- non-JDK imports --------------------------------------------------------

import com.google.common.base.Strings;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import org.apache.shiro.codec.Base64;
import sonia.scm.util.HttpUtil;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

//~--- JDK imports ------------------------------------------------------------

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

    auth = Base64.encodeToString(auth.getBytes(StandardCharsets.ISO_8859_1));
    headers.put("Authorization", "Basic ".concat(auth));

    return self();
  }

  /**
   * Enable authentication with a bearer token.
   * @param bearerToken bearer token
   * @return http request instance
   * @since 2.28.0
   */
  public T bearerAuth(String bearerToken) {
    headers.put("Authorization", "Bearer ".concat(bearerToken));

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

  /**
   * Sets the kind of span for tracing api.
   *
   * @param spanKind kind of span
   * @return request instance
   *
   * @since 2.9.0
   */
  public T spanKind(String spanKind) {
    this.spanKind = spanKind;
    return self();
  }

  /**
   * Sets the response codes which should be traced as successful.
   *
   * Example: If 400 is set as {@link #acceptedStatusCodes} then all requests
   * which get a response with status code 400 will be traced as successful (not failed) request
   *
   * @param codes status codes which should be traced as successful
   * @return request instance
   *
   * @since 2.10.0
   */
  public T acceptStatusCodes(int... codes) {
    this.acceptedStatusCodes = codes;
    return self();
  }

  /**
   * Disables tracing for the request.
   * This should only be done for internal requests.
   *
   * @return request instance
   */
  public T disableTracing() {
    this.spanKind = null;
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
   * Returns the kind of span which is used for the trace api.
   *
   * @return kind of span
   *
   * @since 2.9.0
   */
  public String getSpanKind() {
    return spanKind;
  }


  /**
   * Returns the response codes which are accepted as successful by tracer.
   *
   * @return codes
   *
   * @since 2.10.0
   */
  public int[] getAcceptedStatus() {
    return acceptedStatusCodes;
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

  /** kind of span for trace api */
  private String spanKind = "HTTP Request";

  /** codes which will be marked as successful by tracer */
  private int[] acceptedStatusCodes = new int[]{};
}
