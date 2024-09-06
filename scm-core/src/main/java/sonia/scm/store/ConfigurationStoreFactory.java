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
 * The ConfigurationStoreFactory can be used to create new or get existing {@link ConfigurationStore} objects.
 * <br>
 * <b>Note:</b> the default implementation uses the same location as the {@link ConfigurationEntryStoreFactory}, so be
 * sure that the store names are unique for all {@link ConfigurationEntryStore}s and {@link ConfigurationStore}s.
 * <br>
 * You can either create a global {@link ConfigurationStore} or a {@link ConfigurationStore} for a specific repository.
 * To create a global {@link ConfigurationStore} call:
 * <code><pre>
 *     configurationStoreFactory
 *       .withType(PersistedType.class)
 *       .withName("name")
 *       .build();
 * </pre></code>
 * To create a {@link ConfigurationStore} for a specific repository call:
 * <code><pre>
 *     configurationStoreFactory
 *       .withType(PersistedType.class)
 *       .withName("name")
 *       .forRepository(repository)
 *       .build();
 * </pre></code>
 *
 *
 * @apiviz.landmark
 * @apiviz.uses sonia.scm.store.ConfigurationStore
 */
public interface ConfigurationStoreFactory  {

  /**
   * Creates a new or gets an existing {@link ConfigurationStore}. Instead of calling this method you should use the
   * floating API from {@link #withType(Class)}.
   *
   * @param storeParameters The parameters for the {@link ConfigurationStore}.
   * @return A new or an existing {@link ConfigurationStore} for the given parameters.
   */
  <T> ConfigurationStore<T> getStore(final TypedStoreParameters<T> storeParameters);

  /**
   * Use this to create a new or get an existing {@link ConfigurationStore} with a floating API.
   * @param type The type for the {@link ConfigurationStore}.
   * @return Floating API to set the name and either specify a repository or directly build a global
   * {@link ConfigurationStore}.
   */
  default <T> TypedStoreParametersBuilder<T,ConfigurationStore<T>> withType(Class<T> type) {
    return new TypedStoreParametersBuilder<>(type, this::getStore);
  }
}
