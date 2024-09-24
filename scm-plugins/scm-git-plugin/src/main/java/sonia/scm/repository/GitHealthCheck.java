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
