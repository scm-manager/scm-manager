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

package sonia.scm.filter;


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
 * @since 1.15
 */
public class GZipResponseWrapper extends HttpServletResponseWrapper
{
  protected GZipFilterConfig config = null;

  protected HttpServletResponse origResponse = null;

  protected GZipResponseStream stream = null;

  /** response writer */
  protected PrintWriter writer = null;

  public GZipResponseWrapper(HttpServletResponse response)
  {
    super(response);
    origResponse = response;
  }

  /**
   * @since 1.16
   */
  public GZipResponseWrapper(HttpServletResponse response,
                             GZipFilterConfig config)
  {
    super(response);
    origResponse = response;
    this.config = config;
  }


   public void finishResponse()
  {
    IOUtil.close(writer);

    if ((stream != null) &&!stream.isClosed())
    {
      IOUtil.close(stream);
    }
  }

  @Override
  public void flushBuffer() throws IOException
  {
    if (stream != null)
    {
      stream.flush();
    }
  }

  public GZipFilterConfig getConfig()
  {
    return config;
  }

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


  @Override
  public void setContentLength(int length) {}


  private GZipResponseStream createOutputStream() throws IOException
  {
    return new GZipResponseStream(origResponse, config);
  }

}
