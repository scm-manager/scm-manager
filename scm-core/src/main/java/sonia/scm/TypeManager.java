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

/**
 * Base interface for all type manager classes.
 *
 *
 * @param <T> type of the model object
 * @param <H> type of the handler
 */
public interface TypeManager<T extends ModelObject, H extends Handler<T>> extends Manager<T>
{

  /**
   * Returns the handler for given type or
   * null if no handler of that type is available.
   *
   * @param type name of the handler
   */
  H getHandler(String type);

  /**
   * Returns a {@link java.util.Collection} of all
   * available and configured types.
   */
  Collection<Type> getTypes();
}
