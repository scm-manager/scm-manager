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



package sonia.scm.client;

//~--- non-JDK imports --------------------------------------------------------

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sonia.scm.util.Util;

//~--- JDK imports ------------------------------------------------------------

import java.text.MessageFormat;

/**
 *
 * @author Sebastian Sdorra
 */
public class ScmUrlProvider
{

  /** Field description */
  public static final String API_PATH = "/api/rest/";

  /** Field description */
  public static final String URLPART_AUTHENTICATION = "authentication";

  /** Field description */
  public static final String URLPART_AUTHENTICATION_LOGIN =
    "authentication/login";

  /** Field description */
  public static final String URLPART_GROUP = "groups/";

  /** Field description */
  public static final String URLPART_GROUPS = "groups";

  /** Field description */
  public static final String URLPART_REPOSITORIES = "repositories";

  /** Field description */
  public static final String URLPART_REPOSITORY = "repositories/";

  /** Field description */
  public static final String URLPART_USER = "users/";

  /** Field description */
  public static final String URLPART_USERS = "users";

  /** Field description */
  public static final String URLPATTERN_BROWSE = "repositories/{0}/browse";

  /** Field description */
  public static final String URLPATTERN_CHANGESETS =
    "repositories/{0}/changesets";

  /** Field description */
  public static final String URLPATTERN_CONTENT = "repositories/{0}/content";

  /** the logger for classVar */
  private static final Logger logger =
    LoggerFactory.getLogger(ScmUrlProvider.class);

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   *
   * @param baseUrl
   */
  public ScmUrlProvider(String baseUrl)
  {
    if (!baseUrl.endsWith(API_PATH))
    {
      this.baseUrl = baseUrl.concat(API_PATH);
    }
    else
    {
      this.baseUrl = baseUrl;
    }

    if (logger.isDebugEnabled())
    {
      logger.debug("create new url provider with baseurl {}", this.baseUrl);
    }
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  public String getAuthenticationLoginUrl()
  {
    return getResourceUrl(URLPART_AUTHENTICATION_LOGIN);
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public String getAuthenticationUrl()
  {
    return getResourceUrl(URLPART_AUTHENTICATION);
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public String getBaseUrl()
  {
    return baseUrl;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public String getExtension()
  {
    return extension;
  }

  /**
   * Method description
   *
   *
   * @param name
   *
   * @return
   */
  public String getGroupUrl(String name)
  {
    return getResourceUrl(URLPART_GROUP.concat(name));
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public String getGroupsUrl()
  {
    return getResourceUrl(URLPART_GROUPS);
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public String getRepositoriesUrl()
  {
    return getResourceUrl(URLPART_REPOSITORIES);
  }

  /**
   * Method description
   *
   *
   * @param repositoryId
   * @param revision
   * @param path
   * @since 1.8
   *
   * @return
   */
  public String getRepositoryBrowseUrl(String repositoryId, String path,
          String revision)
  {
    String url = MessageFormat.format(getResourceUrl(URLPATTERN_BROWSE),
                                      repositoryId);
    String s = "?";

    if (Util.isNotEmpty(path))
    {
      url = url.concat(s).concat("path=").concat(path);
      s = "&";
    }

    if (Util.isNotEmpty(revision))
    {
      url = url.concat(s).concat("revision=").concat(revision);
    }

    return url;
  }

  /**
   * Method description
   *
   *
   * @param repositoryId
   * @since 1.8
   *
   * @param start
   * @param limit
   * @return
   */
  public String getRepositoryChangesetUrl(String repositoryId, int start,
          int limit)
  {
    String url = MessageFormat.format(getResourceUrl(URLPATTERN_CHANGESETS),
                                      repositoryId);
    String s = "?";

    if (start >= 0)
    {
      url = url.concat(s).concat("start=").concat(String.valueOf(start));
      s = "&";
    }

    if (limit > 0)
    {
      url = url.concat(s).concat("limit=").concat(String.valueOf(limit));
    }

    return url;
  }

  /**
   * Method description
   *
   *
   * @param repositoryId
   * @param path
   * @param revision
   * @since 1.8
   *
   * @return
   */
  public String getRepositoryContentUrl(String repositoryId, String path,
          String revision)
  {
    String url = MessageFormat.format(getResourceUrl(URLPATTERN_CONTENT,
                   false), repositoryId);

    url = url.concat("?path=").concat(path);

    if (Util.isNotEmpty(revision))
    {
      url = url.concat("&revision=").concat(revision);
    }

    return url;
  }

  /**
   * Method description
   *
   *
   * @param id
   *
   * @return
   */
  public String getRepositoryUrl(String id)
  {
    return getResourceUrl(URLPART_REPOSITORY.concat(id));
  }

  /**
   * Method description
   *
   *
   * @param urlPart
   *
   * @return
   */
  public String getResourceUrl(String urlPart)
  {
    return getResourceUrl(urlPart, true);
  }

  /**
   * Method description
   *
   *
   * @param urlPart
   * @param appendExtension
   * @since 1.8
   *
   * @return
   */
  public String getResourceUrl(String urlPart, boolean appendExtension)
  {
    String resourceUrl = baseUrl.concat(urlPart);

    if (appendExtension)
    {
      resourceUrl = resourceUrl.concat(extension);
    }

    if (logger.isTraceEnabled())
    {
      logger.trace("return resourceurl {}", resourceUrl);
    }

    return resourceUrl;
  }

  /**
   * Method description
   *
   *
   * @param name
   *
   * @return
   */
  public String getUserUrl(String name)
  {
    return getResourceUrl(URLPART_USER.concat(name));
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public String getUsersUrl()
  {
    return getResourceUrl(URLPART_USERS);
  }

  //~--- set methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param baseUrl
   */
  public void setBaseUrl(String baseUrl)
  {
    this.baseUrl = baseUrl;
  }

  /**
   * Method description
   *
   *
   * @param extension
   */
  public void setExtension(String extension)
  {
    this.extension = extension;
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private String baseUrl;

  /** Field description */
  private String extension = ".xml";
}
