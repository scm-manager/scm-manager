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

import jakarta.inject.Inject;

public class DefaultHealthCheckService implements HealthCheckService {

  private final HealthChecker healthChecker;

  @Inject
  public DefaultHealthCheckService(HealthChecker healthChecker) {
    this.healthChecker = healthChecker;
  }

  @Override
  public void fullCheck(String id) {
    RepositoryPermissions.healthCheck(id).check();
    healthChecker.fullCheck(id);
  }

  @Override
  public void fullCheck(Repository repository) {
    RepositoryPermissions.healthCheck(repository).check();
    healthChecker.fullCheck(repository);
  }

  @Override
  public boolean checkRunning(String repositoryId) {
    return healthChecker.checkRunning(repositoryId);
  }

  @Override
  public boolean checkRunning(Repository repository) {
    return healthChecker.checkRunning(repository.getId());
  }
}
