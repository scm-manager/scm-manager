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

import sonia.scm.util.UrlBuilder;

/**
 * @since 1.9
 * @author Sebastian Sdorra
 */
public class RestRepositoryUrlProvider extends RestModelUrlProvider
  implements RepositoryUrlProvider
{

  /** Field description */
  public static final String PARAMETER_LIMIT = "limit";

  /** Field description */
  public static final String PARAMETER_PATH = "path";

  /** Field description */
  public static final String PARAMETER_REVISION = "revision";

  /** Field description */
  public static final String PARAMETER_START = "start";

  /** Field description */
  public static final String PART_BLAME = "blame";

  /** Field description */
  public static final String PART_BROWSE = "browse";

  /**
   * @since 1.12
   */
  public static final String PART_CHANGESET = "changeset";

  /** Field description */
  public static final String PART_CHANGESETS = "changesets";

  /** Field description */
  public static final String PART_CONTENT = "content";

  /** Field description */
  public static final String PART_DIFF = "diff";

  /** Field description */
  public static final String PART_TAGS = "tags";

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   *
   * @param baseUrl
   * @param modelSuffix
   * @param extension
   */
  public RestRepositoryUrlProvider(String baseUrl, String modelSuffix,
    String extension)
  {
    super(baseUrl, modelSuffix, extension);
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

    return new UrlBuilder(base).appendUrlPart(repositoryId).appendUrlPart(
      PART_BLAME).append(extension).appendParameter(
      PARAMETER_PATH, path).appendParameter(
      PARAMETER_REVISION, revision).toString();
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

    return new UrlBuilder(base).appendUrlPart(repositoryId).appendUrlPart(
      PART_BROWSE).append(extension).appendParameter(
      PARAMETER_PATH, path).appendParameter(
      PARAMETER_REVISION, revision).toString();
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

    return new UrlBuilder(base).appendUrlPart(repositoryId).appendUrlPart(
      PART_CHANGESETS).append(extension).appendParameter(
      PARAMETER_PATH, path).appendParameter(
      PARAMETER_REVISION, revision).appendParameter(
      PARAMETER_START, start).appendParameter(
      PARAMETER_LIMIT, limit).toString();
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
    return new UrlBuilder(base).appendUrlPart(repositoryId).appendUrlPart(
      PART_CHANGESETS).append(extension).appendParameter(
      PARAMETER_START, start).appendParameter(
      PARAMETER_LIMIT, limit).toString();
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

    return new UrlBuilder(base).appendUrlPart(repositoryId).appendUrlPart(
      PART_CHANGESET).appendUrlPart(revision).append(extension).toString();
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

    return new UrlBuilder(base).appendUrlPart(repositoryId).appendUrlPart(
      PART_CONTENT).appendParameter(PARAMETER_PATH, path).appendParameter(
      PARAMETER_REVISION, revision).toString();
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
    return new UrlBuilder(base).appendUrlPart(type).appendUrlPart(name).append(
      extension).toString();
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

    return new UrlBuilder(base).appendUrlPart(repositoryId).appendUrlPart(
      PART_DIFF).appendParameter(PARAMETER_REVISION, revision).toString();
  }

  /**
   * Method description
   *
   *
   * @param repositoryId
   *
   * @return
   * @since 1.18
   */
  @Override
  public String getTagsUrl(String repositoryId)
  {
    return new UrlBuilder(base).appendUrlPart(repositoryId).appendUrlPart(
      PART_TAGS).append(extension).toString();
  }
}
