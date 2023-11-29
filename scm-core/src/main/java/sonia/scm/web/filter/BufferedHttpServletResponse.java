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

package sonia.scm.web.filter;

//~--- JDK imports ------------------------------------------------------------

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.WriteListener;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Sebastian Sdorra
 */
public class BufferedHttpServletResponse extends HttpServletResponseWrapper
{

  private static final Logger LOG = LoggerFactory.getLogger(BufferedHttpServletResponse.class);

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
////  @Override
//  @SuppressWarnings("deprecation")
  //TODO What to do?
//  public void setStatus(int sc, String sm)
//  {
//    this.statusCode = sc;
//    this.statusMessage = sm;
//    super.setStatus(sc);
//  }

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

    @Override
    public boolean isReady() {
      return true;
    }

    @Override
    public void setWriteListener(WriteListener writeListener) {
      try {
        writeListener.onWritePossible();
      } catch (IOException e) {
        LOG.debug("could not call writeListener.onWritePossible()", e);
      }
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
  private Set<Cookie> cookies = new HashSet<>();

  /** Field description */
  private int statusCode = HttpServletResponse.SC_OK;

  /** Field description */
  private Map<String, String> headers = new LinkedHashMap<>();

  /** Field description */
  private String statusMessage;
}
