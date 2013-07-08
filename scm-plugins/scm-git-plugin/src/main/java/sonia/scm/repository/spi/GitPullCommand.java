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



package sonia.scm.repository.spi;

//~--- non-JDK imports --------------------------------------------------------

import com.google.common.base.Preconditions;

import org.eclipse.jgit.internal.storage.file.FileRepository;

import sonia.scm.repository.GitRepositoryHandler;
import sonia.scm.repository.GitUtil;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryException;
import sonia.scm.repository.api.PullResponse;

//~--- JDK imports ------------------------------------------------------------

import java.io.File;
import java.io.IOException;

/**
 *
 * @author Sebastian Sdorra
 */
public class GitPullCommand extends AbstractPushOrPullCommand
  implements PullCommand
{

  /**
   * Constructs ...
   *
   *
   * @param repositoryHandler
   * @param context
   * @param repository
   */
  public GitPullCommand(GitRepositoryHandler repositoryHandler,
    GitContext context, Repository repository)
  {
    super(context, repository);
    this.repositoryHandler = repositoryHandler;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param request
   *
   * @return
   *
   * @throws IOException
   * @throws RepositoryException
   */
  @Override
  public PullResponse pull(PullCommandRequest request)
    throws IOException, RepositoryException
  {
    Repository sourceRepository = getRemoteRepository(request);

    File sourceDirectory = repositoryHandler.getDirectory(sourceRepository);

    Preconditions.checkArgument(sourceDirectory.exists(),
      "source repository directory does not exists");

    File targetDirectory = repositoryHandler.getDirectory(repository);

    Preconditions.checkArgument(sourceDirectory.exists(),
      "target repository directory does not exists");

    PullResponse response = null;

    org.eclipse.jgit.lib.Repository source = null;

    try
    {
      source = new FileRepository(sourceDirectory);
      response = new PullResponse(push(source, targetDirectory));

    }
    finally
    {
      GitUtil.close(source);
    }

    return response;
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private GitRepositoryHandler repositoryHandler;
}
