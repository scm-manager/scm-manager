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

import com.github.sdorra.ssp.PermissionActionCheck;
import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.NotFoundException;

import java.util.Set;


public final class HealthChecker {

  private static final Logger logger =
    LoggerFactory.getLogger(HealthChecker.class);

  private final Set<HealthCheck> checks;

  private final RepositoryManager repositoryManager;

  @Inject
  public HealthChecker(Set<HealthCheck> checks,
                       RepositoryManager repositoryManager) {
    this.checks = checks;
    this.repositoryManager = repositoryManager;
  }

  public void check(String id){
    RepositoryPermissions.healthCheck(id).check();

    Repository repository = repositoryManager.get(id);

    if (repository == null) {
      throw new NotFoundException(Repository.class, id);
    }

    doCheck(repository);
  }

  public void check(Repository repository)
  {
    RepositoryPermissions.healthCheck(repository).check();

    doCheck(repository);
  }

  public void checkAll() {
    logger.debug("check health of all repositories");

    PermissionActionCheck<Repository> check = RepositoryPermissions.healthCheck();

    for (Repository repository : repositoryManager.getAll()) {
      if (check.isPermitted(repository)) {
        try {
          check(repository);
        } catch (NotFoundException ex) {
          logger.error("health check ends with exception", ex);
        }
      } else {
        logger.debug(
          "no permissions to execute health check for repository {}",
          repository);
      }
    }
  }

  private void doCheck(Repository repository){
    logger.info("start health check for repository {}", repository);

    HealthCheckResult result = HealthCheckResult.healthy();

    for (HealthCheck check : checks) {
      logger.trace("execute health check {} for repository {}",
        check.getClass(), repository);
      result = result.merge(check.check(repository));
    }

    if (result.isUnhealthy()) {
      logger.warn("repository {} is unhealthy: {}", repository,
        result);
    } else {
      logger.info("repository {} is healthy", repository);
    }

    if (!(repository.isHealthy() && result.isHealthy())) {
      logger.trace("store health check results for repository {}",
        repository);
      repository.setHealthCheckFailures(
        ImmutableList.copyOf(result.getFailures()));
      repositoryManager.modify(repository);
    }
  }


}
