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

//~--- non-JDK imports --------------------------------------------------------

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//~--- JDK imports ------------------------------------------------------------

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

/**
 *
 * @author Sebastian Sdorra
 */
public class BufferedHttpServletRequest extends HttpServletRequestWrapper
{

  /** the logger for BufferedHttpServletRequest */
  private static final Logger logger =
    LoggerFactory.getLogger(BufferedHttpServletRequest.class);

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   *
   * @param request
   * @param logBody
   *
   * @throws IOException
   */
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

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  public byte[] getContentBuffer()
  {
    return buffer;
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

  //~--- inner classes --------------------------------------------------------

  /**
   * Class description
   *
   *
   * @version        Enter version here..., 2011-04-12
   * @author         Sebastian Sdorra
   */
  private static class BufferedServletInputStream extends ServletInputStream
  {

    /**
     * Constructs ...
     *
     *
     * @param bais
     */
    public BufferedServletInputStream(ByteArrayInputStream bais)
    {
      this.bais = bais;
    }

    //~--- methods ------------------------------------------------------------

    /**
     * Method description
     *
     *
     * @return
     */
    @Override
    public int available()
    {
      return bais.available();
    }

    /**
     * Method description
     *
     *
     * @return
     */
    @Override
    public int read()
    {
      return bais.read();
    }

    /**
     * Method description
     *
     *
     * @param buf
     * @param off
     * @param len
     *
     * @return
     */
    @Override
    public int read(byte[] buf, int off, int len)
    {
      return bais.read(buf, off, len);
    }

    //~--- fields -------------------------------------------------------------

    /** Field description */
    private ByteArrayInputStream bais;
  }


  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private ByteArrayInputStream bais;

  /** Field description */
  private ByteArrayOutputStream baos;

  /** Field description */
  private BufferedServletInputStream bsis;

  /** Field description */
  private byte[] buffer;
}
