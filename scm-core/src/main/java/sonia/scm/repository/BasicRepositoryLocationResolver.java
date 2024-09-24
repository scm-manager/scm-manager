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

public abstract class BasicRepositoryLocationResolver<T> extends RepositoryLocationResolver {

  private final Class<T> type;

  protected BasicRepositoryLocationResolver(Class<T> type) {
    this.type = type;
  }

  @Override
  public boolean supportsLocationType(Class<?> type) {
    return type.isAssignableFrom(this.type);
  }
}
