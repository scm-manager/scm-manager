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

import java.io.IOException;
import java.util.List;

/**
 * Searches and import existing repositories.
 *
 * @since 1.12
 * @deprecated
 */
@Deprecated
public interface ImportHandler {

  /**
   * Import existing and non managed repositories.
   *
   * @param manager The global {@link RepositoryManager}
   * @return a {@link List} names of imported repositories
   * @throws IOException
   * @deprecated
   */
  public List<String> importRepositories(RepositoryManager manager) throws IOException;
}
