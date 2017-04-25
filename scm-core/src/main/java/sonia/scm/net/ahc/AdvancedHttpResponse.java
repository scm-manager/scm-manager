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

import com.google.common.base.Charsets;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import com.google.common.collect.Multimap;
import com.google.common.io.ByteSource;

//~--- JDK imports ------------------------------------------------------------

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;

/**
 * Http response. The response of a {@link AdvancedHttpRequest} or
 * {@link AdvancedHttpRequestWithBody}.
 *
 * @author Sebastian Sdorra
 * @since 1.46
 */
public abstract class AdvancedHttpResponse
{

  /**
   * Returns the response content as byte source.
   *
   *
   * @return response content as byte source
   * @throws IOException
   */
  public abstract ByteSource contentAsByteSource() throws IOException;

  //~--- get methods ----------------------------------------------------------

  /**
   * Returns the response headers.
   *
   *
   * @return response headers
   */
  public abstract Multimap<String, String> getHeaders();

  /**
   * Returns the status code of the response.
   *
   *
   * @return status code
   */
  public abstract int getStatus();

  /**
   * Returns the status text of the response.
   *
   *
   * @return status text
   */
  public abstract String getStatusText();

  //~--- methods --------------------------------------------------------------

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
   *
   *
   * @return content as byte array
   *
   * @throws IOException
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
   *
   *
   * @return read of response content
   *
   * @throws IOException
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
   *
   *
   * @return response content as stram
   *
   * @throws IOException
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
   *
   *
   * @return response content
   *
   * @throws IOException
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
   * the {@link ContentTransformer} which is responsible for the the given
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

  //~--- get methods ----------------------------------------------------------

  /**
   * Returns the first header value for the given header name or {@code null}.
   *
   *
   * @param name header name
   *
   * @return header value or {@code null}
   */
  public String getFirstHeader(String name)
  {
    return Iterables.getFirst(getHeaders().get(name), null);
  }

  /**
   * Returns {@code true} if the response was successful. A response is
   * successful, if the status code is greater than 199 and lower than 400.
   *
   * @return {@code true} if the response was successful
   */
  public boolean isSuccessful()
  {
    int status = getStatus();

    return (status > 199) && (status < 400);
  }
}
