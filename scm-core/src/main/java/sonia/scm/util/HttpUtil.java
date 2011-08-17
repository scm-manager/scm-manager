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



package sonia.scm.util;

//~--- non-JDK imports --------------------------------------------------------

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sonia.scm.config.ScmConfiguration;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Util method for the http protocol.
 *
 * @author Sebastian Sdorra
 */
public class HttpUtil
{

  /** authentication realm for basic authentication */
  public static final String AUTHENTICATION_REALM = "SONIA :: SCM Manager";

  /** authentication header */
  public static final String HEADER_WWW_AUTHENTICATE = "WWW-Authenticate";

  /**
   * Default http port
   * @since 1.5
   */
  public static final int PORT_HTTP = 80;

  /**
   * Default https port
   * @since 1.5
   */
  public static final int PORT_HTTPS = 443;

  /**
   * Default http scheme
   * @since 1.5
   */
  public static final String SCHEME_HTTP = "http";

  /**
   * Default https scheme
   * @since 1.5
   */
  public static final String SCHEME_HTTPS = "https";

  /**
   * Url folder separator
   * @since 1.5
   */
  public static final String SEPARATOR_PATH = "/";

  /**
   * Url port separator
   * @since 1.5
   */
  public static final String SEPARATOR_PORT = ":";

  /**
   * Url scheme separator
   * @since 1.5
   */
  public static final String SEPARATOR_SCHEME = "://";

  /** message for unauthorized request */
  public static final String STATUS_UNAUTHORIZED_MESSAGE =
    "Authorization Required";

  /** the logger for HttpUtil */
  private static final Logger logger = LoggerFactory.getLogger(HttpUtil.class);

  //~--- methods --------------------------------------------------------------

  /**
   * Send an unauthorized header back to the client
   *
   *
   * @param response - the http response
   *
   * @throws IOException
   */
  public static void sendUnauthorized(HttpServletResponse response)
          throws IOException
  {
    response.setHeader(
        HEADER_WWW_AUTHENTICATE,
        "Basic realm=\"".concat(AUTHENTICATION_REALM).concat("\""));
    response.sendError(HttpServletResponse.SC_UNAUTHORIZED,
                       STATUS_UNAUTHORIZED_MESSAGE);
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Return the complete url of the given path.
   *
   *
   * @param configuration - main SCM-Manager configuration
   * @param path to get the url from
   *
   * @since 1.5
   *
   * @return the complete url of the given path
   */
  public static String getCompleteUrl(ScmConfiguration configuration,
          String path)
  {
    String url = configuration.getBaseUrl();

    if (url.endsWith(SEPARATOR_PATH) && path.startsWith(SEPARATOR_PATH))
    {
      url = url.substring(0, url.length());
    }
    else if (!path.startsWith(SEPARATOR_PATH))
    {
      path = SEPARATOR_PATH.concat(path);
    }

    return url.concat(path);
  }

  /**
   * Returns the port of the url parameter.
   *
   * @param url
   * @return port of url
   */
  public static int getPortFromUrl(String url)
  {
    AssertUtil.assertIsNotEmpty(url);

    int port = PORT_HTTP;
    int schemeIndex = url.indexOf(SEPARATOR_SCHEME);

    if (schemeIndex > 0)
    {
      String urlWithoutScheme = url.substring(schemeIndex
                                  + SEPARATOR_SCHEME.length());
      int portIndex = urlWithoutScheme.lastIndexOf(SEPARATOR_PORT);

      if (portIndex > 0)
      {
        String portString = urlWithoutScheme.substring(portIndex + 1);
        int slIndex = portString.indexOf(SEPARATOR_PATH);

        if (slIndex > 0)
        {
          portString = portString.substring(0, slIndex);
        }

        try
        {
          port = Integer.parseInt(portString);
        }
        catch (NumberFormatException ex)
        {
          logger.error("could not parse port part of url", ex);
        }
      }
      else if (url.startsWith(SCHEME_HTTPS))
      {
        port = PORT_HTTPS;
      }
    }

    return port;
  }

  /**
   * Returns the server port
   *
   *
   * @param configuration
   * @param request
   *
   * @return the server port
   */
  public static int getServerPort(ScmConfiguration configuration,
                                  HttpServletRequest request)
  {
    int port = PORT_HTTP;
    String baseUrl = configuration.getBaseUrl();

    if (Util.isNotEmpty(baseUrl))
    {
      port = getPortFromUrl(baseUrl);
    }
    else
    {
      port = request.getServerPort();
    }

    return port;
  }

  /**
   * Return the request uri with out the context path.
   *
   *
   * @param request - the http client request
   *
   * @return the request uri with out the context path
   */
  public static String getStrippedURI(HttpServletRequest request)
  {
    return getStrippedURI(request, request.getRequestURI());
  }

  /**
   * Returns the given uri without the context path.
   *
   *
   * @param request - the http client request
   * @param uri - the uri to get the stripped uri from
   *
   * @return uri without context path
   */
  public static String getStrippedURI(HttpServletRequest request, String uri)
  {
    return uri.substring(request.getContextPath().length());
  }

  /**
   * Returns the given uri without ending separator.
   *
   *
   * @param uri - to strip ending separator
   *
   * @return the given uri without a ending separator
   * @since 1.7
   */
  public static String getUriWithoutEndSeperator(String uri)
  {
    if (uri.endsWith(SEPARATOR_PATH))
    {
      uri = uri.substring(0, uri.length() - 1);
    }

    return uri;
  }

  /**
   * Returns the given uri without leading separator.
   *
   *
   * @param uri - to strip leading separator
   *
   * @return the given uri without leading separator
   * @since 1.7
   */
  public static String getUriWithoutStartSeperator(String uri)
  {
    if (uri.startsWith(SEPARATOR_PATH))
    {
      uri = uri.substring(1);
    }

    return uri;
  }
}
