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

package sonia.scm.net.ahc;


import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.io.ByteSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;

import java.net.HttpURLConnection;

import java.util.List;
import java.util.Map.Entry;

/**
 * Http server response object of {@link DefaultAdvancedHttpClient}.
 *
 * @since 1.46
 */
public class DefaultAdvancedHttpResponse extends AdvancedHttpResponse
{
  private final DefaultAdvancedHttpClient client;

  private final HttpURLConnection connection;

  /** server response status */
  private final int status;

  /** server response text */
  private final String statusText;

  /** http headers */
  private Multimap<String, String> headers;

  /**
   * Constructs a new {@link DefaultAdvancedHttpResponse}.
   *
   * @param client ahc
   * @param connection http connection
   * @param status response status code
   * @param statusText response status text
   */
  DefaultAdvancedHttpResponse(DefaultAdvancedHttpClient client,
HttpURLConnection connection, int status, String statusText)
  {
    this.client = client;
    this.connection = connection;
    this.status = status;
    this.statusText = statusText;
  }


 
  @Override
  public ByteSource contentAsByteSource() throws IOException
  {
    return new URLConnectionByteSource(connection);
  }


 
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

 
  @Override
  public int getStatus()
  {
    return status;
  }

 
  @Override
  public String getStatusText()
  {
    return statusText;
  }


 
  @Override
  protected ContentTransformer createTransformer(Class<?> type,
    String contentType)
  {
    return client.createTransformer(type, contentType);
  }



  /**
   * {@link ByteSource} implementation of a http connection.
   */
  private static class URLConnectionByteSource extends ByteSource
  {

    private static final Logger logger =
      LoggerFactory.getLogger(URLConnectionByteSource.class);

    private final HttpURLConnection connection;

    private URLConnectionByteSource(HttpURLConnection connection)
    {
      this.connection = connection;
    }

    

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

  }

}
