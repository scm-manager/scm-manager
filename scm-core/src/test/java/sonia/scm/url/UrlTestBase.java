/**
 * Copyright (c) 2010, Sebastian Sdorra
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 3. Neither the name of SCM-Manager; nor the names of its
 *    contributors may be used to endorse or promote products derived from this
 *    software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * http://bitbucket.org/sdorra/scm-manager
 *
 */


package sonia.scm.url;

//~--- non-JDK imports --------------------------------------------------------

import sonia.scm.util.HttpUtil;

/**
 *
 * @author Sebastian Sdorra
 */
public abstract class UrlTestBase
{

  /** Field description */
  public static final String EXTENSION_JSON = ".json";

  /** Field description */
  public static final String EXTENSION_XML = ".xml";

  /** Field description */
  public static final String URLSUFFIX_INDEX = "/index.html";

  /** Field description */
  public static final String URLSUFFIX_RESTAPI = "/api/rest/";

  /** Field description */
  protected static final String BASEURL = "http://scm.scm-manager.org/scm";

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param baseUrl
   *
   * @return
   */
  protected String createBaseRestUrl(String baseUrl)
  {
    return baseUrl.concat(URLSUFFIX_RESTAPI);
  }

  /**
   * Method description
   *
   *
   * @param baseUrl
   * @param urlPart
   * @param extension
   *
   * @return
   */
  protected String createRestUrl(String baseUrl, String urlPart,
                                 String extension)
  {
    return createBaseRestUrl(baseUrl).concat(urlPart).concat(extension);
  }

  /**
   * Method description
   *
   *
   * @param baseUrl
   *
   * @return
   */
  protected String createWuiUrl(String baseUrl)
  {
    return baseUrl.concat(URLSUFFIX_INDEX);
  }

  /**
   * Method description
   *
   *
   * @param baseUrl
   * @param param
   *
   * @return
   */
  protected String createWuiUrl(String baseUrl, String param)
  {
    return baseUrl.concat(URLSUFFIX_INDEX).concat(
        HttpUtil.SEPARATOR_HASH).concat(param);
  }

  /**
   * Method description
   *
   *
   * @param baseUrl
   *
   * @return
   */
  protected UrlProvider createWuiUrlProvider(String baseUrl)
  {
    return UrlProviderFactory.createUrlProvider(baseUrl,
            UrlProviderFactory.TYPE_WUI);
  }
}
