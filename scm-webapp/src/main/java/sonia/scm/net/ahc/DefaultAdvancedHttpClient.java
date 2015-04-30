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

import com.google.common.base.Strings;
import com.google.common.collect.Multimap;
import com.google.common.io.Closeables;
import com.google.inject.Inject;

import org.apache.shiro.codec.Base64;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sonia.scm.config.ScmConfiguration;
import sonia.scm.net.Proxies;
import sonia.scm.net.TrustAllHostnameVerifier;
import sonia.scm.net.TrustAllTrustManager;
import sonia.scm.util.HttpUtil;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;
import java.io.OutputStream;

import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.ProtocolException;
import java.net.Proxy;
import java.net.SocketAddress;
import java.net.URL;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;

/**
 *
 * @author Sebastian Sdorra
 * @since 1.46
 */
public class DefaultAdvancedHttpClient extends AdvancedHttpClient
{

  /** Field description */
  public static final String CREDENTIAL_SEPARATOR = ":";

  /** Field description */
  public static final String HEADER_PROXY_AUTHORIZATION = "Proxy-Authorization";

  /** Field description */
  public static final String PREFIX_BASIC_AUTHENTICATION = "Basic ";

  /** Field description */
  public static final int TIMEOUT_CONNECTION = 30000;

  /** Field description */
  public static final int TIMEOUT_RAED = 1200000;

  /**
   * the logger for DefaultAdvancedHttpClient
   */
  private static final Logger logger =
    LoggerFactory.getLogger(DefaultAdvancedHttpClient.class);

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   *
   * @param configuration
   */
  @Inject
  public DefaultAdvancedHttpClient(ScmConfiguration configuration)
  {
    this.configuration = configuration;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param request
   *
   * @return
   *
   * @throws IOException
   */
  @Override
  protected AdvancedHttpResponse request(BaseHttpRequest<?> request)
    throws IOException
  {
    HttpURLConnection connection = openConnection(request,
                                     new URL(request.getUrl()));

    applyBaseSettings(request, connection);

    if (connection instanceof HttpsURLConnection)
    {
      applySSLSettings(request, (HttpsURLConnection) connection);
    }

    Content content = null;

    if (request instanceof AdvancedHttpRequestWithBody)
    {
      AdvancedHttpRequestWithBody ahrwb = (AdvancedHttpRequestWithBody) request;

      content = ahrwb.getContent();

      if (content != null)
      {
        content.prepare(ahrwb);
      }
      else
      {
        request.header(HttpUtil.HEADER_CONTENT_LENGTH, "0");
      }
    }
    else
    {
      request.header(HttpUtil.HEADER_CONTENT_LENGTH, "0");
    }

    applyHeaders(request, connection);

    if (content != null)
    {
      applyContent(connection, content);
    }

    return new DefaultAdvancedHttpResponse(connection,
      connection.getResponseCode(), connection.getResponseMessage());
  }

  private void appendProxyAuthentication(HttpURLConnection connection)
  {
    String username = configuration.getProxyUser();
    String password = configuration.getProxyPassword();

    if (!Strings.isNullOrEmpty(username) ||!Strings.isNullOrEmpty(password))
    {
      logger.debug("append proxy authentication header for user {}", username);

      String auth = username.concat(CREDENTIAL_SEPARATOR).concat(password);

      auth = Base64.encodeToString(auth.getBytes());
      connection.addRequestProperty(HEADER_PROXY_AUTHORIZATION,
        PREFIX_BASIC_AUTHENTICATION.concat(auth));
    }
  }

  private void applyBaseSettings(BaseHttpRequest<?> request,
    HttpURLConnection connection)
    throws ProtocolException
  {
    connection.setRequestMethod(request.getMethod());
    connection.setReadTimeout(TIMEOUT_RAED);
    connection.setConnectTimeout(TIMEOUT_CONNECTION);
  }

  private void applyContent(HttpURLConnection connection, Content content)
    throws IOException
  {
    connection.setDoOutput(true);

    OutputStream output = null;

    try
    {
      output = connection.getOutputStream();
      content.process(output);
    }
    finally
    {
      Closeables.close(output, true);
    }
  }

  private void applyHeaders(BaseHttpRequest<?> request,
    HttpURLConnection connection)
  {
    Multimap<String, String> headers = request.getHeaders();

    for (String key : headers.keySet())
    {
      for (String value : headers.get(key))
      {
        connection.setRequestProperty(key, value);
      }
    }
  }

  private void applySSLSettings(BaseHttpRequest<?> request,
    HttpsURLConnection connection)
  {
    if (request.isDisableCertificateValidation())
    {
      logger.trace("disable certificate validation");

      try
      {
        TrustManager[] trustAllCerts = new TrustManager[] {
                                         new TrustAllTrustManager() };
        SSLContext sc = SSLContext.getInstance("SSL");

        sc.init(null, trustAllCerts, new java.security.SecureRandom());
        connection.setSSLSocketFactory(sc.getSocketFactory());
      }
      catch (KeyManagementException ex)
      {
        logger.error("could not disable certificate validation", ex);
      }
      catch (NoSuchAlgorithmException ex)
      {
        logger.error("could not disable certificate validation", ex);
      }
    }

    if (request.isDisableHostnameValidation())
    {
      logger.trace("disable hostname validation");
      connection.setHostnameVerifier(new TrustAllHostnameVerifier());
    }
  }

  private HttpURLConnection openConnection(BaseHttpRequest<?> request, URL url)
    throws IOException
  {
    HttpURLConnection connection;

    if (isProxyEnabled(request))
    {
      connection = openProxyConnection(request, url);
      appendProxyAuthentication(connection);
    }
    else
    {
      if (request.isIgnoreProxySettings())
      {
        logger.trace("ignore proxy settings");
      }

      logger.debug("fetch {}", url.toExternalForm());

      connection = (HttpURLConnection) url.openConnection();
    }

    return connection;
  }

  private HttpURLConnection openProxyConnection(BaseHttpRequest<?> request,
    URL url)
    throws IOException
  {
    if (logger.isDebugEnabled())
    {
      logger.debug("fetch '{}' using proxy {}:{}", url.toExternalForm(),
        configuration.getProxyServer(), configuration.getProxyPort());
    }

    SocketAddress address =
      new InetSocketAddress(configuration.getProxyServer(),
        configuration.getProxyPort());

    return (HttpURLConnection) url.openConnection(new Proxy(Proxy.Type.HTTP,
      address));
  }

  //~--- get methods ----------------------------------------------------------

  private boolean isProxyEnabled(BaseHttpRequest<?> request)
  {
    return !request.isIgnoreProxySettings()
      && Proxies.isEnabled(configuration, request.getUrl());
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private final ScmConfiguration configuration;
}
