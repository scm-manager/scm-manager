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



package sonia.scm.net;

//~--- non-JDK imports --------------------------------------------------------

import sonia.scm.util.IOUtil;
import sonia.scm.util.Util;

//~--- JDK imports ------------------------------------------------------------

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import java.net.HttpURLConnection;
import java.net.URLConnection;

import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

/**
 *
 * @author Sebastian Sdorra
 */
public class URLHttpResponse implements HttpResponse
{

  /** Field description */
  public static final String ENCODING_GZIP = "gzip";

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   *
   * @param connection
   */
  public URLHttpResponse(URLConnection connection)
  {
    this.connection = connection;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @throws IOException
   */
  @Override
  public void close() throws IOException
  {
    if ((connection != null) &&!clientClose)
    {
      InputStream in = getContent();

      if (in != null)
      {
        in.close();
      }
    }
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   *
   * @throws IOException
   */
  @Override
  public InputStream getContent() throws IOException
  {
    clientClose = true;

    String enc = connection.getContentEncoding();
    InputStream input = null;

    if (Util.isNotEmpty(enc) && enc.contains(ENCODING_GZIP))
    {
      input = new GZIPInputStream(connection.getInputStream());
    }
    else
    {
      input = connection.getInputStream();
    }

    return input;
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
  public String getContentAsString() throws IOException
  {
    String result = null;
    InputStream in = null;
    ByteArrayOutputStream baos = null;

    try
    {
      in = getContent();
      baos = new ByteArrayOutputStream();
      IOUtil.copy(in, baos);
      baos.flush();
      result = new String(baos.toByteArray());
    }
    finally
    {
      if (in != null)
      {
        in.close();
      }

      if (baos != null)
      {
        baos.close();
      }
    }

    return result;
  }

  /**
   * Method description
   *
   *
   * @param name
   *
   * @return
   */
  @Override
  public String getHeader(String name)
  {
    return connection.getHeaderField(name);
  }

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  public Map<String, List<String>> getHeaderMap()
  {
    return connection.getHeaderFields();
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
  public int getStatusCode() throws IOException
  {
    int result = -1;

    if (connection instanceof HttpURLConnection)
    {
      result = ((HttpURLConnection) connection).getResponseCode();
    }

    return result;
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private boolean clientClose = false;

  /** Field description */
  private URLConnection connection;
}
