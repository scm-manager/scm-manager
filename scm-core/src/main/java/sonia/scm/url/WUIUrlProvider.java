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
 * @since 1.9
 * @author Sebastian Sdorra
 */
public class WUIUrlProvider implements UrlProvider
{

  /** Field description */
  public static final String COMPONENT_CONFIG = "scmConfig";

  /** Field description */
  public static final String COMPONENT_GROUP = "groupPanel";

  /** Field description */
  public static final String COMPONENT_REPOSITORY = "repositoryPanel";

  /** Field description */
  public static final String COMPONENT_USER = "userPanel";

  /** Field description */
  public static final String PART_INDEX = "index.html";

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   *
   * @param baseUrl
   */
  public WUIUrlProvider(String baseUrl)
  {
    this.baseUrl = HttpUtil.append(baseUrl, PART_INDEX);
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  public String getAuthenticationUrl()
  {

    // ???
    return null;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  public String getConfigUrl()
  {
    return HttpUtil.appendHash(baseUrl, COMPONENT_CONFIG);
  }

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  public ModelUrlProvider getGroupUrlProvider()
  {
    return new WUIModelUrlProvider(baseUrl, COMPONENT_GROUP);
  }

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  public RepositoryUrlProvider getRepositoryUrlProvider()
  {
    return new WUIRepositoryUrlProvider(baseUrl, COMPONENT_REPOSITORY);
  }

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  public String getStateUrl()
  {

    // ???
    return null;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  public ModelUrlProvider getUserUrlProvider()
  {
    return new WUIModelUrlProvider(baseUrl, COMPONENT_USER);
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private String baseUrl;
}
