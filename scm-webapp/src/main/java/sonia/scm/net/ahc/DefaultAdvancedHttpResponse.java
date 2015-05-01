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

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.io.ByteSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;
import java.io.InputStream;

import java.net.HttpURLConnection;

import java.util.List;
import java.util.Map.Entry;

/**
 * Http server response object of {@link DefaultAdvancedHttpClient}.
 *
 * @author Sebastian Sdorra
 * @since 1.46
 */
public class DefaultAdvancedHttpResponse extends AdvancedHttpResponse
{

  /**
   * Constructs a new {@link DefaultAdvancedHttpResponse}.
   *
   *
   * @param connection http connection
   * @param status response status code
   * @param statusText response status text
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
   * {@inheritDoc}
   */
  @Override
  public ByteSource contentAsByteSource() throws IOException
  {
    return new URLConnectionByteSource(connection);
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * {@inheritDoc}
   */
  @Override
  public Multimap<String, String> getHeaders()
  {
    if (headers == null)
    {
      headers = LinkedHashMultimap.create();

      for (Entry<String, List<String>> e :
        connection.getHeaderFields().entrySet())
      {
        headers.putAll(e.getKey(), e.getValue());
      }
    }

    return headers;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int getStatus()
  {
    return status;
  }

  /**
   * {@inheritDoc}
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
     * the logger for URLConnectionByteSource
     */
    private static final Logger logger =
      LoggerFactory.getLogger(URLConnectionByteSource.class);

    //~--- constructors -------------------------------------------------------

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
     * Opens the input stream of http connection, if an error occurs during
     * opening the method will return the error stream instead.
     *
     *
     * @return input or error stream of http connection
     *
     * @throws IOException
     */
    @Override
    public InputStream openStream() throws IOException
    {
      InputStream stream;

      try
      {
        stream = connection.getInputStream();
      }
      catch (IOException ex)
      {
        if (logger.isDebugEnabled())
        {
          logger.debug(
            "could not open input stream, open error stream instead", ex);
        }

        stream = connection.getErrorStream();
      }

      return stream;
    }

    //~--- fields -------------------------------------------------------------

    /** http connection */
    private final HttpURLConnection connection;
  }


  //~--- fields ---------------------------------------------------------------

  /** http connection */
  private final HttpURLConnection connection;

  /** server response status */
  private final int status;

  /** server response text */
  private final String statusText;

  /** http headers */
  private Multimap<String, String> headers;
}
