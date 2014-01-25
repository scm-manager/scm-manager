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

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//~--- JDK imports ------------------------------------------------------------

import java.io.File;

/**
 *
 * @author Sebastian Sdorra
 * @since 1.36
 */
public abstract class DirectoryHealthCheck implements HealthCheck
{

  /** Field description */
  private static final HealthCheckFailure NO_TYPE =
    new HealthCheckFailure("2OOTx6ta71", "Repository has no type",
      "The repository does not have a configured type.");

  /** Field description */
  private static final HealthCheckFailure NO_HANDLER =
    new HealthCheckFailure("CqOTx7Jkq1", "No handler for repository type",
      "There is no registered repository handler for the type of the repository.");

  /** Field description */
  private static final HealthCheckFailure NO_DIRECTORY =
    new HealthCheckFailure("AcOTx7fD51", "handler could not return directory",
      "The repository handler was not able to return a directory for the repository");

  /** Field description */
  private static final HealthCheckFailure DIRECTORY_DOES_NOT_EXISTS =
    new HealthCheckFailure("1oOTx803F1",
      "repository directory does not exists",
      "The repository does not exists. Perhaps it was deleted outside of scm-manafer.");

  /**
   * the logger for DirectoryHealthCheck
   */
  private static final Logger logger =
    LoggerFactory.getLogger(DirectoryHealthCheck.class);

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   *
   * @param repositoryManager
   */
  protected DirectoryHealthCheck(RepositoryManager repositoryManager)
  {
    this.repositoryManager = repositoryManager;
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
  protected abstract HealthCheckResult check(Repository repository,
    File directory);

  /**
   * Method description
   *
   *
   * @param repository
   *
   * @return
   */
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

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param repository
   *
   * @return
   */
  protected boolean isCheckResponsible(Repository repository)
  {
    return true;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param repository
   *
   * @return
   */
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
          ((RepositoryDirectoryHandler) handler).getDirectory(repository);

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

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private final RepositoryManager repositoryManager;
}
