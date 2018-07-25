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

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import org.eclipse.jgit.errors.RepositoryNotFoundException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.RepositoryCache;
import org.eclipse.jgit.lib.RepositoryCache.FileKey;
import org.eclipse.jgit.transport.resolver.RepositoryResolver;
import org.eclipse.jgit.transport.resolver.ServiceNotAuthorizedException;
import org.eclipse.jgit.transport.resolver.ServiceNotEnabledException;
import org.eclipse.jgit.util.FS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.repository.GitConfig;
import sonia.scm.repository.GitRepositoryHandler;
import sonia.scm.repository.RepositoryProvider;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;

//~--- JDK imports ------------------------------------------------------------

/**
 *
 * @author Sebastian Sdorra
 */
public class GitRepositoryResolver implements RepositoryResolver<HttpServletRequest>
{

  /** the logger for GitRepositoryResolver */
  private static final Logger logger = LoggerFactory.getLogger(GitRepositoryResolver.class);

  //~--- constructors ---------------------------------------------------------

  @Inject
  public GitRepositoryResolver(GitRepositoryHandler handler, RepositoryProvider repositoryProvider)
  {
    this.handler = handler;
    this.repositoryProvider = repositoryProvider;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param request
   * @param repositoryName
   *
   * @return
   *
   * @throws RepositoryNotFoundException
   * @throws ServiceNotAuthorizedException
   * @throws ServiceNotEnabledException
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
        File gitdir = findRepository(config.getRepositoryDirectory(), repo.getId());
        if (gitdir == null) {
          throw new RepositoryNotFoundException(repositoryName);
        }

        logger.debug("try to open git repository at {}", gitdir);

        return RepositoryCache.open(FileKey.lenient(gitdir, FS.DETECTED), true);
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
    catch (RuntimeException | IOException e)
    {
      throw new RepositoryNotFoundException(repositoryName, e);
    }
  }

  @VisibleForTesting
  File findRepository(File parentDirectory, String repositoryName) {
    File repositoryDirectory = new File(parentDirectory, repositoryName);
    if (repositoryDirectory.exists()) {
      return repositoryDirectory;
    }
    
    if (endsWithDotGit(repositoryName)) {
      String repositoryNameWithoutDotGit = repositoryNameWithoutDotGit(repositoryName);
      repositoryDirectory = new File(parentDirectory, repositoryNameWithoutDotGit);
      if (repositoryDirectory.exists()) {
        return repositoryDirectory;
      }
    }
    
    return null;
  }
  
  private boolean endsWithDotGit(String repositoryName) {
    return repositoryName.endsWith(GitRepositoryHandler.DOT_GIT);
  }
  
  private String repositoryNameWithoutDotGit(String repositoryName) {
    return repositoryName.substring(0, repositoryName.length() - GitRepositoryHandler.DOT_GIT.length());
  }

  //~--- fields ---------------------------------------------------------------

  private final GitRepositoryHandler handler;
  private final RepositoryProvider repositoryProvider;
}
