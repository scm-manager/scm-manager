/*
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
