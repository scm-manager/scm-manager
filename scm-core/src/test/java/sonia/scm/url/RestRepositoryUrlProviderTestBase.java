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
public abstract class RestRepositoryUrlProviderTestBase
        extends RepositoryUrlProviderTestBase
{

  /** Field description */
  public static final String URLPART_PREFIX = "repositories";

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  protected abstract String getExtension();

  /**
   * Method description
   *
   *
   * @param baseUrl
   * @param repositoryId
   * @param path
   * @param revision
   *
   * @return
   */
  @Override
  protected String getExpectedBlameUrl(String baseUrl, String repositoryId,
          String path, String revision)
  {
    return createRestUrl(
        baseUrl,
        repositoryId.concat(HttpUtil.SEPARATOR_PATH).concat("blame")).concat(
            "?path=").concat(path).concat("&revision=").concat(revision);
  }

  /**
   * Method description
   *
   *
   * @param baseUrl
   * @param repositoryId
   * @param path
   * @param revision
   *
   * @return
   */
  @Override
  protected String getExpectedBrowseUrl(String baseUrl, String repositoryId,
          String path, String revision)
  {
    return createRestUrl(
        baseUrl,
        repositoryId.concat(HttpUtil.SEPARATOR_PATH).concat("browse")).concat(
            "?path=").concat(path).concat("&revision=").concat(revision);
  }

  /**
   * Method description
   *
   *
   * @param baseUrl
   * @param repositoryId
   * @param path
   * @param revision
   * @param start
   * @param limit
   *
   * @return
   */
  @Override
  protected String getExpectedChangesetUrl(String baseUrl, String repositoryId,
          String path, String revision, int start, int limit)
  {
    return createRestUrl(
        baseUrl,
        repositoryId.concat(HttpUtil.SEPARATOR_PATH).concat(
          "changesets")).concat("?path=").concat(path).concat(
              "&revision=").concat(revision).concat("&start=").concat(
              String.valueOf(start)).concat("&limit=").concat(
              String.valueOf(limit));
  }

  /**
   * Method description
   *
   *
   * @param baseUrl
   * @param repositoryId
   * @param start
   * @param limit
   *
   * @return
   */
  @Override
  protected String getExpectedChangesetUrl(String baseUrl, String repositoryId,
          int start, int limit)
  {
    return createRestUrl(
        baseUrl,
        repositoryId.concat(HttpUtil.SEPARATOR_PATH).concat(
          "changesets")).concat("?start=").concat(String.valueOf(start)).concat(
              "&limit=").concat(String.valueOf(limit));
  }

  /**
   * Method description
   *
   *
   * @param baseUrl
   * @param repositoryId
   * @param path
   * @param revision
   *
   * @return
   */
  @Override
  protected String getExpectedContentUrl(String baseUrl, String repositoryId,
          String path, String revision)
  {
    return createRestUrl(
        baseUrl,
        "repositories".concat(HttpUtil.SEPARATOR_PATH).concat(
          repositoryId).concat(HttpUtil.SEPARATOR_PATH).concat(
          "content"), "").concat("?path=").concat(path).concat(
              "&revision=").concat(revision);
  }

  /**
   * Method description
   *
   *
   * @param baseUrl
   * @param type
   * @param name
   *
   * @return
   */
  @Override
  protected String getExpectedDetailUrl(String baseUrl, String type,
          String name)
  {
    return createRestUrl(baseUrl,
                         type.concat(HttpUtil.SEPARATOR_PATH).concat(name));
  }

  /**
   * Method description
   *
   *
   * @param baseUrl
   * @param repositoryId
   * @param revision
   *
   * @return
   */
  @Override
  protected String getExpectedDiffUrl(String baseUrl, String repositoryId,
          String revision)
  {
    return createRestUrl(
        baseUrl,
        "repositories".concat(HttpUtil.SEPARATOR_PATH).concat(
          repositoryId).concat(HttpUtil.SEPARATOR_PATH).concat(
          "diff"), "").concat("?revision=").concat(revision);
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param baseUrl
   * @param urlPart
   *
   * @return
   */
  private String createRestUrl(String baseUrl, String urlPart)
  {
    return createRestUrl(
        baseUrl,
        URLPART_PREFIX.concat(HttpUtil.SEPARATOR_PATH).concat(urlPart),
        getExtension());
  }
}
