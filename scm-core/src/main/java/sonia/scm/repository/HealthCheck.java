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


import sonia.scm.plugin.ExtensionPoint;

/**
 * Repository health check. Executes a check to verify the health
 * state of a repository.
 *
 * @since 1.36
 */
@ExtensionPoint(multi = true)
public interface HealthCheck
{

  /**
   * Returns the result of the repository health check.
   *
   * @param repository repository to check
   */
  HealthCheckResult check(Repository repository);
}
