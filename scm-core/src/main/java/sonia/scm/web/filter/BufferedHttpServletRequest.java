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
