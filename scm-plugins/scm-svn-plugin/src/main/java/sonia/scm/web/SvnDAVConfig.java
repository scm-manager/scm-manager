/**
 * MIT License
 *
 * Copyright (c) 2020-present Cloudogu GmbH and Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package sonia.scm.web;

//~--- non-JDK imports --------------------------------------------------------

import org.tmatesoft.svn.core.internal.server.dav.CollectionRenderer;
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
   * @param collectionRenderer
   * @param repositoryProvider
   */
  public SvnDAVConfig(DAVConfig davConfig, SvnRepositoryHandler handler,
    CollectionRenderer collectionRenderer,
    RepositoryProvider repositoryProvider)
  {
    this.davConfig = davConfig;
    this.collectionRenderer = collectionRenderer;
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
  public CollectionRenderer getCollectionRenderer()
  {
    return collectionRenderer;
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
      directory = handler.getDirectory(repository.getId());
    }

    return directory;
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private final CollectionRenderer collectionRenderer;

  /** Field description */
  private final DAVConfig davConfig;

  /** Field description */
  private final SvnRepositoryHandler handler;

  /** Field description */
  private final RepositoryProvider repositoryProvider;
}
