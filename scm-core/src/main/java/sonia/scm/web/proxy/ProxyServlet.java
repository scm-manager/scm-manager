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

import com.google.common.base.Preconditions;
import com.google.common.io.ByteStreams;
import com.google.common.io.Closer;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

//~--- JDK imports ------------------------------------------------------------

/**
 *
 * @author Sebastian Sdorra
 * @since 1.25
 */
@Singleton
public class ProxyServlet extends HttpServlet
{

  /**
   * the logger for ProxyServlet
   */
  private static final Logger logger =
    LoggerFactory.getLogger(ProxyServlet.class);

  /** Field description */
  private static final long serialVersionUID = 5589963595604482849L;

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   *
   * @param configurationProvider
   */
  @Inject
  public ProxyServlet(ProxyConfigurationProvider configurationProvider)
  {
    this.configurationProvider = configurationProvider;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param request
   * @param response
   */
  @Override
  public void service(HttpServletRequest request, HttpServletResponse response)
  {

    HttpURLConnection con;

    try
    {
      ProxyConfiguration configuration =
        configurationProvider.getConfiguration(request);

      Preconditions.checkNotNull(configuration);

      if (logger.isDebugEnabled())
      {
        logger.debug("proxy request for {}", configuration);
      }

      con = createConnection(configuration, request);

      if (configuration.isCopyRequestHeaders())
      {
        logger.trace("copy request headers");
        copyRequestHeaders(configuration, request, con);
      }
      else
      {
        logger.trace("skip copy of request headers");
      }

      con.connect();

      int responseCode = con.getResponseCode();

      logger.trace("resonse returned status code {}", responseCode);
      response.setStatus(responseCode);

      if (configuration.isCopyResponseHeaders())
      {
        logger.trace("copy response headers");
        copyResponseHeaders(configuration, con, response);
      }
      else
      {
        logger.trace("skip copy of response headers");
      }

      copyContent(con, response);

      con.disconnect();
    }
    catch (IOException ex)
    {
      logger.error("could not proxy request", ex);
      response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }

  }

  /**
   * Method description
   *
   *
   * @param con
   * @param response
   *
   * @throws IOException
   */
  private void copyContent(HttpURLConnection con, HttpServletResponse response)
    throws IOException
  {

    try (Closer closer = Closer.create()) {
      InputStream webToProxyBuf =
        closer.register(new BufferedInputStream(con.getInputStream()));
      OutputStream proxyToClientBuf =
        closer.register(new BufferedOutputStream(response.getOutputStream()));

      long bytes = ByteStreams.copy(webToProxyBuf, proxyToClientBuf);

      logger.trace("copied {} bytes for proxy", bytes);
    }
  }

  /**
   * Method description
   *
   *
   *
   * @param configuration
   * @param request
   * @param con
   */
  @SuppressWarnings("unchecked")
  private void copyRequestHeaders(ProxyConfiguration configuration,
    HttpServletRequest request, HttpURLConnection con)
  {
    Enumeration<String> names = request.getHeaderNames();
    Enumeration<String> values;
    String headerName;
    String value;

    while (names.hasMoreElements())
    {
      headerName = names.nextElement();

      if (!configuration.getRequestHeaderExcludes().contains(headerName))
      {

        values = request.getHeaders(headerName);

        while (values.hasMoreElements())
        {
          value = values.nextElement();
          logger.trace("set request header {}={}", headerName, value);
          con.setRequestProperty(headerName, value);
        }
      }
      else if (logger.isTraceEnabled())
      {
        logger.trace(
          "skip request header {}, because it is in the exclude list",
          headerName);
      }
    }
  }

  /**
   * Method description
   *
   *
   *
   * @param configuration
   * @param con
   * @param response
   */
  private void copyResponseHeaders(ProxyConfiguration configuration,
    HttpURLConnection con, HttpServletResponse response)
  {
    Map<String, List<String>> headers = con.getHeaderFields();
    String key;

    for (Entry<String, List<String>> e : headers.entrySet())
    {
      key = e.getKey();

      if (key != null)
      {

        if (!configuration.getResponseHeaderExcludes().contains(key))
        {
          for (String v : e.getValue())
          {
            logger.trace("add response header {}={}", key, v);
            response.addHeader(key, v);
          }

        }
        else if (logger.isTraceEnabled())
        {
          logger.trace(
            "skip request header {}, because it is in the exclude list", key);
        }

      }
      else if (logger.isTraceEnabled())
      {
        logger.trace("response header key is null");
      }
    }
  }

  /**
   * Method description
   *
   *
   * @param configuration
   * @param request
   *
   * @return
   *
   * @throws IOException
   */
  private HttpURLConnection createConnection(ProxyConfiguration configuration,
    HttpServletRequest request)
    throws IOException
  {
    HttpURLConnection con =
      (HttpURLConnection) configuration.getUrl().openConnection();

    con.setRequestMethod(request.getMethod());
    con.setDoOutput(true);
    con.setDoInput(true);
    con.setUseCaches(configuration.isCacheEnabled());

    return con;
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private final ProxyConfigurationProvider configurationProvider;
}
