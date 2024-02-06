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
    
package sonia.scm.repository;


import com.google.inject.Inject;

import sonia.scm.plugin.Extension;

import java.io.File;

/**
 * Simple {@link HealthCheck} for git repositories.
 *
 * @since 1.39
 */
@Extension
public final class GitHealthCheck extends DirectoryHealthCheck
{

  private static final HealthCheckFailure COULD_NOT_FIND_GIT_DIRECTORIES =
    new HealthCheckFailure("AKOdhQ0pw1",
      "Could not find .git or refs directory",
      "The git repository does not contain a .git or a refs directory.");

  private static final String DIRECTORY_DOT_GIT = ".git";

  private static final String DIRECTORY_REFS = "refs";


 
  @Inject
  public GitHealthCheck(RepositoryManager repositoryManager)
  {
    super(repositoryManager);
  }



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
