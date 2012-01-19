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
public class WUIRepositoryUrlProvider extends WUIModelUrlProvider
        implements RepositoryUrlProvider
{

  /** Field description */
  public static final String COMPONENT_BROWSER = "repositoryBrowser";

  /** Field description */
  public static final String COMPONENT_CHANGESETS =
    "repositoryChangesetViewerPanel";

  /** Field description */
  public static final String COMPONENT_CONTENT = "contentPanel";

  /** Field description */
  public static final String COMPONENT_DETAIL = "repositoryPanel";

  /** Field description */
  public static final String COMPONENT_DIFF = "diffPanel";

  /** Field description */
  public static final String VIEW_BLAME = "blame";

  /**
   * @since 1.12
   */
  public static final String VIEW_CHANGESET = "changeset";

  /** Field description */
  public static final String VIEW_CONTENT = "content";

  /** Field description */
  public static final String VIEW_HISTORY = "history";

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   *
   * @param baseUrl
   * @param component
   */
  public WUIRepositoryUrlProvider(String baseUrl, String component)
  {
    super(baseUrl, component);
    this.baseUrl = baseUrl;
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param repositoryId
   * @param path
   * @param revision
   *
   * @return
   */
  @Override
  public String getBlameUrl(String repositoryId, String path, String revision)
  {
    revision = UrlUtil.fixRevision(revision);

    return new WUIUrlBuilder(baseUrl, COMPONENT_CONTENT).append(
        repositoryId).append(revision).append(path).append(
        VIEW_BLAME).toString();
  }

  /**
   * Method description
   *
   *
   * @param repositoryId
   * @param path
   * @param revision
   *
   * @return
   */
  @Override
  public String getBrowseUrl(String repositoryId, String path, String revision)
  {
    revision = UrlUtil.fixRevision(revision);

    return new WUIUrlBuilder(baseUrl, COMPONENT_BROWSER).append(
        repositoryId).append(revision).append(path).toString();
  }

  /**
   * Method description
   *
   *
   * @param repositoryId
   * @param path
   * @param revision
   * @param start
   * @param limit
   *
   * @return
   */
  @Override
  public String getChangesetUrl(String repositoryId, String path,
                                String revision, int start, int limit)
  {
    revision = UrlUtil.fixRevision(revision);

    // TODO handle start and limit
    return new WUIUrlBuilder(baseUrl, COMPONENT_CONTENT).append(
        repositoryId).append(revision).append(path).append(
        VIEW_HISTORY).toString();
  }

  /**
   * Method description
   *
   *
   * @param repositoryId
   * @param start
   * @param limit
   *
   * @return
   */
  @Override
  public String getChangesetUrl(String repositoryId, int start, int limit)
  {
    return new WUIUrlBuilder(baseUrl, COMPONENT_CHANGESETS).append(
        repositoryId).append(start).append(limit).toString();
  }

  /**
   * Method description
   *
   *
   * @param repositoryId
   * @param revision
   *
   * @return
   *
   * @since 1.12
   */
  @Override
  public String getChangesetUrl(String repositoryId, String revision)
  {
    revision = UrlUtil.fixRevision(revision);

    return new WUIUrlBuilder(baseUrl, VIEW_CHANGESET).append(
        repositoryId).append(revision).toString();
  }

  /**
   * Method description
   *
   *
   * @param repositoryId
   * @param path
   * @param revision
   *
   * @return
   */
  @Override
  public String getContentUrl(String repositoryId, String path, String revision)
  {
    revision = UrlUtil.fixRevision(revision);

    return new WUIUrlBuilder(baseUrl, COMPONENT_CONTENT).append(
        repositoryId).append(revision).append(path).append(
        VIEW_HISTORY).toString();
  }

  /**
   * Method description
   *
   *
   * @param type
   * @param name
   *
   * @return
   * @since 1.11
   */
  @Override
  public String getDetailUrl(String type, String name)
  {
    name = type.concat(HttpUtil.SEPARATOR_PATH).concat(name);

    return new WUIUrlBuilder(baseUrl, COMPONENT_DETAIL).append(name).toString();
  }

  /**
   * Method description
   *
   *
   * @param repositoryId
   * @param revision
   *
   * @return
   */
  @Override
  public String getDiffUrl(String repositoryId, String revision)
  {
    revision = UrlUtil.fixRevision(revision);

    return new WUIUrlBuilder(baseUrl, COMPONENT_DIFF).append(
        repositoryId).append(revision).toString();
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private String baseUrl;
}
