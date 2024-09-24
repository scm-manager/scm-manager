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

/**
 * A field of a {@link SearchableType}.
 *
 * @since 2.23.0
 */
@Beta
public interface SearchableField {

  /**
   * Returns the name of the searchable field.
   */
  String getName();

  /**
   * Returns the type of the searchable field.
   */
  Class<?> getType();
}
