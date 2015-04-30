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

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;

/**
 * Advanced client for http operations. The {@link AdvancedHttpClient} replaces
 * the much more simpler implementation {@link sonia.scm.net.HttpClient}. The
 * {@link AdvancedHttpClient} offers a fluid interface for handling most common
 * http operations. The {@link AdvancedHttpClient} can be injected by the 
 * default injection mechanism of SCM-Manager.
 * <p>&nbsp;</p>
 * <b>Http GET example:</b>
 *
 * <pre><code>
 * AdvancedHttpResponse response = client.get("https://www.scm-manager.org")
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
 * AdvancedHttpResponse response = client.post("https://www.scm-manager.org")
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
 * @author Sebastian Sdorra
 * @since 1.46
 * 
 * @apiviz.landmark
 */
public abstract class AdvancedHttpClient
{

  /**
   * Executes the given request and returns the http response. Implementation
   * have to check, if the instance if from type 
   * {@link AdvancedHttpRequestWithBody} in order to handle request contents.
   *
   *
   * @param request request to execute
   *
   * @return http response
   *
   * @throws IOException
   */
  protected abstract AdvancedHttpResponse request(BaseHttpRequest<?> request)
    throws IOException;

  /**
   * Returns a builder for a DELETE request.
   *
   *
   * @param url request url
   *
   * @return request builder
   */
  public AdvancedHttpRequestWithBody delete(String url)
  {
    return new AdvancedHttpRequestWithBody(this, HttpMethod.DELETE, url);
  }

  /**
   * Returns a builder for a HEAD request.
   *
   *
   * @param url request url
   *
   * @return request builder
   */
  public AdvancedHttpRequest head(String url)
  {
    return new AdvancedHttpRequest(this, HttpMethod.HEAD, url);
  }

  /**
   * Returns a request builder with a custom method. <strong>Note:</strong> not 
   * every method is supported by the underlying implementation of the http 
   * client.
   *
   *
   * @param method http method
   * @param url request url
   *
   * @return request builder
   */
  public AdvancedHttpRequestWithBody method(String method, String url)
  {
    return new AdvancedHttpRequestWithBody(this, method, url);
  }

  /**
   * Returns a builder for a OPTIONS request.
   *
   *
   * @param url request url
   *
   * @return request builder
   */
  public AdvancedHttpRequestWithBody options(String url)
  {
    return new AdvancedHttpRequestWithBody(this, HttpMethod.OPTIONS, url);
  }

  /**
   * Returns a builder for a POST request.
   *
   *
   * @param url request url
   *
   * @return request builder
   */
  public AdvancedHttpRequestWithBody post(String url)
  {
    return new AdvancedHttpRequestWithBody(this, HttpMethod.POST, url);
  }

  /**
   * Returns a builder for a PUT request.
   *
   *
   * @param url request url
   *
   * @return request builder
   */
  public AdvancedHttpRequestWithBody put(String url)
  {
    return new AdvancedHttpRequestWithBody(this, HttpMethod.PUT, url);
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Returns a builder for a GET request.
   *
   *
   * @param url request url
   *
   * @return request builder
   */
  public AdvancedHttpRequest get(String url)
  {
    return new AdvancedHttpRequest(this, HttpMethod.GET, url);
  }
}
