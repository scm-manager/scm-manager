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
 * <b>Note:</b> the default implementation uses the same location as the {@link ConfigurationStoreFactory}, so be sure
 * that the store names are unique for all {@link ConfigurationEntryStore}s and {@link ConfigurationEntryStore}s.
 * <br/>
 * <br/>
 * The ConfigurationEntryStoreFactory can be used to create new or get existing {@link ConfigurationEntryStore}s.
 * <br/>
 * <br/>
 * You can either create a global {@link ConfigurationEntryStore} or a {@link ConfigurationEntryStore} for a specific
 * repository. Call this to create a global {@link ConfigurationEntryStore}:
 * <br/>
 * <code>configurationEntryStoreFactory.withType(PersistedType.class).withName("name").build();</code>
 * <br/>
 * <br/>
 * Call this to create a {@link ConfigurationEntryStore} for a specific repository:
 * <br/>
 * <code>
 *     configurationEntryStoreFactory
 *       .withType(PersistedType.class)
 *       .withName("name")
 *       .forRepository(repository)
 *       .build();
 * </code>
 *
 * @since 1.31
 */
public interface ConfigurationEntryStoreFactory {

  /**
   * Creates a new or gets an existing {@link ConfigurationEntryStore}. Instead of calling this method you should use
   * the floating API from {@link #withType(Class)}.
   *
   * @param storeParameters The parameters for the {@link ConfigurationEntryStore}.
   * @return A new or an existing {@link ConfigurationEntryStore} for the given parameters.
   */
  <T> ConfigurationEntryStore<T> getStore(final TypedStoreParameters<T> storeParameters);

  /**
   * Use this to create a new or get an existing {@link ConfigurationEntryStore} with a floating API.
   * @param type The type for the {@link ConfigurationEntryStore}.
   * @return Floating API to set the name and either specify a repository or directly build a global
   * {@link ConfigurationEntryStore}.
   */
  default <T> TypedStoreParametersBuilder<T, ConfigurationEntryStore<T>> withType(Class<T> type) {
    return new TypedStoreParametersBuilder<>(type, this::getStore);
  }
}
