/*
 * Copyright (c) 2020 - present Cloudogu GmbH
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

package sonia.scm.web.filter;


import com.google.common.base.Strings;
import com.google.inject.Singleton;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.util.IOUtil;
import sonia.scm.util.Util;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Map.Entry;


@Singleton
public class LoggingFilter extends HttpFilter
{

  private static final Logger logger =
    LoggerFactory.getLogger(LoggingFilter.class);


  @Override
  protected void doFilter(HttpServletRequest request,
    HttpServletResponse response, FilterChain chain)
    throws IOException, ServletException
  {
    if (logger.isDebugEnabled())
    {
      boolean logBody = logger.isTraceEnabled() && isTextRequest(request);
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

    HttpSession session = request.getSession(false);

    if (session != null)
    {
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
    }

    if (logger.isTraceEnabled())
    {
      byte[] contentBuffer = request.getContentBuffer();

      if ((contentBuffer != null) && (contentBuffer.length > 0))
      {
        logger.trace("Content: ".concat(new String(contentBuffer)));
      }
    }
  }

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

    if (logger.isTraceEnabled() && isTextRequest(orgResponse))
    {
      byte[] content = response.getContentBuffer();

      if ((content != null) && (content.length > 0))
      {
        ServletOutputStream out = null;

        try
        {
          out = orgResponse.getOutputStream();
          out.write(content);
        }
        finally
        {
          IOUtil.close(out);
        }

        logger.trace("Content: ".concat(new String(content)));
      }
    }
  }



  private boolean isTextRequest(HttpServletRequest request)
  {
    return isTextRequest(request.getContentType());
  }


  private boolean isTextRequest(HttpServletResponse response)
  {
    return isTextRequest(response.getContentType());
  }


  private boolean isTextRequest(String contentType)
  {
    return !Strings.isNullOrEmpty(contentType)
      && contentType.toLowerCase(Locale.ENGLISH).startsWith("text");
  }
}
