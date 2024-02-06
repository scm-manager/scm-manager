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
