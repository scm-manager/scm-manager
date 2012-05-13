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



package sonia.scm.filter;

//~--- non-JDK imports --------------------------------------------------------

import sonia.scm.util.IOUtil;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

/**
 *
 * @author Sebastian Sdorra
 * @since 1.15
 */
public class GZipResponseWrapper extends HttpServletResponseWrapper
{

  /**
   * Constructs ...
   *
   *
   * @param response
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

  /** Field description */
  protected GZipFilterConfig config = null;

  /** Field description */
  protected HttpServletResponse origResponse = null;

  /** Field description */
  protected GZipResponseStream stream = null;

  /** Field description */
  protected PrintWriter writer = null;
}
