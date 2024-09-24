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

package sonia.scm.repository.api;


import sonia.scm.repository.Repository;

/**
 * This exception is throw if no {@link RepositoryService}
 * is available for the given {@link Repository}.
 *
 * @since 1.17
 */
public final class RepositoryServiceNotFoundException extends RuntimeException
{
  private Repository repository;

  public RepositoryServiceNotFoundException(Repository repository)
  {
    super("could not find a repository service provider implementation for repository "
      .concat(repository.getName()));
    this.repository = repository;
  }


  /**
   * Returns the unsupported repository.
   */
  public Repository getRepository()
  {
    return repository;
  }

}
