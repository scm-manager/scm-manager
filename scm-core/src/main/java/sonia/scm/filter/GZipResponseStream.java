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


import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.WriteListener;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.util.IOUtil;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPOutputStream;

/**
 * Response stream for gzip encoding.
 *
 * @since 1.15
 */
public class GZipResponseStream extends ServletOutputStream
{

  protected ByteArrayOutputStream baos = null;

  protected GZIPOutputStream gzipstream = null;

  protected boolean closed = false;

  protected ServletOutputStream output = null;

  protected HttpServletResponse response = null;

  private static final Logger logger =
    LoggerFactory.getLogger(GZipResponseStream.class);


  public GZipResponseStream(HttpServletResponse response) throws IOException
  {
    this(response, null);
  }

  /**
   * @since 1.16
   */
  public GZipResponseStream(HttpServletResponse response,
    GZipFilterConfig config)
    throws IOException
  {
    super();
    closed = false;
    this.response = response;
    response.addHeader("Content-Encoding", "gzip");

    if ((config == null) || config.isBufferResponse())
    {
      if (logger.isTraceEnabled())
      {
        logger.trace("use buffered gzip stream");
      }

      this.output = response.getOutputStream();
      baos = new ByteArrayOutputStream();
      gzipstream = new GZIPOutputStream(baos);
    }
    else
    {
      if (logger.isTraceEnabled())
      {
        logger.trace("use unbuffered gzip stream");
      }

      gzipstream = new GZIPOutputStream(response.getOutputStream());
    }
  }


  @Override
  public void close() throws IOException
  {
    if (closed)
    {
      throw new IOException("This output stream has already been closed");
    }

    gzipstream.finish();
    gzipstream.close();

    if (baos != null)
    {
      byte[] bytes = baos.toByteArray();

      response.addIntHeader("Content-Length", bytes.length);

      try
      {
        output.write(bytes);
        output.flush();
      }
      finally
      {
        IOUtil.close(output);
      }
    }

    closed = true;
  }

  @Override
  public void flush() throws IOException
  {
    if (closed)
    {
      throw new IOException("Cannot flush a closed output stream");
    }

    gzipstream.flush();
  }

   public void reset()
  {

    // noop
  }

  @Override
  public void write(int b) throws IOException
  {
    if (closed)
    {
      throw new IOException("Cannot write to a closed output stream");
    }

    gzipstream.write((byte) b);
  }

  @Override
  public void write(byte b[]) throws IOException
  {
    write(b, 0, b.length);
  }

  @Override
  public void write(byte b[], int off, int len) throws IOException
  {
    if (closed)
    {
      throw new IOException("Cannot write to a closed output stream");
    }

    gzipstream.write(b, off, len);
  }


  public boolean isClosed()
  {
    return closed;
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
      logger.debug("could not call writeListener.onWritePossible()", e);
    }
  }

}
