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


import com.google.common.io.ByteSource;

import java.io.File;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * Http request with body.
 *
 * @since 1.46
 */
public class AdvancedHttpRequestWithBody
  extends BaseHttpRequest<AdvancedHttpRequestWithBody>
{
  private Content content;

  /**
   * Constructs a new {@link AdvancedHttpRequestWithBody}.
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
   * @return {@code this}
   */
  public AdvancedHttpRequestWithBody contentLength(long length)
  {
    return header("Content-Length", String.valueOf(length));
  }

  /**
   * Sets the content type for the request.
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
   * @return {@code this}
   */
  public AdvancedHttpRequestWithBody stringContent(String content)
  {
    return stringContent(content, StandardCharsets.UTF_8);
  }

  /**
   * Sets the string as request content.
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


  /**
   * Returns the content or the request.
   */
  public Content getContent()
  {
    return content;
  }


  @Override
  protected AdvancedHttpRequestWithBody self()
  {
    return this;
  }

}
