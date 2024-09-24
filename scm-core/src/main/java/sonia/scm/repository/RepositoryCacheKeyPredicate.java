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
 * @since 1.17
 */
public class RepositoryCacheKeyPredicate<T extends RepositoryCacheKey>
  implements Predicate<T>
{
  private final String repositoryId;
 
  public RepositoryCacheKeyPredicate(String repositoryId)
  {
    this.repositoryId = repositoryId;
  }



  @Override
  public boolean apply(T item)
  {
    return repositoryId.equals(item.getRepositoryId());
  }

}
