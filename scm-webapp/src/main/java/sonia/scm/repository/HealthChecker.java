/**
 * Copyright (c) 2010, Sebastian Sdorra All rights reserved.
 * <p>
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * <p>
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer. 2. Redistributions in
 * binary form must reproduce the above copyright notice, this list of
 * conditions and the following disclaimer in the documentation and/or other
 * materials provided with the distribution. 3. Neither the name of SCM-Manager;
 * nor the names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 * <p>
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
 * <p>
 * http://bitbucket.org/sdorra/scm-manager
 */

package sonia.scm.repository;

import com.github.sdorra.ssp.PermissionActionCheck;
import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.ConcurrentModificationException;
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

  public void check(String id) throws NotFoundException {
    RepositoryPermissions.healthCheck(id).check();

    Repository repository = repositoryManager.get(id);

    if (repository == null) {
      throw new RepositoryNotFoundException(
        "could not find repository with id ".concat(id));
    }

    doCheck(repository);
  }

  public void check(Repository repository)
    throws NotFoundException, ConcurrentModificationException {
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
        } catch (ConcurrentModificationException | NotFoundException ex) {
          logger.error("health check ends with exception", ex);
        }
      } else {
        logger.debug(
          "no permissions to execute health check for repository {}",
          repository.getId());
      }
    }
  }

  private void doCheck(Repository repository) throws NotFoundException {
    logger.info("start health check for repository {}", repository.getName());

    HealthCheckResult result = HealthCheckResult.healthy();

    for (HealthCheck check : checks) {
      logger.trace("execute health check {} for repository {}",
        check.getClass(), repository.getName());
      result = result.merge(check.check(repository));
    }

    if (result.isUnhealthy()) {
      logger.warn("repository {} is unhealthy: {}", repository.getName(),
        result);
    } else {
      logger.info("repository {} is healthy", repository.getName());
    }

    if (!(repository.isHealthy() && result.isHealthy())) {
      logger.trace("store health check results for repository {}",
        repository.getName());
      repository.setHealthCheckFailures(
        ImmutableList.copyOf(result.getFailures()));
      repositoryManager.modify(repository);
    }
  }


}
