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

package sonia.scm.web.filter;

//~--- non-JDK imports --------------------------------------------------------

import com.google.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sonia.scm.util.Util;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;

import java.util.Date;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import javax.servlet.http.HttpSession;

/**
 *
 * @author Sebastian Sdorra
 */
@Singleton
public class LoggingFilter extends HttpFilter
{

  /** Field description */
  private static final Logger logger =
    LoggerFactory.getLogger(LoggingFilter.class);

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param request
   * @param response
   * @param chain
   *
   * @throws IOException
   * @throws ServletException
   */
  @Override
  protected void doFilter(HttpServletRequest request,
                          HttpServletResponse response, FilterChain chain)
          throws IOException, ServletException
  {
    if (logger.isDebugEnabled())
    {
      LoggingHttpServletRequest loggingRequest =
        new LoggingHttpServletRequest(request);
      LoggingHttpServletResponse loggingResponse =
        new LoggingHttpServletResponse(response);

      logRequest(loggingRequest);
      chain.doFilter(request, response);
      logResponse(loggingResponse);
    }
    else
    {
      chain.doFilter(request, response);
    }
  }

  /**
   * Method description
   *
   *
   * @param request
   */
  private void logRequest(LoggingHttpServletRequest request)
  {
    logger.debug("**************** request ****************");
    logger.debug("Info: Request-Uri = ".concat(request.getRequestURI()));
    logger.debug("Info: Remote-Addr = ".concat(request.getRemoteAddr()));
    logger.debug(
        "Info: Content-Size = ".concat(
          Integer.toString(request.getContentLength())));
    logger.debug(
        "Info: Content-Type = ".concat(Util.nonNull(request.getContentType())));
    logger.debug("Info: Method = ".concat(request.getMethod()));
    logger.debug(
        "Info: AuthType = ".concat(Util.nonNull(request.getAuthType())));

    Enumeration headers = request.getHeaderNames();

    while (headers.hasMoreElements())
    {
      String header = (String) headers.nextElement();

      logger.debug(
          "Header: ".concat(header).concat(" = ").concat(
            request.getHeader(header)));
    }

    Cookie[] cookies = request.getCookies();

    if (cookies != null)
    {
      for (Cookie cookie : cookies)
      {
        logger.debug(
            "Cookie: ".concat(cookie.getName()).concat(" = ").concat(
              cookie.getValue()));
      }
    }

    Enumeration parameters = request.getParameterNames();

    if (parameters != null)
    {
      while (parameters.hasMoreElements())
      {
        String parameter = (String) parameters.nextElement();

        logger.debug(
            "Parameter: ".concat(parameter).concat(" = ").concat(
              request.getParameter(parameter)));
      }
    }

    Enumeration attributes = request.getAttributeNames();

    if (attributes != null)
    {
      while (attributes.hasMoreElements())
      {
        String attribute = (String) attributes.nextElement();

        logger.debug(
            "Attribute: ".concat(attribute).concat(" = ").concat(
              request.getAttribute(attribute).toString()));
      }
    }

    HttpSession session = request.getSession(true);

    logger.debug("Session-New: ".concat(Boolean.toString(session.isNew())));

    Enumeration sAttributes = session.getAttributeNames();

    if (sAttributes != null)
    {
      while (sAttributes.hasMoreElements())
      {
        String sAttribute = (String) sAttributes.nextElement();

        logger.debug(
            "Session-Attribute: ".concat(sAttribute).concat(" = ").concat(
              request.getSession().getAttribute(sAttribute).toString()));
      }
    }
  }

  /**
   * Method description
   *
   *
   * @param response
   */
  private void logResponse(LoggingHttpServletResponse response)
  {
    logger.debug("**************** response ****************");
    logger.debug(
        "status code = ".concat(Integer.toString(response.getStatusCode())));
    logger.debug(
        "status message = ".concat(Util.nonNull(response.getStatusMessage())));
    logger.debug(
        "charset = ".concat(Util.nonNull(response.getCharacterEncoding())));
    logger.debug(
        "content-type = ".concat(Util.nonNull(response.getContentType())));
    logger.debug(
        "content-length = ".concat(
          Integer.toString(response.getContentLength())));

    for (Cookie cookie : response.getCookies())
    {
      logger.debug(
          "Cookie: ".concat(cookie.getName()).concat(" = ").concat(
            cookie.getValue()));
    }

    for (Entry<String, String> header : response.getHeaders().entrySet())
    {
      logger.debug(
          "Header: ".concat(header.getKey()).concat(" = ").concat(
            header.getValue()));
    }
  }

  //~--- inner classes --------------------------------------------------------

  /**
   * Class description
   *
   *
   * @version        Enter version here..., 10/09/29
   * @author         Enter your name here...
   */
  private static class LoggingHttpServletRequest
          extends HttpServletRequestWrapper
  {

    /**
     * Constructs ...
     *
     *
     * @param request
     */
    public LoggingHttpServletRequest(HttpServletRequest request)
    {
      super(request);
    }
  }


  /**
   * Class description
   *
   *
   * @version        Enter version here..., 10/09/29
   * @author         Enter your name here...
   */
  private static class LoggingHttpServletResponse
          extends HttpServletResponseWrapper
  {

    /**
     * Constructs ...
     *
     *
     * @param response
     */
    public LoggingHttpServletResponse(HttpServletResponse response)
    {
      super(response);
    }

    //~--- methods ------------------------------------------------------------

    /**
     * Method description
     *
     *
     * @param cookie
     */
    @Override
    public void addCookie(Cookie cookie)
    {
      cookies.add(cookie);
      super.addCookie(cookie);
    }

    /**
     * Method description
     *
     *
     * @param name
     * @param date
     */
    @Override
    public void addDateHeader(String name, long date)
    {
      headers.put(name, new Date(date).toString());
      super.addDateHeader(name, date);
    }

    /**
     * Method description
     *
     *
     * @param name
     * @param value
     */
    @Override
    public void addHeader(String name, String value)
    {
      headers.put(name, value);
      super.addHeader(name, value);
    }

    /**
     * Method description
     *
     *
     * @param name
     * @param value
     */
    @Override
    public void addIntHeader(String name, int value)
    {
      headers.put(name, Integer.toString(value));
      super.addIntHeader(name, value);
    }

    /**
     * Method description
     *
     *
     * @param sc
     *
     * @throws IOException
     */
    @Override
    public void sendError(int sc) throws IOException
    {
      this.statusCode = sc;
      super.sendError(sc);
    }

    /**
     * Method description
     *
     *
     * @param sc
     * @param msg
     *
     * @throws IOException
     */
    @Override
    public void sendError(int sc, String msg) throws IOException
    {
      this.statusCode = sc;
      this.statusMessage = msg;
      super.sendError(sc, msg);
    }

    //~--- get methods --------------------------------------------------------

    /**
     * Method description
     *
     *
     * @return
     */
    public int getContentLength()
    {
      return contentLength;
    }

    /**
     * Method description
     *
     *
     * @return
     */
    public Set<Cookie> getCookies()
    {
      return cookies;
    }

    /**
     * Method description
     *
     *
     * @return
     */
    public Map<String, String> getHeaders()
    {
      return headers;
    }

    /**
     * Method description
     *
     *
     * @return
     */
    public int getStatusCode()
    {
      return statusCode;
    }

    /**
     * Method description
     *
     *
     * @return
     */
    public String getStatusMessage()
    {
      return statusMessage;
    }

    //~--- set methods --------------------------------------------------------

    /**
     * Method description
     *
     *
     * @param len
     */
    @Override
    public void setContentLength(int len)
    {
      this.contentLength = len;
      super.setContentLength(len);
    }

    /**
     * Method description
     *
     *
     * @param name
     * @param date
     */
    @Override
    public void setDateHeader(String name, long date)
    {
      headers.put(name, new Date(date).toString());
      super.setDateHeader(name, date);
    }

    /**
     * Method description
     *
     *
     * @param name
     * @param value
     */
    @Override
    public void setHeader(String name, String value)
    {
      headers.put(name, value);
      super.setHeader(name, value);
    }

    /**
     * Method description
     *
     *
     * @param name
     * @param value
     */
    @Override
    public void setIntHeader(String name, int value)
    {
      headers.put(name, Integer.toString(value));
      super.setIntHeader(name, value);
    }

    /**
     * Method description
     *
     *
     * @param sc
     */
    @Override
    public void setStatus(int sc)
    {
      this.statusCode = sc;
      super.setStatus(sc);
    }

    /**
     * Method description
     *
     *
     * @param sc
     * @param sm
     */
    @Override
    public void setStatus(int sc, String sm)
    {
      this.statusCode = sc;
      this.statusMessage = sm;
      super.setStatus(sc, sm);
    }

    //~--- fields -------------------------------------------------------------

    /** Field description */
    private int contentLength = -1;

    /** Field description */
    private Set<Cookie> cookies = new HashSet<Cookie>();

    /** Field description */
    private int statusCode = HttpServletResponse.SC_OK;

    /** Field description */
    private Map<String, String> headers = new LinkedHashMap<String, String>();

    /** Field description */
    private String statusMessage;
  }
}
