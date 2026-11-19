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

package sonia.scm.store;

import java.util.Optional;

import static java.util.Optional.ofNullable;

/**
 * ConfigurationStore for configuration objects. <strong>Note:</strong> the default
 * implementation use JAXB to marshall the configuration objects.
 *
 * @param <T> type of the configuration objects
 */
public interface ConfigurationStore<T> {

  /**
   * Returns an independent copy of the stored configuration. Changes to the
   * returned object are only stored when passed to {@link #set(Object)}.
   *
   * @return configuration object from store
   */
  T get();

  /**
   * Returns an independent copy of the stored configuration, if present.
   *
   * @return configuration object from store
   */
  default Optional<T> getOptional() {
    return ofNullable(get());
  }

  /**
   * Stores an independent snapshot of the given configuration object. Later
   * changes to the given object do not change the stored configuration.
   *
   * @param object configuration object to store
   */
  void set(T object);

  /**
   * Deletes the configuration.
   * @since 2.24.0
   */
  default void delete() {
    throw new StoreException("Delete operation is not implemented by the store");
  }
}
