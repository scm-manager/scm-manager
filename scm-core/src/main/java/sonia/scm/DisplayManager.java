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

package sonia.scm;

import java.util.Collection;
import java.util.Optional;

public interface DisplayManager<T extends ReducedModelObject> {

  int DEFAULT_LIMIT = 5;

  /**
   * Returns a {@link Collection} of filtered objects
   *
   * @param filter the searched string
   * @return filtered object from the store
   */
  Collection<T> autocomplete(String filter);

  Optional<T> get(String id);
}
