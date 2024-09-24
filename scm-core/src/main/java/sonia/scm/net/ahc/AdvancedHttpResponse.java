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


import com.google.common.base.Charsets;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import com.google.common.collect.Multimap;
import com.google.common.io.ByteSource;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;

/**
 * Http response. The response of a {@link AdvancedHttpRequest} or
 * {@link AdvancedHttpRequestWithBody}.
 *
 * @since 1.46
 */
public abstract class AdvancedHttpResponse
{

  /**
   * Returns the response content as byte source.
   */
  public abstract ByteSource contentAsByteSource() throws IOException;


  /**
   * Returns the response headers.
   */
  public abstract Multimap<String, String> getHeaders();

  /**
   * Returns the status code of the response.
   */
  public abstract int getStatus();

  /**
   * Returns the status text of the response.
   */
  public abstract String getStatusText();


  /**
   * Creates a {@link ContentTransformer} for the given Content-Type.
   *
   * @param type object type
   * @param contentType content-type
   * @throws ContentTransformerNotFoundException if no
   *   {@link ContentTransformer} could be found for the content-type
   *
   * @return {@link ContentTransformer}
   */
  protected abstract ContentTransformer createTransformer(Class<?> type,
    String contentType);

  /**
   * Returns the content of the response as byte array.
   */
  public byte[] content() throws IOException
  {
    ByteSource content = contentAsByteSource();
    byte[] data = null;

    if (content != null)
    {
      data = content.read();
    }

    return data;
  }

  /**
   * Returns a reader for the content of the response.
   */
  public BufferedReader contentAsReader() throws IOException
  {
    ByteSource content = contentAsByteSource();
    BufferedReader reader = null;

    if (content != null)
    {
      reader = content.asCharSource(Charsets.UTF_8).openBufferedStream();
    }

    return reader;
  }

  /**
   * Returns response content as stream.
   */
  public InputStream contentAsStream() throws IOException
  {
    ByteSource content = contentAsByteSource();
    InputStream stream = null;

    if (content != null)
    {
      stream = content.openBufferedStream();
    }

    return stream;
  }

  /**
   * Returns the response content as string.
   */
  public String contentAsString() throws IOException
  {
    ByteSource content = contentAsByteSource();
    String value = null;

    if (content != null)
    {
      value = content.asCharSource(Charsets.UTF_8).read();
    }

    return value;
  }

  /**
   * Transforms the response content from json to the given type.
   *
   * @param <T> object type
   * @param type object type
   *
   * @throws ContentTransformerNotFoundException if no
   *   {@link ContentTransformer} could be found for the json content-type
   *
   * @return transformed object
   *
   * @throws IOException
   */
  public <T> T contentFromJson(Class<T> type) throws IOException
  {
    return contentTransformed(type, ContentType.JSON);
  }

  /**
   * Transforms the response content from xml to the given type.
   *
   * @param <T> object type
   * @param type object type
   *
   * @throws ContentTransformerNotFoundException if no
   *   {@link ContentTransformer} could be found for the xml content-type
   *
   * @return transformed object
   *
   * @throws IOException
   */
  public <T> T contentFromXml(Class<T> type) throws IOException
  {
    return contentTransformed(type, ContentType.XML);
  }

  /**
   * Transforms the response content to the given type. The method uses the
   * content-type header to pick the right {@link ContentTransformer}.
   *
   * @param <T> object type
   * @param type object type
   *
   * @throws ContentTransformerNotFoundException if no
   *   {@link ContentTransformer} could be found for the content-type
   *
   * @return transformed object
   *
   * @throws IOException
   */
  public <T> T contentTransformed(Class<T> type) throws IOException
  {
    String contentType = getFirstHeader("Content-Type");

    if (Strings.isNullOrEmpty(contentType))
    {
      throw new ContentTransformerException(
        "response does not return a Content-Type header");
    }

    return contentTransformed(type, contentType);
  }

  /**
   * Transforms the response content to the given type. The method will use
   * the {@link ContentTransformer} which is responsible for the given
   * content type.
   *
   * @param <T> object type
   * @param type object type
   * @param contentType type to pick {@link ContentTransformer}
   *
   * @throws ContentTransformerNotFoundException if no
   *   {@link ContentTransformer} could be found for the content-type
   *
   * @return transformed object
   *
   * @throws IOException
   */
  public <T> T contentTransformed(Class<T> type, String contentType)
    throws IOException
  {
    T object = null;
    ByteSource source = contentAsByteSource();

    if (source != null)
    {
      ContentTransformer transformer = createTransformer(type, contentType);

      object = transformer.unmarshall(type, contentAsByteSource());
    }

    return object;
  }

  /**
   * Returns the first header value for the given header name or {@code null}.
   */
  public String getFirstHeader(String name)
  {
    return Iterables.getFirst(getHeaders().get(name), null);
  }

  /**
   * Returns {@code true} if the response was successful. A response is
   * successful, if the status code is greater than 199 and lower than 400.
   */
  public boolean isSuccessful()
  {
    int status = getStatus();

    return (status > 199) && (status < 400);
  }
}
