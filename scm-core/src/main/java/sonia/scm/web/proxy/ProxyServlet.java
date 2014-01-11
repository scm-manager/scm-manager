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

import com.google.common.base.Preconditions;
import com.google.common.io.ByteStreams;
import com.google.common.io.Closer;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//~--- JDK imports ------------------------------------------------------------

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

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
    Closer closer = Closer.create();

    try
    {
      InputStream webToProxyBuf =
        closer.register(new BufferedInputStream(con.getInputStream()));
      OutputStream proxyToClientBuf =
        closer.register(new BufferedOutputStream(response.getOutputStream()));

      long bytes = ByteStreams.copy(webToProxyBuf, proxyToClientBuf);

      logger.trace("copied {} bytes for proxy", bytes);
    }
    finally
    {
      closer.close();
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
