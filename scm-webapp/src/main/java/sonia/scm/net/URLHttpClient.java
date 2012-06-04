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

import com.google.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sonia.scm.SCMContextProvider;
import sonia.scm.config.ScmConfiguration;
import sonia.scm.util.IOUtil;
import sonia.scm.util.Util;

//~--- JDK imports ------------------------------------------------------------

import com.sun.jersey.core.util.Base64;

import java.io.IOException;
import java.io.OutputStreamWriter;

import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.SocketAddress;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;

/**
 *
 * @author Sebastian Sdorra
 */
public class URLHttpClient implements HttpClient
{

  /** Field description */
  public static final String CREDENTIAL_SEPARATOR = ":";

  /** Field description */
  public static final String ENCODING = "UTF-8";

  /** Field description */
  public static final String HEADER_ACCEPT_ENCODING = "Accept-Encoding";

  /** Field description */
  public static final String HEADER_ACCEPT_ENCODING_VALUE = "gzip";

  /** Field description */
  public static final String HEADER_AUTHORIZATION = "Authorization";

  /** Field description */
  public static final String HEADER_PROXY_AUTHORIZATION = "Proxy-Authorization";

  /** Field description */
  public static final String HEADER_USERAGENT = "User-Agent";

  /** Field description */
  public static final String HEADER_USERAGENT_VALUE = "SCM-Manager ";

  /** Field description */
  public static final String METHOD_POST = "POST";

  /** Field description */
  public static final String PREFIX_BASIC_AUTHENTICATION = "Basic ";

  /** Field description */
  public static final int TIMEOUT_CONNECTION = 30000;

  /** Field description */
  public static final int TIMEOUT_RAED = 1200000;

  /** the logger for URLHttpClient */
  private static final Logger logger =
    LoggerFactory.getLogger(URLHttpClient.class);

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   *
   *
   * @param context
   * @param configuration
   */
  @Inject
  public URLHttpClient(SCMContextProvider context,
                       ScmConfiguration configuration)
  {
    this.context = context;
    this.configuration = configuration;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param url
   *
   * @return
   *
   * @throws IOException
   */
  @Override
  public HttpResponse post(String url) throws IOException
  {
    HttpURLConnection connection = (HttpURLConnection) openConnection(null,
                                     url);

    connection.setRequestMethod(METHOD_POST);

    return new URLHttpResponse(connection);
  }

  /**
   * Method description
   *
   *
   * @param url
   * @param parameters
   *
   * @return
   *
   * @throws IOException
   */
  @Override
  public HttpResponse post(String url, Map<String, List<String>> parameters)
          throws IOException
  {
    HttpURLConnection connection = (HttpURLConnection) openConnection(null,
                                     url);

    connection.setRequestMethod(METHOD_POST);
    appendPostParameter(connection, parameters);

    return new URLHttpResponse(connection);
  }

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
  public HttpResponse post(HttpRequest request) throws IOException
  {
    HttpURLConnection connection = (HttpURLConnection) openConnection(request,
                                     request.getUrl());

    connection.setRequestMethod(METHOD_POST);
    appendPostParameter(connection, request.getParameters());

    return new URLHttpResponse(connection);
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param spec
   *
   * @return
   *
   * @throws IOException
   */
  @Override
  public HttpResponse get(String spec) throws IOException
  {
    return new URLHttpResponse(openConnection(null, spec));
  }

  /**
   * Method description
   *
   *
   * @param url
   * @param parameters
   *
   * @return
   *
   * @throws IOException
   */
  @Override
  public HttpResponse get(String url, Map<String, List<String>> parameters)
          throws IOException
  {
    url = createGetUrl(url, parameters);

    return new URLHttpResponse(openConnection(null, url));
  }

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
  public HttpResponse get(HttpRequest request) throws IOException
  {
    String url = createGetUrl(request.getUrl(), request.getParameters());

    return new URLHttpResponse(openConnection(request, url),
                               request.isDecodeGZip());
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param connection
   * @param header
   * @param username
   * @param password
   */
  private void appendBasicAuthHeader(HttpURLConnection connection,
                                     String header, String username,
                                     String password)
  {
    if (Util.isNotEmpty(username) || Util.isNotEmpty(password))
    {
      username = Util.nonNull(username);
      password = Util.nonNull(password);

      if (logger.isDebugEnabled())
      {
        logger.debug("append {} header for user {}", header, username);
      }

      String auth = username.concat(CREDENTIAL_SEPARATOR).concat(password);

      auth = new String(Base64.encode(auth.getBytes()));
      connection.addRequestProperty(header,
                                    PREFIX_BASIC_AUTHENTICATION.concat(auth));
    }
  }

  /**
   * Method description
   *
   *
   * @param headers
   * @param connection
   */
  private void appendHeaders(Map<String, List<String>> headers,
                             URLConnection connection)
  {
    if (Util.isNotEmpty(headers))
    {
      for (Map.Entry<String, List<String>> e : headers.entrySet())
      {
        String name = e.getKey();
        List<String> values = e.getValue();

        if (Util.isNotEmpty(name) && Util.isNotEmpty(values))
        {
          for (String value : values)
          {
            if (logger.isTraceEnabled())
            {
              logger.trace("append header {}:{}", name, value);
            }

            connection.setRequestProperty(name, value);
          }
        }
        else if (logger.isWarnEnabled())
        {
          logger.warn("value of {} header is empty", name);
        }
      }
    }
    else if (logger.isTraceEnabled())
    {
      logger.trace("header map is emtpy");
    }
  }

  /**
   * Method description
   *
   *
   * @param connection
   * @param parameters
   *
   * @throws IOException
   */
  private void appendPostParameter(HttpURLConnection connection,
                                   Map<String, List<String>> parameters)
          throws IOException
  {
    if (Util.isNotEmpty(parameters))
    {
      connection.setDoOutput(true);

      OutputStreamWriter writer = null;

      try
      {
        writer = new OutputStreamWriter(connection.getOutputStream());

        Iterator<Map.Entry<String, List<String>>> it =
          parameters.entrySet().iterator();

        while (it.hasNext())
        {
          Map.Entry<String, List<String>> p = it.next();
          List<String> values = p.getValue();

          if (Util.isNotEmpty(values))
          {
            String key = encode(p.getKey());

            for (String value : values)
            {
              writer.append(key).append("=").append(encode(value));
            }

            if (it.hasNext())
            {
              writer.write("&");
            }
          }
        }
      }
      finally
      {
        IOUtil.close(writer);
      }
    }
  }

  /**
   * Method description
   *
   *
   * @param request
   * @param connection
   */
  private void applySSLSettings(HttpRequest request,
                                HttpsURLConnection connection)
  {
    if (request.isDisableCertificateValidation())
    {
      if (logger.isTraceEnabled())
      {
        logger.trace("disable certificate validation");
      }

      try
      {
        TrustManager[] trustAllCerts = new TrustManager[] {
                                         new TrustAllTrustManager() };
        SSLContext sc = SSLContext.getInstance("SSL");

        sc.init(null, trustAllCerts, new java.security.SecureRandom());
        connection.setSSLSocketFactory(sc.getSocketFactory());
      }
      catch (Exception ex)
      {
        logger.error("could not disable certificate validation", ex);
      }
    }

    if (request.isDisableHostnameValidation())
    {
      if (logger.isTraceEnabled())
      {
        logger.trace("disable hostname validation");
      }

      connection.setHostnameVerifier(new TrustAllHostnameVerifier());
    }
  }

  /**
   * Method description
   *
   *
   * @param url
   * @param parameters
   *
   * @return
   */
  private String createGetUrl(String url, Map<String, List<String>> parameters)
  {
    if (Util.isNotEmpty(parameters))
    {
      StringBuilder ub = new StringBuilder(url);
      boolean first = url.contains("?");

      for (Map.Entry<String, List<String>> p : parameters.entrySet())
      {
        String key = encode(p.getKey());
        List<String> values = p.getValue();

        if (Util.isNotEmpty(values))
        {
          for (String value : values)
          {
            if (first)
            {
              ub.append("?");
              first = false;
            }
            else
            {
              ub.append("&");
            }

            ub.append(key).append("=").append(encode(value));
          }
        }
      }

      url = ub.toString();
    }

    return url;
  }

  /**
   * Method description
   *
   *
   * @param param
   *
   * @return
   */
  private String encode(String param)
  {
    try
    {
      param = URLEncoder.encode(param, ENCODING);
    }
    catch (IOException ex)
    {
      throw new RuntimeException(ex);
    }

    return param;
  }

  /**
   * Method description
   *
   *
   *
   * @param request
   * @param spec
   *
   * @return
   *
   * @throws IOException
   */
  private HttpURLConnection openConnection(HttpRequest request, String spec)
          throws IOException
  {
    return openConnection(request, new URL(spec));
  }

  /**
   * Method description
   *
   *
   *
   * @param request
   * @param url
   *
   * @return
   *
   * @throws IOException
   */
  private HttpURLConnection openConnection(HttpRequest request, URL url)
          throws IOException
  {
    HttpURLConnection connection = null;

    if (!request.isIgnoreProxySettings() && configuration.isEnableProxy())
    {
      if (logger.isDebugEnabled())
      {
        logger.debug("fetch '{}' using proxy {}:{}",
                     new Object[] { url.toExternalForm(),
                                    configuration.getProxyServer(),
                                    configuration.getProxyPort() });
      }

      SocketAddress address =
        new InetSocketAddress(configuration.getProxyServer(),
                              configuration.getProxyPort());

      connection =
        (HttpURLConnection) url.openConnection(new Proxy(Proxy.Type.HTTP,
          address));
    }
    else
    {
      if (request.isIgnoreProxySettings() && logger.isTraceEnabled())
      {
        logger.trace("ignore proxy settings");
      }

      if (logger.isDebugEnabled())
      {
        logger.debug("fetch '{}'", url.toExternalForm());
      }

      connection = (HttpURLConnection) url.openConnection();
    }

    if (connection instanceof HttpsURLConnection)
    {
      applySSLSettings(request, (HttpsURLConnection) connection);
    }

    connection.setReadTimeout(TIMEOUT_RAED);
    connection.setConnectTimeout(TIMEOUT_CONNECTION);

    if (request != null)
    {
      Map<String, List<String>> headers = request.getHeaders();

      appendHeaders(headers, connection);

      String username = request.getUsername();
      String password = request.getPassword();

      appendBasicAuthHeader(connection, HEADER_AUTHORIZATION, username,
                            password);
    }

    connection.setRequestProperty(HEADER_ACCEPT_ENCODING,
                                  HEADER_ACCEPT_ENCODING_VALUE);
    connection.setRequestProperty(
        HEADER_USERAGENT, HEADER_USERAGENT_VALUE.concat(context.getVersion()));

    String username = configuration.getProxyUser();
    String password = configuration.getProxyPassword();

    appendBasicAuthHeader(connection, HEADER_PROXY_AUTHORIZATION, username,
                          password);

    return connection;
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private ScmConfiguration configuration;

  /** Field description */
  private SCMContextProvider context;
}
