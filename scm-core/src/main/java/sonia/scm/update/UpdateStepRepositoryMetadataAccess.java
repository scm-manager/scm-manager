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

package sonia.scm.update;

import sonia.scm.repository.Repository;

/**
 * Use this in {@link sonia.scm.migration.UpdateStep}s only to read repository objects directly from locations given by
 * {@link sonia.scm.repository.RepositoryLocationResolver}.
 */
public interface UpdateStepRepositoryMetadataAccess<T> {
  /**
   * Reads the repository from the given location.
   * @param location the location to read from
   * @return the repository
   * @throws sonia.scm.repository.InternalRepositoryException if the repository could not be read
   */
  Repository read(T location);
}
