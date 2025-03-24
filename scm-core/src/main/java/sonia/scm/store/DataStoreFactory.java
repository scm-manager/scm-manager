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

/**
 * The DataStoreFactory can be used to create new or get existing {@link DataStore}s.
 * You can either create a global {@link DataStore} or a {@link DataStore} for a specific repository.
 * <br/>
 * <br/>
 * Call this to create a global {@link DataStore}:
 * <br/>
 * <code>dataStoreFactory.withType(PersistedType.class).withName("name").build();</code>
 * <br/>
 * <br/>
 * To create a {@link DataStore} for a specific repository:
 * <br/>
 * <code>dataStoreFactory.withType(PersistedType.class).withName("name").forRepository(repository).build();</code>
 *
 * @since 1.23
 */
public interface DataStoreFactory {

  /**
   * Creates a new or gets an existing {@link DataStore}. Instead of calling this method you should use the
   * floating API from {@link #withType(Class)}.
   *
   * @param storeParameters The parameters for the {@link DataStore}.
   * @return A new or an existing {@link DataStore} for the given parameters.
   */
  <T> DataStore<T> getStore(final TypedStoreParameters<T> storeParameters);

  /**
   * Use this to create a new or get an existing {@link DataStore} with a floating API.
   * @param type The type for the {@link DataStore}.
   * @return Floating API to set the name and either specify a repository or directly build a global
   * {@link DataStore}.
   */
  default <T> TypedStoreParametersBuilder<T, DataStore<T>> withType(Class<T> type) {
    return new TypedStoreParametersBuilder<>(type, this::getStore);
  }
}
