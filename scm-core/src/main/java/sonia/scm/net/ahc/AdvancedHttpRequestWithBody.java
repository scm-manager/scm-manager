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
import com.google.common.io.ByteSource;

//~--- JDK imports ------------------------------------------------------------

import java.io.File;

import java.nio.charset.Charset;

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
    return stringContent(content, Charsets.UTF_8);
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
