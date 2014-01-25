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

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;

import org.apache.shiro.SecurityUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sonia.scm.security.Role;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;

import java.util.Set;

/**
 *
 * @author Sebastian Sdorra
 */
public final class HealthChecker
{

  /**
   * the logger for HealthChecker
   */
  private static final Logger logger =
    LoggerFactory.getLogger(HealthChecker.class);

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   *
   * @param checks
   * @param repositoryManager
   */
  @Inject
  public HealthChecker(Set<HealthCheck> checks,
    RepositoryManager repositoryManager)
  {
    this.checks = checks;
    this.repositoryManager = repositoryManager;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param id
   *
   *
   * @throws IOException
   * @throws RepositoryException
   * @throws RepositoryNotFoundException
   */
  public void check(String id)
    throws RepositoryNotFoundException, RepositoryException, IOException
  {
    SecurityUtils.getSubject().checkRole(Role.ADMIN);

    Repository repository = repositoryManager.get(id);

    if (repository == null)
    {
      throw new RepositoryNotFoundException(
        "could not find repository with id ".concat(id));
    }

    check(repository);
  }

  /**
   * Method description
   *
   *
   * @param repository
   *
   * @throws IOException
   * @throws RepositoryException
   */
  public void check(Repository repository)
    throws RepositoryException, IOException
  {
    logger.info("start health check for repository {}", repository.getName());
    SecurityUtils.getSubject().checkRole(Role.ADMIN);

    HealthCheckResult result = HealthCheckResult.healthy();

    for (HealthCheck check : checks)
    {
      logger.trace("execute health check {} for repository {}",
        check.getClass(), repository.getName());
      result = result.merge(check.check(repository));
    }

    if (result.isUnhealthy())
    {
      logger.warn("repository {} is unhealthy: {}", repository.getName(),
        result);
    }
    else
    {
      logger.info("repository {} is healthy", repository.getName());
    }

    if (!(repository.isHealthy() && result.isHealthy()))
    {
      logger.trace("store health check results for repository {}",
        repository.getName());
      repository.setHealthCheckFailures(
        ImmutableList.copyOf(result.getFailures()));
      repositoryManager.modify(repository);
    }
  }

  /**
   * Method description
   *
   *
   */
  public void checkAll()
  {
    logger.debug("check health of all repositories");
    SecurityUtils.getSubject().checkRole(Role.ADMIN);

    for (Repository repository : repositoryManager.getAll())
    {
      try
      {
        check(repository);
      }
      catch (Exception ex)
      {
        logger.error("health check ends with exception", ex);
      }
    }
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private final Set<HealthCheck> checks;

  /** Field description */
  private final RepositoryManager repositoryManager;
}
