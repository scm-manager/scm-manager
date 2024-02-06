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


import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;


public class BufferedHttpServletRequest extends HttpServletRequestWrapper
{
  private static final Logger logger =
    LoggerFactory.getLogger(BufferedHttpServletRequest.class);

  private ByteArrayInputStream bais;

  private ByteArrayOutputStream baos;

  private BufferedServletInputStream bsis;

  private byte[] buffer;


  public BufferedHttpServletRequest(HttpServletRequest request, boolean logBody)
          throws IOException
  {
    super(request);

    if (logBody)
    {
      InputStream is = request.getInputStream();

      baos = new ByteArrayOutputStream();

      byte buf[] = new byte[1024];
      int len;

      while ((len = is.read(buf)) > 0)
      {
        baos.write(buf, 0, len);
      }

      buffer = baos.toByteArray();
    }
  }


  
  public byte[] getContentBuffer()
  {
    return buffer;
  }


  @Override
  public ServletInputStream getInputStream() throws IOException
  {
    ServletInputStream stream = null;

    if (buffer != null)
    {
      try
      {
        bais = new ByteArrayInputStream(buffer);
        bsis = new BufferedServletInputStream(bais);
        stream = bsis;
      }
      catch (Exception ex)
      {
        logger.error("could not create InputStream", ex);
      }
    }
    else
    {
      stream = super.getInputStream();
    }

    return stream;
  }




  private static class BufferedServletInputStream extends ServletInputStream
  {
    private ByteArrayInputStream bais;
  
    public BufferedServletInputStream(ByteArrayInputStream bais)
    {
      this.bais = bais;
    }

    @Override
    public int available()
    {
      return bais.available();
    }

  
    @Override
    public int read()
    {
      return bais.read();
    }

    @Override
    public int read(byte[] buf, int off, int len)
    {
      return bais.read(buf, off, len);
    }

    @Override
    public boolean isFinished() {
      return bais.available() == 0;
    }

    @Override
    public boolean isReady() {
      return bais.available() > 0;
    }

    @Override
    public void setReadListener(ReadListener readListener) {
      try {
        readListener.onDataAvailable();
      } catch (IOException e) {
        logger.debug("could not call readListener.onDataAvailable()", e);
      }
    }

  }
}
