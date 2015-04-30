/**
 * Copyright (c) 2014, Sebastian Sdorra All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer. 2. Redistributions in
 * binary form must reproduce the above copyright notice, this list of
 * conditions and the following disclaimer in the documentation and/or other
 * materials provided with the distribution. 3. Neither the name of SCM-Manager;
 * nor the names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * http://bitbucket.org/sdorra/scm-manager
 *
 */



package sonia.scm.net.ahc;

//~--- non-JDK imports --------------------------------------------------------

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.io.ByteSource;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;
import java.io.InputStream;

import java.net.HttpURLConnection;

import java.util.List;
import java.util.Map.Entry;

/**
 *
 * @author Sebastian Sdorra
 * @since 1.46
 */
public class DefaultAdvancedHttpResponse extends AdvancedHttpResponse
{

  /**
   * Constructs ...
   *
   *
   * @param connection
   * @param status
   * @param statusText
   */
  DefaultAdvancedHttpResponse(HttpURLConnection connection, int status,
    String statusText)
  {
    this.connection = connection;
    this.status = status;
    this.statusText = statusText;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   *
   * @throws IOException
   */
  @Override
  public ByteSource contentAsByteSource() throws IOException
  {
    return new URLConnectionByteSource(connection);
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  public Multimap<String, String> getHeaders()
  {
    if (headers == null)
    {
      headers = HashMultimap.create();

      for (Entry<String, List<String>> e :
        connection.getHeaderFields().entrySet())
      {
        headers.putAll(e.getKey(), e.getValue());
      }
    }

    return headers;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  public int getStatus()
  {
    return status;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  public String getStatusText()
  {
    return statusText;
  }

  //~--- inner classes --------------------------------------------------------

  /**
   * {@link ByteSource} implementation of a http connection.
   */
  private static class URLConnectionByteSource extends ByteSource
  {

    /**
     * Constructs a new {@link URLConnectionByteSource}.
     *
     *
     * @param connection http connection
     */
    private URLConnectionByteSource(HttpURLConnection connection)
    {
      this.connection = connection;
    }

    //~--- methods ------------------------------------------------------------

    /**
     * Opens the http connection.
     *
     *
     * @return http connection
     *
     * @throws IOException
     */
    @Override
    public InputStream openStream() throws IOException
    {
      return connection.getInputStream();
    }

    //~--- fields -------------------------------------------------------------

    /** http connection */
    private final HttpURLConnection connection;
  }


  //~--- fields ---------------------------------------------------------------

  /** http connection */
  private final HttpURLConnection connection;

  /** Field description */
  private final int status;

  /** Field description */
  private final String statusText;

  /** http headers */
  private Multimap<String, String> headers;
}
