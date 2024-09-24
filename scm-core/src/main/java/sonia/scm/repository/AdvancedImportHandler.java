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

/**
 * Searches and import existing repositories. The {@link AdvancedImportHandler}
 * gives more control over the result of the import as the
 * {@link ImportHandler}.
 *
 * @since 1.43
 * @deprecated
 */
@Deprecated
public interface AdvancedImportHandler extends ImportHandler
{

  /**
   * Import existing and non managed repositories. Returns result which
   * contains names of the successfully imported directories and the names of
   * the failed directories
   *
   *
   * @param manager The global {@link RepositoryManager}
   *
   * @return result which contains names of the successfully imported
   *   directories and the names of the failed directories.
   */
  public ImportResult importRepositoriesFromDirectory(
    RepositoryManager manager);
}
