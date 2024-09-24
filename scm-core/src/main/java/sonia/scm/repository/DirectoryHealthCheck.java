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


import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 *
 * @since 1.36
 */
public abstract class DirectoryHealthCheck implements HealthCheck
{

  private static final HealthCheckFailure NO_TYPE =
    new HealthCheckFailure("2OOTx6ta71", "Repository has no type",
      "The repository does not have a configured type.");

  private static final HealthCheckFailure NO_HANDLER =
    new HealthCheckFailure("CqOTx7Jkq1", "No handler for repository type",
      "There is no registered repository handler for the type of the repository.");

  private static final HealthCheckFailure NO_DIRECTORY =
    new HealthCheckFailure("AcOTx7fD51", "handler could not return directory",
      "The repository handler was not able to return a directory for the repository");

  private static final HealthCheckFailure DIRECTORY_DOES_NOT_EXISTS =
    new HealthCheckFailure("1oOTx803F1",
      "repository directory does not exists",
      "The repository does not exists. Perhaps it was deleted outside of scm-manager.");

 
  private static final Logger logger =
    LoggerFactory.getLogger(DirectoryHealthCheck.class);

  private final RepositoryManager repositoryManager;
 
  protected DirectoryHealthCheck(RepositoryManager repositoryManager)
  {
    this.repositoryManager = repositoryManager;
  }



  protected abstract HealthCheckResult check(Repository repository,
    File directory);


  @Override
  public HealthCheckResult check(Repository repository)
  {
    Preconditions.checkNotNull(repository, "repository is required");

    HealthCheckResult result = HealthCheckResult.healthy();

    if (isCheckResponsible(repository))
    {
      result = doCheck(repository);
    }
    else if (logger.isDebugEnabled())
    {
      logger.debug("check is not responsible for repository {}",
        repository.getName());
    }

    return result;
  }



  protected boolean isCheckResponsible(Repository repository)
  {
    return true;
  }



  private HealthCheckResult doCheck(Repository repository)
  {
    HealthCheckResult result;
    String repositoryType = repository.getType();

    if (Strings.isNullOrEmpty(repositoryType))
    {
      result = HealthCheckResult.unhealthy(NO_TYPE);
    }
    else
    {
      RepositoryHandler handler = repositoryManager.getHandler(repositoryType);

      if (handler == null)
      {
        result = HealthCheckResult.unhealthy(NO_HANDLER);
      }
      else if (handler instanceof RepositoryDirectoryHandler)
      {
        File directory =
          ((RepositoryDirectoryHandler) handler).getDirectory(repository.getId());

        if (directory == null)
        {
          result = HealthCheckResult.unhealthy(NO_DIRECTORY);
        }
        else if (!directory.exists())
        {
          result = HealthCheckResult.unhealthy(DIRECTORY_DOES_NOT_EXISTS);
        }
        else
        {
          result = check(repository, directory);
        }
      }
      else
      {
        logger.debug(
          "repository handler {} does not act on direcotries, returning healthy",
          repositoryType);
        result = HealthCheckResult.healthy();
      }
    }

    return result;
  }

}
