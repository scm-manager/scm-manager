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
