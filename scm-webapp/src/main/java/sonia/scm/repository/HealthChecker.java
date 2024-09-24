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
import jakarta.inject.Singleton;
import org.apache.shiro.SecurityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.NotFoundException;
import sonia.scm.config.ScmConfiguration;
import sonia.scm.notifications.Notification;
import sonia.scm.notifications.NotificationSender;
import sonia.scm.notifications.Type;
import sonia.scm.repository.api.Command;
import sonia.scm.repository.api.RepositoryService;
import sonia.scm.repository.api.RepositoryServiceFactory;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static java.util.Collections.synchronizedCollection;

@Singleton
final class HealthChecker {

  private static final Logger logger =
    LoggerFactory.getLogger(HealthChecker.class);

  private final Set<HealthCheck> checks;

  private final RepositoryManager repositoryManager;
  private final RepositoryServiceFactory repositoryServiceFactory;
  private final RepositoryPostProcessor repositoryPostProcessor;

  private final Collection<String> checksRunning = synchronizedCollection(new HashSet<>());

  private final ExecutorService healthCheckExecutor = Executors.newSingleThreadExecutor();

  private final ScmConfiguration scmConfiguration;
  private final NotificationSender notificationSender;

  @Inject
  HealthChecker(Set<HealthCheck> checks,
                RepositoryManager repositoryManager,
                RepositoryServiceFactory repositoryServiceFactory,
                RepositoryPostProcessor repositoryPostProcessor,
                ScmConfiguration scmConfiguration,
                NotificationSender notificationSender) {
    this.checks = checks;
    this.repositoryManager = repositoryManager;
    this.repositoryServiceFactory = repositoryServiceFactory;
    this.repositoryPostProcessor = repositoryPostProcessor;
    this.scmConfiguration = scmConfiguration;
    this.notificationSender = notificationSender;
  }

  void lightCheck(String id) {
    RepositoryPermissions.healthCheck(id).check();

    Repository repository = loadRepository(id);

    doLightCheck(repository);
  }

  void fullCheck(String id) {
    RepositoryPermissions.healthCheck(id).check();

    Repository repository = loadRepository(id);

    doFullCheck(repository);
  }

  void lightCheck(Repository repository) {
    RepositoryPermissions.healthCheck(repository).check();

    doLightCheck(repository);
  }

  void fullCheck(Repository repository) {
    RepositoryPermissions.healthCheck(repository).check();

    doFullCheck(repository);
  }

  private Repository loadRepository(String id) {
    Repository repository = repositoryManager.get(id);

    if (repository == null) {
      throw new NotFoundException(Repository.class, id);
    }
    return repository;
  }

  void lightCheckAll() {
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
    withLockedRepository(repository, () -> {
      HealthCheckResult result = gatherLightChecks(repository);

      if (result.isUnhealthy()) {
        logger.warn("repository {} is unhealthy: {}", repository,
          result);
      } else {
        logger.info("repository {} is healthy", repository);
      }

      storeResult(repository, result);
    });
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
    withLockedRepository(repository, () ->
      runInExecutorAndWait(repository, () -> {
        HealthCheckResult lightCheckResult = gatherLightChecks(repository);
        HealthCheckResult fullCheckResult = gatherFullChecks(repository);
        HealthCheckResult result = lightCheckResult.merge(fullCheckResult);

        notifyCurrentUser(repository, result);
        storeResult(repository, result);
      })
    );
  }

  private void withLockedRepository(Repository repository, Runnable runnable) {
    if (!checksRunning.add(repository.getId())) {
      logger.debug("check for repository {} is already running", repository);
      return;
    }
    try {
      runnable.run();
    } finally {
      checksRunning.remove(repository.getId());
    }
  }

  private void runInExecutorAndWait(Repository repository, Runnable runnable) {
    try {
      healthCheckExecutor.submit(runnable).get();
    } catch (ExecutionException e) {
      logger.warn("could not submit task for health check for repository {}", repository, e);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
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
      repositoryPostProcessor.setCheckResults(repository, result.getFailures());

      notifyEmergencyContacts(repository);
    }
  }

  public boolean checkRunning(String repositoryId) {
    return checksRunning.contains(repositoryId);
  }

  private void notifyCurrentUser(Repository repository, HealthCheckResult result) {
    if (repository.isHealthy() && result.isHealthy()) {
      notificationSender.send(getHealthCheckSuccessNotification(repository));
    } else {
      notificationSender.send(getHealthCheckFailedNotification(repository));
    }
  }

  private void notifyEmergencyContacts(Repository repository) {
    String currentUser = SecurityUtils.getSubject().getPrincipal().toString();
    Set<String> emergencyContacts = scmConfiguration.getEmergencyContacts();
    for (String user : emergencyContacts) {
      if (!user.equals(currentUser)) {
        notificationSender.send(getHealthCheckFailedNotification(repository), user);
      }
    }
  }

  private Notification getHealthCheckFailedNotification(Repository repository) {
    return new Notification(Type.ERROR, "/repo/" + repository.getNamespaceAndName() + "/settings/general", "healthCheckFailed");
  }

  private Notification getHealthCheckSuccessNotification(Repository repository) {
    return new Notification(Type.SUCCESS, "/repo/" + repository.getNamespaceAndName() + "/settings/general", "healthCheckSuccess");
  }
}
