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



package sonia.scm.repository;

//~--- non-JDK imports --------------------------------------------------------

import com.google.inject.Inject;

import sonia.scm.plugin.ext.Extension;

//~--- JDK imports ------------------------------------------------------------

import java.io.File;

/**
 * Simple {@link HealthCheck} for git repositories.
 *
 * @author Sebastian Sdorra
 * @since 1.39
 */
@Extension
public class GitHealthCheck extends DirectoryHealthCheck
{

  /** Field description */
  private static final HealthCheckFailure COULD_NOT_FIND_GIT_DIRECTORIES =
    new HealthCheckFailure("AKOdhQ0pw1",
      "Could not find .git or refs directory",
      "The git repository does not contain a .git or a refs directory.");

  /** Field description */
  private static final String DIRECTORY_DOT_GIT = ".git";

  /** Field description */
  private static final String DIRECTORY_REFS = "refs";

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   *
   * @param repositoryManager
   */
  @Inject
  public GitHealthCheck(RepositoryManager repositoryManager)
  {
    super(repositoryManager);
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param repository
   * @param directory
   *
   * @return
   */
  @Override
  protected HealthCheckResult check(Repository repository, File directory)
  {
    HealthCheckResult result = HealthCheckResult.healthy();

    if (!isGitRepository(directory))
    {
      result = HealthCheckResult.unhealthy(COULD_NOT_FIND_GIT_DIRECTORIES);
    }

    return result;
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Returns {@code true} if the repository is from type git.
   *
   *
   * @param repository repository for the health check
   *
   * @return {@code true} for a mercurial git
   */
  @Override
  protected boolean isCheckResponsible(Repository repository)
  {
    return GitRepositoryHandler.TYPE_NAME.equalsIgnoreCase(
      repository.getType());
  }

  /**
   * Returns {@code true} if the directory contains a .git directory or a refs
   * directory (bare git repository).
   *
   *
   * @param directory git repository directory
   *
   * @return {@code true} if the directory contains a git repository
   */
  private boolean isGitRepository(File directory)
  {
    return new File(directory, DIRECTORY_DOT_GIT).exists()
      || new File(directory, DIRECTORY_REFS).exists();
  }
}
