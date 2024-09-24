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


import com.google.common.base.Predicate;

/**
 * Used to filter collections of repositories by its type.
 *
 * @since 1.16
 */
public class RepositoryTypePredicate implements Predicate<Repository>
{
  /** type to filter */
  private String type;

  public RepositoryTypePredicate(String type)
  {
    this.type = type;
  }


  /**
   * Return true if the repository is from the given type.
   *
   * @param repository repository to check
   */
  @Override
  public boolean apply(Repository repository)
  {
    return type.equalsIgnoreCase(repository.getType());
  }

}
