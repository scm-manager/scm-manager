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

import java.io.IOException;

/**
 * Advanced client for http operations.The {@link AdvancedHttpClient} offers
 * a fluid interface for handling most common
 * http operations. The {@link AdvancedHttpClient} can be injected by the
 * default injection mechanism of SCM-Manager.
 * <p>&nbsp;</p>
 * <b>Http GET example:</b>
 *
 * <pre><code>
 * AdvancedHttpResponse response = client.get("https://scm-manager.org")
 *                                       .decodeGZip(true)
 *                                       .request();
 *
 * System.out.println(response.contentAsString());
 * </code></pre>
 *
 * <p>&nbsp;</p>
 * <b>Http POST example:</b>
 *
 * <pre><code>
 * AdvancedHttpResponse response = client.post("https://scm-manager.org")
 *                                       .formContent()
 *                                       .field("firstname", "Tricia")
 *                                       .field("lastname", "McMillan")
 *                                       .build()
 *                                       .request();
 *
 * if (response.isSuccessful()){
 *   System.out.println("success");
 * }
 * </code></pre>
 *
 * @since 1.46
 */
public abstract class AdvancedHttpClient {

  /**
   * Creates a {@link ContentTransformer} for the given Content-Type.
   *
   * @param type        object type
   * @param contentType content-type
   * @return {@link ContentTransformer}
   * @throws ContentTransformerNotFoundException if no
   *                                             {@link ContentTransformer} could be found for the content-type
   */
  protected abstract ContentTransformer createTransformer(Class<?> type,
                                                          String contentType);

  /**
   * Executes the given request and returns the http response. Implementation
   * have to check, if the instance is from type
   * {@link AdvancedHttpRequestWithBody} in order to handle request contents.
   *
   * @param request request to execute
   * @return http response
   * @throws IOException
   */
  protected abstract AdvancedHttpResponse request(BaseHttpRequest<?> request)
    throws IOException;

  /**
   * Returns a builder for a DELETE request.
   */
  public AdvancedHttpRequestWithBody delete(String url) {
    return new AdvancedHttpRequestWithBody(this, HttpMethod.DELETE, url);
  }

  /**
   * Returns a builder for a HEAD request.
   */
  public AdvancedHttpRequest head(String url) {
    return new AdvancedHttpRequest(this, HttpMethod.HEAD, url);
  }

  /**
   * Returns a request builder with a custom method. <strong>Note:</strong> not
   * every method is supported by the underlying implementation of the http
   * client.
   */
  public AdvancedHttpRequestWithBody method(String method, String url) {
    return new AdvancedHttpRequestWithBody(this, method, url);
  }

  /**
   * Returns a builder for a OPTIONS request.
   */
  public AdvancedHttpRequestWithBody options(String url) {
    return new AdvancedHttpRequestWithBody(this, HttpMethod.OPTIONS, url);
  }

  /**
   * Returns a builder for a POST request.
   */
  public AdvancedHttpRequestWithBody post(String url) {
    return new AdvancedHttpRequestWithBody(this, HttpMethod.POST, url);
  }

  /**
   * Returns a builder for a PUT request.
   */
  public AdvancedHttpRequestWithBody put(String url) {
    return new AdvancedHttpRequestWithBody(this, HttpMethod.PUT, url);
  }


  /**
   * Returns a builder for a GET request.
   */
  public AdvancedHttpRequest get(String url) {
    return new AdvancedHttpRequest(this, HttpMethod.GET, url);
  }
}
