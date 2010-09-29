/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */



package sonia.scm.web.filter;

//~--- non-JDK imports --------------------------------------------------------

import com.google.inject.Singleton;
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
import java.util.logging.Level;
import java.util.logging.Logger;

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
    Logger.getLogger(LoggingFilter.class.getName());

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
    if (logger.isLoggable(Level.FINEST))
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
    logger.finest("**************** request ****************");
    logger.finest("Info: Request-Uri = ".concat(request.getRequestURI()));
    logger.finest("Info: Remote-Addr = ".concat(request.getRemoteAddr()));
    logger.finest(
        "Info: Content-Size = ".concat(
          Integer.toString(request.getContentLength())));
    logger.finest(
        "Info: Content-Type = ".concat(Util.nonNull(request.getContentType())));
    logger.finest("Info: Method = ".concat(request.getMethod()));
    logger.finest(
        "Info: AuthType = ".concat(Util.nonNull(request.getAuthType())));

    Enumeration headers = request.getHeaderNames();

    while (headers.hasMoreElements())
    {
      String header = (String) headers.nextElement();

      logger.finest(
          "Header: ".concat(header).concat(" = ").concat(
            request.getHeader(header)));
    }

    Cookie[] cookies = request.getCookies();

    if (cookies != null)
    {
      for (Cookie cookie : cookies)
      {
        logger.finest(
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

        logger.finest(
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

        logger.finest(
            "Attribute: ".concat(attribute).concat(" = ").concat(
              request.getAttribute(attribute).toString()));
      }
    }

    HttpSession session = request.getSession(true);

    logger.finest("Session-New: ".concat(Boolean.toString(session.isNew())));

    Enumeration sAttributes = session.getAttributeNames();

    if (sAttributes != null)
    {
      while (sAttributes.hasMoreElements())
      {
        String sAttribute = (String) sAttributes.nextElement();

        logger.finest(
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
    logger.finest("**************** response ****************");
    logger.finest(
        "status code = ".concat(Integer.toString(response.getStatusCode())));
    logger.finest(
        "status message = ".concat(Util.nonNull(response.getStatusMessage())));
    logger.finest(
        "charset = ".concat(Util.nonNull(response.getCharacterEncoding())));
    logger.finest(
        "content-type = ".concat(Util.nonNull(response.getContentType())));
    logger.finest(
        "content-length = ".concat(
          Integer.toString(response.getContentLength())));

    for (Cookie cookie : response.getCookies())
    {
      logger.finest(
          "Cookie: ".concat(cookie.getName()).concat(" = ").concat(
            cookie.getValue()));
    }

    for (Entry<String, String> header : response.getHeaders().entrySet())
    {
      logger.finest(
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
