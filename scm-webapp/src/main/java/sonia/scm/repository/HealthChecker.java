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

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.NotFoundException;
import sonia.scm.repository.api.Command;
import sonia.scm.repository.api.RepositoryService;
import sonia.scm.repository.api.RepositoryServiceFactory;

import java.io.IOException;
import java.util.Set;

public final class HealthChecker {

  private static final Logger logger =
    LoggerFactory.getLogger(HealthChecker.class);

  private final Set<HealthCheck> checks;

  private final RepositoryManager repositoryManager;
  private final RepositoryServiceFactory repositoryServiceFactory;

  @Inject
  public HealthChecker(Set<HealthCheck> checks,
                       RepositoryManager repositoryManager, RepositoryServiceFactory repositoryServiceFactory) {
    this.checks = checks;
    this.repositoryManager = repositoryManager;
    this.repositoryServiceFactory = repositoryServiceFactory;
  }

  public void lightCheck(String id) {
    RepositoryPermissions.healthCheck(id).check();

    Repository repository = loadRepository(id);

    doLightCheck(repository);
  }

  public void fullCheck(String id) {
    RepositoryPermissions.healthCheck(id).check();

    Repository repository = loadRepository(id);

    doFullCheck(repository);
  }

  private Repository loadRepository(String id) {
    Repository repository = repositoryManager.get(id);

    if (repository == null) {
      throw new NotFoundException(Repository.class, id);
    }
    return repository;
  }

  public void lightCheck(Repository repository) {
    RepositoryPermissions.healthCheck(repository).check();

    doLightCheck(repository);
  }

  public void fullCheck(Repository repository) {
    RepositoryPermissions.healthCheck(repository).check();

    doFullCheck(repository);
  }

  public void lightCheckAll() {
    logger.debug("check health of all repositories");

    for (Repository repository : repositoryManager.getAll()) {
      if (RepositoryPermissions.healthCheck().isPermitted(repository)) {
        try {
          lightCheck(repository);
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

  private void doLightCheck(Repository repository) {
    HealthCheckResult result = gatherLightChecks(repository);

    if (result.isUnhealthy()) {
      logger.warn("repository {} is unhealthy: {}", repository,
        result);
    } else {
      logger.info("repository {} is healthy", repository);
    }

    storeResult(repository, result);
  }

  private HealthCheckResult gatherLightChecks(Repository repository) {
    logger.info("start light health check for repository {}", repository);

    HealthCheckResult result = HealthCheckResult.healthy();

    for (HealthCheck check : checks) {
      logger.trace("execute light health check {} for repository {}",
        check.getClass(), repository);
      result = result.merge(check.check(repository));
    }
    return result;
  }

  private void doFullCheck(Repository repository) {
    HealthCheckResult lightCheckResult = gatherLightChecks(repository);
    HealthCheckResult fullCheckResult = gatherFullChecks(repository);
    HealthCheckResult result = lightCheckResult.merge(fullCheckResult);

    storeResult(repository, result);
  }

  private HealthCheckResult gatherFullChecks(Repository repository) {
    try (RepositoryService service = repositoryServiceFactory.create(repository)) {
      if (service.isSupported(Command.FULL_HEALTH_CHECK)) {
        return service.getFullCheckCommand().check();
      } else {
        return HealthCheckResult.healthy();
      }
    } catch (IOException e) {
      throw new InternalRepositoryException(repository, "error during full health check", e);
    }
  }

  private void storeResult(Repository repository, HealthCheckResult result) {
    if (!(repository.isHealthy() && result.isHealthy())) {
      logger.trace("store health check results for repository {}",
        repository);
      repository.setHealthCheckFailures(
        ImmutableList.copyOf(result.getFailures()));
      repositoryManager.modify(repository);
    }
  }
}
