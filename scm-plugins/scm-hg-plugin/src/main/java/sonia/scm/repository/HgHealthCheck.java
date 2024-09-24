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
 * Simple {@link HealthCheck} for mercurial repositories.
 *
 * @since 1.39
 */
@Extension
public final class HgHealthCheck extends DirectoryHealthCheck
{

  private static final HealthCheckFailure COULD_NOT_FIND_DOT_HG_DIRECTORY =
    new HealthCheckFailure("6bOdhOXpB1", "Could not find .hg directory",
      "The mercurial repository does not contain .hg directory.");

  private static final String DOT_HG = ".hg";


 
  @Inject
  public HgHealthCheck(RepositoryManager repositoryManager)
  {
    super(repositoryManager);
  }



  @Override
  protected HealthCheckResult check(Repository repository, File directory)
  {
    HealthCheckResult result = HealthCheckResult.healthy();
    File dotHgDirectory = new File(directory, DOT_HG);

    if (!dotHgDirectory.exists())
    {
      result = HealthCheckResult.unhealthy(COULD_NOT_FIND_DOT_HG_DIRECTORY);
    }

    return result;
  }


  /**
   * Returns {@code true} if the repository is from type mercurial.
   *
   *
   * @param repository repository for the health check
   *
   * @return {@code true} for a mercurial repository
   */
  @Override
  protected boolean isCheckResponsible(Repository repository)
  {
    return HgRepositoryHandler.TYPE_NAME.equalsIgnoreCase(repository.getType());
  }
}
