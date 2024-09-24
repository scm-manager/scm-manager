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


import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import jakarta.servlet.http.HttpServletRequest;
import org.eclipse.jgit.errors.RepositoryNotFoundException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.transport.resolver.RepositoryResolver;
import org.eclipse.jgit.transport.resolver.ServiceNotEnabledException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.repository.GitConfig;
import sonia.scm.repository.GitRepositoryHandler;
import sonia.scm.repository.GitUtil;
import sonia.scm.repository.RepositoryProvider;

import java.io.File;
import java.io.IOException;


public class GitRepositoryResolver implements RepositoryResolver<HttpServletRequest>
{
  private static final Logger logger = LoggerFactory.getLogger(GitRepositoryResolver.class);

  private final GitRepositoryHandler handler;
  private final RepositoryProvider repositoryProvider;

  @Inject
  public GitRepositoryResolver(GitRepositoryHandler handler, RepositoryProvider repositoryProvider)
  {
    this.handler = handler;
    this.repositoryProvider = repositoryProvider;
  }


  /**
   * @return jgit repository
   *
   */
  @Override
  public Repository open(HttpServletRequest request, String repositoryName) throws RepositoryNotFoundException, ServiceNotEnabledException
  {
    try
    {
      sonia.scm.repository.Repository repo = repositoryProvider.get();

      Preconditions.checkState(repo != null, "repository to handle not found");
      Preconditions.checkState(GitRepositoryHandler.TYPE_NAME.equals(repo.getType()), "got a non git repository in GitRepositoryResolver of type " + repo.getType());

      GitConfig config = handler.getConfig();

      if (config.isValid())
      {
        File gitdir = handler.getDirectory(repo.getId());
        if (gitdir == null) {
          throw new RepositoryNotFoundException(repositoryName);
        }

        logger.debug("try to open git repository at {}", gitdir);

        return GitUtil.open(gitdir);
      }
      else
      {
        if (logger.isWarnEnabled())
        {
          logger.warn("gitconfig is not valid, the service is not available");
        }

        throw new ServiceNotEnabledException();
      }
    }
    catch (IOException e)
    {
      // REVIEW
      throw new RepositoryNotFoundException(repositoryName, e);
    }
  }

}
