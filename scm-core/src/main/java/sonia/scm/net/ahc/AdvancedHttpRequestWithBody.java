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

//~--- non-JDK imports --------------------------------------------------------

import com.google.common.io.ByteSource;

//~--- JDK imports ------------------------------------------------------------

import java.io.File;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * Http request with body.
 *
 * @author Sebastian Sdorra
 * @since 1.46
 */
public class AdvancedHttpRequestWithBody
  extends BaseHttpRequest<AdvancedHttpRequestWithBody>
{

  /**
   * Constructs a new {@link AdvancedHttpRequestWithBody}.
   *
   *
   * @param client http client
   * @param method http method
   * @param url url
   */
  AdvancedHttpRequestWithBody(AdvancedHttpClient client, String method,
    String url)
  {
    super(client, method, url);
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Sets the content length for the request.
   *
   *
   * @param length content length
   *
   * @return {@code this}
   */
  public AdvancedHttpRequestWithBody contentLength(long length)
  {
    return header("Content-Length", String.valueOf(length));
  }

  /**
   * Sets the content type for the request.
   *
   *
   * @param contentType content type
   *
   * @return {@code this}
   */
  public AdvancedHttpRequestWithBody contentType(String contentType)
  {
    return header("Content-Type", contentType);
  }

  /**
   * Sets the content of the file as request content.
   *
   *
   * @param file file
   *
   * @return {@code this}
   */
  public AdvancedHttpRequestWithBody fileContent(File file)
  {
    this.content = new FileContent(file);

    return this;
  }

  /**
   * Returns a {@link FormContentBuilder}. The builder can be used to add form
   * parameters as content for the request. <strong>Note:</strong> you have to
   * call {@link FormContentBuilder#build()} in order to apply the form content
   * to the request.
   *
   * @return form content builder
   */
  public FormContentBuilder formContent()
  {
    return new FormContentBuilder(this);
  }

  /**
   * Transforms the given object to a json string and set this string as request
   * content.
   *
   * @param object object to transform
   * @throws ContentTransformerNotFoundException if no
   *   {@link ContentTransformer} could be found for the json content-type
   *
   * @return {@code this}
   */
  public AdvancedHttpRequestWithBody jsonContent(Object object)
  {
    return transformedContent(ContentType.JSON, object);
  }

  /**
   * Sets the raw data as request content.
   *
   *
   * @param data raw data
   *
   * @return {@code this}
   */
  public AdvancedHttpRequestWithBody rawContent(byte[] data)
  {
    this.content = new RawContent(data);

    return this;
  }

  /**
   * Sets the raw data as request content.
   *
   *
   * @param source byte source
   *
   * @return {@code this}
   */
  public AdvancedHttpRequestWithBody rawContent(ByteSource source)
  {
    this.content = new ByteSourceContent(source);

    return this;
  }

  /**
   * Sets the string as request content.
   *
   *
   * @param content string content
   *
   * @return {@code this}
   */
  public AdvancedHttpRequestWithBody stringContent(String content)
  {
    return stringContent(content, StandardCharsets.UTF_8);
  }

  /**
   * Sets the string as request content.
   *
   *
   * @param content string content
   * @param charset charset of content
   *
   * @return {@code this}
   */
  public AdvancedHttpRequestWithBody stringContent(String content,
    Charset charset)
  {
    this.content = new StringContent(content, charset);

    return this;
  }

  /**
   * Transforms the given object to a string and set this string as request
   * content. The content-type is used to pick the right
   * {@link ContentTransformer}. The method will throw an exception if no
   * {@link ContentTransformer} for the content-type could be found.
   *
   * @param contentType content-type
   * @param object object to transform
   * @throws ContentTransformerNotFoundException if no
   *   {@link ContentTransformer} could be found for the given content-type
   *
   * @return {@code this}
   */
  public AdvancedHttpRequestWithBody transformedContent(String contentType,
    Object object)
  {
    ContentTransformer transformer =
      client.createTransformer(object.getClass(), contentType);
    ByteSource value = transformer.marshall(object);

    contentType(contentType);

    return rawContent(value);
  }

  /**
   * Use custom implementation of {@link Content} to create request content.
   * @param content content implementation
   * @return {@code this}
   * @since 2.27.0
   */
  AdvancedHttpRequestWithBody content(Content content) {
    this.content = content;
    return this;
  }

  /**
   * Transforms the given object to a xml string and set this string as request
   * content.
   *
   * @param object object to transform
   * @throws ContentTransformerNotFoundException if no
   *   {@link ContentTransformer} could be found for the xml content-type
   *
   * @return {@code this}
   */
  public AdvancedHttpRequestWithBody xmlContent(Object object)
  {
    return transformedContent(ContentType.XML, object);
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Returns the content or the request.
   *
   *
   * @return request content
   */
  public Content getContent()
  {
    return content;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Returns {@code this}.
   *
   *
   * @return {@code this}
   */
  @Override
  protected AdvancedHttpRequestWithBody self()
  {
    return this;
  }

  //~--- fields ---------------------------------------------------------------

  /** request content */
  private Content content;
}
