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
    
package sonia.scm.filter;

//~--- non-JDK imports --------------------------------------------------------

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;
import sonia.scm.util.IOUtil;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

/**
 * Response wrapper for gzip encoding.
 *
 * @author Sebastian Sdorra
 * @since 1.15
 */
public class GZipResponseWrapper extends HttpServletResponseWrapper
{

  /**
   * Constructs a new GZipResponseWrapper
   *
   *
   * @param response http response to wrap
   */
  public GZipResponseWrapper(HttpServletResponse response)
  {
    super(response);
    origResponse = response;
  }

  /**
   * Constructs ...
   *
   *
   * @param response
   * @since 1.16
   * @param config
   */
  public GZipResponseWrapper(HttpServletResponse response,
                             GZipFilterConfig config)
  {
    super(response);
    origResponse = response;
    this.config = config;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   */
  public void finishResponse()
  {
    IOUtil.close(writer);

    if ((stream != null) &&!stream.isClosed())
    {
      IOUtil.close(stream);
    }
  }

  /**
   * Method description
   *
   *
   * @throws IOException
   */
  @Override
  public void flushBuffer() throws IOException
  {
    if (stream != null)
    {
      stream.flush();
    }
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  public GZipFilterConfig getConfig()
  {
    return config;
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
    if (writer != null)
    {
      throw new IllegalStateException("getWriter() has already been called!");
    }

    if (stream == null)
    {
      stream = createOutputStream();
    }

    return stream;
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
    if (writer != null)
    {
      return writer;
    }

    if (stream != null)
    {
      throw new IllegalStateException(
          "getOutputStream() has already been called!");
    }

    stream = createOutputStream();
    writer = new PrintWriter(new OutputStreamWriter(stream, "UTF-8"));

    return writer;
  }

  //~--- set methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param length
   */
  @Override
  public void setContentLength(int length) {}

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   *
   * @throws IOException
   */
  private GZipResponseStream createOutputStream() throws IOException
  {
    return new GZipResponseStream(origResponse, config);
  }

  //~--- fields ---------------------------------------------------------------

  /** gzip filter config */
  protected GZipFilterConfig config = null;

  /** original http response */
  protected HttpServletResponse origResponse = null;

  /** gzip stream */
  protected GZipResponseStream stream = null;

  /** response writer */
  protected PrintWriter writer = null;
}
