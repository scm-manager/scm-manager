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

package sonia.scm.search;

import com.google.common.annotations.Beta;

import java.util.Collection;

/**
 * A type which can be searched with the {@link SearchEngine}.
 *
 * @since 2.21.0
 */
@Beta
public interface SearchableType {

  /**
   * Return name of the type.
   */
  String getName();

  /**
   * Return type in form of class.
   */
  Class<?> getType();

  /**
   * Returns collection of searchable fields.
   *
   *  @since 2.23.0
   */
  Collection<? extends SearchableField> getFields();

  default boolean limitableToRepository() {
    return false;
  }

  default boolean limitableToNamespace() {
    return false;
  }
}
