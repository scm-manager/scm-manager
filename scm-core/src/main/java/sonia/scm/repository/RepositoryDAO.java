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


import sonia.scm.GenericDAO;

/**
 * Data access object for repositories. This class should only used by the
 * {@link RepositoryManager}. Plugins and other classes should use the
 * {@link RepositoryManager} instead.
 *
 * @since 1.14
 */
public interface RepositoryDAO extends GenericDAO<Repository>
{

  /**
   * Returns true if a repository with specified
   * namespace and name exists in the backend.
   *
   *
   * @param namespaceAndName namespace and name of the repository
   *
   * @return true if the repository exists
   */
  boolean contains(NamespaceAndName namespaceAndName);


  /**
   * Returns the repository with the specified namespace and name or null
   * if no such repository exists in the backend.
   *
   * @return repository with the specified namespace and name or null
   */
  Repository get(NamespaceAndName namespaceAndName);
}
