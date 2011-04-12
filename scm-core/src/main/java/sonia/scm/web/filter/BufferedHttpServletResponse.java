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

//~--- JDK imports ------------------------------------------------------------

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;

import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

/**
 *
 * @author Sebastian Sdorra
 */
public class BufferedHttpServletResponse extends HttpServletResponseWrapper
{

  /**
   *     Constructs ...
   *
   *
   *     @param response
   * @param logBody
   */
  public BufferedHttpServletResponse(HttpServletResponse response,
                                     boolean logBody)
  {
    super(response);

    if (logBody)
    {
      pw = new ByteArrayPrintWriter();
    }
  }

  //~--- methods --------------------------------------------------------------

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

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  public byte[] getContentBuffer()
  {
    byte[] content = null;

    if (pw != null)
    {
      content = pw.toByteArray();
    }

    return content;
  }

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
   *
   * @throws IOException
   */
  @Override
  public ServletOutputStream getOutputStream() throws IOException
  {
    return (pw != null)
           ? pw.getStream()
           : super.getOutputStream();
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

  /**
   * Method description
   *
   *
   * @return
   *
   * @throws IOException
   */
  @Override
  public PrintWriter getWriter() throws IOException
  {
    return (pw != null)
           ? pw.getWriter()
           : super.getWriter();
  }

  //~--- set methods ----------------------------------------------------------

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

  //~--- inner classes --------------------------------------------------------

  /**
   * Class description
   *
   *
   * @version        Enter version here..., 2011-04-12
   * @author         Sebastian Sdorra
   */
  private static class ByteArrayPrintWriter
  {

    /**
     * Method description
     *
     *
     * @return
     */
    public byte[] toByteArray()
    {
      return baos.toByteArray();
    }

    //~--- get methods --------------------------------------------------------

    /**
     * Method description
     *
     *
     * @return
     */
    public ServletOutputStream getStream()
    {
      return sos;
    }

    /**
     * Method description
     *
     *
     * @return
     */
    public PrintWriter getWriter()
    {
      return pw;
    }

    //~--- fields -------------------------------------------------------------

    /** Field description */
    private ByteArrayOutputStream baos = new ByteArrayOutputStream();

    /** Field description */
    private PrintWriter pw = new PrintWriter(baos);

    /** Field description */
    private ServletOutputStream sos = new ByteArrayServletStream(baos);
  }


  /**
   * Class description
   *
   *
   * @version        Enter version here..., 2011-04-12
   * @author         Sebastian Sdorra
   */
  private static class ByteArrayServletStream extends ServletOutputStream
  {

    /**
     * Constructs ...
     *
     *
     * @param baos
     */
    ByteArrayServletStream(ByteArrayOutputStream baos)
    {
      this.baos = baos;
    }

    //~--- methods ------------------------------------------------------------

    /**
     * Method description
     *
     *
     * @param param
     *
     * @throws IOException
     */
    @Override
    public void write(int param) throws IOException
    {
      baos.write(param);
    }

    //~--- fields -------------------------------------------------------------

    /** Field description */
    private ByteArrayOutputStream baos;
  }


  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private int contentLength = -1;

  /** Field description */
  private ByteArrayPrintWriter pw = null;

  /** Field description */
  private Set<Cookie> cookies = new HashSet<Cookie>();

  /** Field description */
  private int statusCode = HttpServletResponse.SC_OK;

  /** Field description */
  private Map<String, String> headers = new LinkedHashMap<String, String>();

  /** Field description */
  private String statusMessage;
}
