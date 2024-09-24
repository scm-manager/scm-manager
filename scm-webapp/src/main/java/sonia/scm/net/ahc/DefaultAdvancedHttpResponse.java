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
