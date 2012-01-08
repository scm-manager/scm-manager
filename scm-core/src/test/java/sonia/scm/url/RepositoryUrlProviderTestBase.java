/**
 * Copyright (c) 2010, Sebastian Sdorra All rights reserved.
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



package sonia.scm.url;

//~--- non-JDK imports --------------------------------------------------------

import org.junit.Test;

import static org.junit.Assert.*;

/**
 *
 * @author Sebastian Sdorra
 */
public abstract class RepositoryUrlProviderTestBase extends UrlTestBase
{

  /** Field description */
  private static final String NAME = "scm/main";

  /** Field description */
  private static final String PATH = "scm-webapp/pom.xml";

  /** Field description */
  private static final String REPOSITORY_ID =
    "E3882BE7-7D0D-421B-B178-B2AA9E897135";

  /** Field description */
  private static final String REVISION = "b282fb2dd12a";

  /** Field description */
  private static final String TYPE = "hg";

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param baseUrl
   *
   * @return
   */
  protected abstract RepositoryUrlProvider createRepositoryUrlProvider(
          String baseUrl);

  //~--- get methods ----------------------------------------------------------

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
  protected abstract String getExpectedBlameUrl(String baseUrl,
          String repositoryId, String path, String revision);

  /**
   * Method description
   *
   *
   *
   * @param baseUrl
   * @param repositoryId
   * @param path
   * @param revision
   *
   * @return
   */
  protected abstract String getExpectedBrowseUrl(String baseUrl,
          String repositoryId, String path, String revision);

  /**
   * Method description
   *
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
  protected abstract String getExpectedChangesetUrl(String baseUrl,
          String repositoryId, String path, String revision, int start,
          int limit);

  /**
   * Method description
   *
   *
   *
   * @param baseUrl
   * @param repositoryId
   * @param start
   * @param limit
   *
   * @return
   */
  protected abstract String getExpectedChangesetUrl(String baseUrl,
          String repositoryId, int start, int limit);

  /**
   * Method description
   *
   *
   *
   * @param baseUrl
   * @param repositoryId
   * @param path
   * @param revision
   *
   * @return
   */
  protected abstract String getExpectedContentUrl(String baseUrl,
          String repositoryId, String path, String revision);

  /**
   * Method description
   *
   *
   *
   * @param baseUrl
   * @param type
   * @param name
   *
   * @return
   * @since 1.11
   */
  protected abstract String getExpectedDetailUrl(String baseUrl, String type,
          String name);

  /**
   * Method description
   *
   *
   *
   * @param baseUrl
   * @param repositoryId
   * @param revision
   *
   * @return
   */
  protected abstract String getExpectedDiffUrl(String baseUrl,
          String repositoryId, String revision);

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   */
  @Test
  public void testGetBlameUrl()
  {
    assertEquals(
        getExpectedBlameUrl(BASEURL, REPOSITORY_ID, PATH, REVISION),
        createRepositoryUrlProvider(BASEURL).getBlameUrl(
          REPOSITORY_ID, PATH, REVISION));
  }

  /**
   * Method description
   *
   */
  @Test
  public void testGetBrowserUrl()
  {
    assertEquals(
        getExpectedBrowseUrl(BASEURL, REPOSITORY_ID, PATH, REVISION),
        createRepositoryUrlProvider(BASEURL).getBrowseUrl(
          REPOSITORY_ID, PATH, REVISION));
  }

  /**
   * Method description
   *
   */
  @Test
  public void testGetChangesetUrl()
  {
    assertEquals(
        getExpectedChangesetUrl(BASEURL, REPOSITORY_ID, PATH, REVISION, 0, 20),
        createRepositoryUrlProvider(BASEURL).getChangesetUrl(
          REPOSITORY_ID, PATH, REVISION, 0, 20));
    assertEquals(
        getExpectedChangesetUrl(BASEURL, REPOSITORY_ID, 0, 20),
        createRepositoryUrlProvider(BASEURL).getChangesetUrl(
          REPOSITORY_ID, 0, 20));
  }

  /**
   * Method description
   *
   */
  @Test
  public void testGetContentUrl()
  {
    assertEquals(
        getExpectedContentUrl(BASEURL, REPOSITORY_ID, PATH, REVISION),
        createRepositoryUrlProvider(BASEURL).getContentUrl(
          REPOSITORY_ID, PATH, REVISION));
  }

  /**
   * Method description
   *
   */
  @Test
  public void testGetDetailUrl()
  {
    assertEquals(getExpectedDetailUrl(BASEURL, TYPE, NAME),
                 createRepositoryUrlProvider(BASEURL).getDetailUrl(TYPE, NAME));
  }

  /**
   * Method description
   *
   */
  @Test
  public void testGetDiffUrl()
  {
    assertEquals(getExpectedDiffUrl(BASEURL, REPOSITORY_ID, REVISION),
                 createRepositoryUrlProvider(BASEURL).getDiffUrl(REPOSITORY_ID,
                   REVISION));
  }
}
