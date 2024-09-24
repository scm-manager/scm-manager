/*
 * Copyright (c) 2020 - present Cloudogu GmbH
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

package sonia.scm.web;


import org.tmatesoft.svn.core.internal.server.dav.CollectionRenderer;
import org.tmatesoft.svn.core.internal.server.dav.DAVConfig;
import org.tmatesoft.svn.core.internal.server.dav.SVNPathBasedAccess;

import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryProvider;
import sonia.scm.repository.SvnRepositoryHandler;

import java.io.File;


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


  
  @Override
  public String getActivitiesDBPath()
  {
    return null;
  }

  
  @Override
  public CollectionRenderer getCollectionRenderer()
  {
    return collectionRenderer;
  }

  
  @Override
  public String getRepositoryName()
  {
    return davConfig.getRepositoryName();
  }

  
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

  
  @Override
  public SVNPathBasedAccess getSVNAccess()
  {
    return davConfig.getSVNAccess();
  }

  
  @Override
  public String getXSLTIndex()
  {
    return davConfig.getXSLTIndex();
  }

  
  @Override
  public boolean isAllowBulkUpdates()
  {
    return davConfig.isAllowBulkUpdates();
  }

  
  @Override
  public boolean isAllowDepthInfinity()
  {
    return davConfig.isAllowDepthInfinity();
  }

  
  @Override
  public boolean isAnonymousAllowed()
  {
    return davConfig.isAnonymousAllowed();
  }

  
  @Override
  public boolean isAutoVersioning()
  {
    return davConfig.isAutoVersioning();
  }

  
  @Override
  public boolean isListParentPath()
  {
    return davConfig.isListParentPath();
  }

  
  @Override
  public boolean isNoAuthIfAnonymousAllowed()
  {
    return davConfig.isNoAuthIfAnonymousAllowed();
  }

  
  @Override
  public boolean isUsingPBA()
  {
    return davConfig.isUsingPBA();
  }

  
  @Override
  public boolean isUsingRepositoryPathDirective()
  {
    return true;
  }

  
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

  private final CollectionRenderer collectionRenderer;

  private final DAVConfig davConfig;

  private final SvnRepositoryHandler handler;

  private final RepositoryProvider repositoryProvider;
}
