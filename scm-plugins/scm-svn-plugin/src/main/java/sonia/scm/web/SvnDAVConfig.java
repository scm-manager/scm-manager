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



package sonia.scm.web;

//~--- non-JDK imports --------------------------------------------------------

import org.tmatesoft.svn.core.internal.server.dav.DAVConfig;
import org.tmatesoft.svn.core.internal.server.dav.SVNPathBasedAccess;

import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryProvider;
import sonia.scm.repository.SvnRepositoryHandler;

//~--- JDK imports ------------------------------------------------------------

import java.io.File;

/**
 *
 * @author Sebastian Sdorra
 */
public class SvnDAVConfig extends DAVConfig
{

  /**
   * Constructs ...
   *
   *
   * @param davConfig
   * @param handler
   * @param repositoryProvider
   */
  public SvnDAVConfig(DAVConfig davConfig, SvnRepositoryHandler handler,
                      RepositoryProvider repositoryProvider)
  {
    this.davConfig = davConfig;
    this.handler = handler;
    this.repositoryProvider = repositoryProvider;
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  public String getActivitiesDBPath()
  {
    return null;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  public String getRepositoryName()
  {
    return davConfig.getRepositoryName();
  }

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  public String getRepositoryParentPath()
  {
    String path = null;
    File directory = getRepositoryDirectory();

    if (directory != null)
    {
      path = directory.getParent();
    }
    else
    {
      path = davConfig.getRepositoryPath();
    }

    return path;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  public String getRepositoryPath()
  {
    String path = null;
    File directory = getRepositoryDirectory();

    if (directory != null)
    {
      path = directory.getAbsolutePath();
    }
    else
    {
      path = davConfig.getRepositoryPath();
    }

    return path;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  public SVNPathBasedAccess getSVNAccess()
  {
    return davConfig.getSVNAccess();
  }

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  public String getXSLTIndex()
  {
    return davConfig.getXSLTIndex();
  }

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  public boolean isAllowBulkUpdates()
  {
    return davConfig.isAllowBulkUpdates();
  }

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  public boolean isAllowDepthInfinity()
  {
    return davConfig.isAllowDepthInfinity();
  }

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  public boolean isAnonymousAllowed()
  {
    return davConfig.isAnonymousAllowed();
  }

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  public boolean isAutoVersioning()
  {
    return davConfig.isAutoVersioning();
  }

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  public boolean isListParentPath()
  {
    return davConfig.isListParentPath();
  }

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  public boolean isNoAuthIfAnonymousAllowed()
  {
    return davConfig.isNoAuthIfAnonymousAllowed();
  }

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  public boolean isUsingPBA()
  {
    return davConfig.isUsingPBA();
  }

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  public boolean isUsingRepositoryPathDirective()
  {
    return true;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  private File getRepositoryDirectory()
  {
    File directory = null;
    Repository repository = repositoryProvider.get();

    if (repository != null)
    {
      directory = handler.getDirectory(repository);
    }

    return directory;
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private DAVConfig davConfig;

  /** Field description */
  private SvnRepositoryHandler handler;

  /** Field description */
  private RepositoryProvider repositoryProvider;
}
