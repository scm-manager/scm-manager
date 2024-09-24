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

package sonia.scm.web.api;

import de.otto.edison.hal.HalRepresentation;
import sonia.scm.repository.Repository;

/**
 * Maps a repository to a hal representation.
 * This is especially useful if a plugin would deliver a repository to the frontend.
 *
 * @since 2.0.0
 */
public interface RepositoryToHalMapper {

  /**
   * Returns the hal representation of the repository.
   *
   * @param repository repository to map
   * @return hal representation
   */
  HalRepresentation map(Repository repository);
}
