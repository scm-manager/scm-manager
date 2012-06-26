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

import com.google.common.io.Closeables;
import com.google.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sonia.scm.util.Util;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;

import java.util.Enumeration;
import java.util.Map.Entry;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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
      boolean logBody = logger.isTraceEnabled();
      BufferedHttpServletRequest bufferedRequest =
        new BufferedHttpServletRequest(request, logBody);
      BufferedHttpServletResponse bufferedResponse =
        new BufferedHttpServletResponse(response, logBody);

      logRequest(bufferedRequest);
      chain.doFilter(bufferedRequest, bufferedResponse);
      logResponse(response, bufferedResponse);
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
  private void logRequest(BufferedHttpServletRequest request)
  {
    logger.debug("**************** request ****************");
    logger.debug("Info: Request-Uri = {}", request.getRequestURI());
    logger.debug("Info: Remote-Addr = {}", request.getRemoteAddr());
    logger.debug("Info: Remote-User = {}",
                 Util.nonNull(request.getRemoteUser()));
    logger.debug("Info: Content-Size = {}",
                 Integer.toString(request.getContentLength()));
    logger.debug("Info: Content-Type = {}",
                 Util.nonNull(request.getContentType()));
    logger.debug("Info: Method = {}", request.getMethod());
    logger.debug("Info: AuthType = {}", Util.nonNull(request.getAuthType()));

    Enumeration headers = request.getHeaderNames();

    while (headers.hasMoreElements())
    {
      String header = (String) headers.nextElement();

      logger.debug("Header: {} = {}", header, request.getHeader(header));
    }

    Cookie[] cookies = request.getCookies();

    if (cookies != null)
    {
      for (Cookie cookie : cookies)
      {
        logger.debug("Cookie: {} = {}", cookie.getName(), cookie.getValue());
      }
    }

    Enumeration parameters = request.getParameterNames();

    if (parameters != null)
    {
      while (parameters.hasMoreElements())
      {
        String parameter = (String) parameters.nextElement();

        logger.debug("Parameter: {} = {}", parameter,
                     request.getParameter(parameter));
      }
    }

    Enumeration attributes = request.getAttributeNames();

    if (attributes != null)
    {
      while (attributes.hasMoreElements())
      {
        String attribute = (String) attributes.nextElement();

        logger.debug("Attribute: {} = {}", attribute,
                     request.getAttribute(attribute).toString());
      }
    }

    HttpSession session = request.getSession(true);

    logger.debug("Session-New: {}", Boolean.toString(session.isNew()));

    Enumeration sAttributes = session.getAttributeNames();

    if (sAttributes != null)
    {
      while (sAttributes.hasMoreElements())
      {
        String sAttribute = (String) sAttributes.nextElement();

        logger.debug("Session-Attribute: {} = {}", sAttribute,
                     request.getSession().getAttribute(sAttribute).toString());
      }
    }

    if (logger.isTraceEnabled())
    {
      logger.trace("Content: ".concat(new String(request.getContentBuffer())));
    }
  }

  /**
   * Method description
   *
   *
   *
   * @param orgResponse
   * @param response
   *
   * @throws IOException
   */
  private void logResponse(HttpServletResponse orgResponse,
                           BufferedHttpServletResponse response)
          throws IOException
  {
    logger.debug("**************** response ****************");
    logger.debug("status code = {}",
                 Integer.toString(response.getStatusCode()));
    logger.debug("status message = {}",
                 Util.nonNull(response.getStatusMessage()));
    logger.debug("charset = {}", Util.nonNull(response.getCharacterEncoding()));
    logger.debug("content-type = {}", Util.nonNull(response.getContentType()));
    logger.debug("content-length = {}",
                 Integer.toString(response.getContentLength()));

    for (Cookie cookie : response.getCookies())
    {
      logger.debug("Cookie: {} = {}", cookie.getName(), cookie.getValue());
    }

    for (Entry<String, String> header : response.getHeaders().entrySet())
    {
      logger.debug("Header: {} = {}", header.getKey(), header.getValue());
    }

    if (logger.isTraceEnabled())
    {
      byte[] content = response.getContentBuffer();
      ServletOutputStream out = null;

      try
      {
        out = orgResponse.getOutputStream();
        out.write(content);
      }
      finally
      {
        Closeables.closeQuietly(out);
      }

      logger.trace("Content: ".concat(new String(content)));
    }
  }
}
