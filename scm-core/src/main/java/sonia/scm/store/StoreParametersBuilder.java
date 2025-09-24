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

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import sonia.scm.repository.Repository;

import java.util.function.Function;

/**
 * Builder for {@link StoreParameters}.
 *
 * @param <S> type of store
 */
public final class StoreParametersBuilder<S> {

  private final StoreParametersImpl parameters;
  private final Function<StoreParameters, S> factory;

  StoreParametersBuilder(String name, Function<StoreParameters, S> factory) {
    this.parameters = new StoreParametersImpl(name);
    this.factory = factory;
  }

  @Getter
  @Setter(AccessLevel.PRIVATE)
  @RequiredArgsConstructor
  private static class StoreParametersImpl implements StoreParameters {
    private final String name;
    private String repositoryId;
    private String namespace;
    private boolean ignoreReadOnly;
  }


  /**
   * Use this to create or get a store for a specific repository. This step is optional. If you want to
   * have a global store, omit this.
   *
   * @param repository The optional repository for the store.
   * @return Floating API to finish the call.
   */
  public StoreParametersBuilder<S> forRepository(Repository repository) {
    parameters.repositoryId = repository.getId();
    return this;
  }

  /**
   * Use this to create or get a store for a specific repository. This step is optional. If you want to
   * have a global store, omit this.
   *
   * @param repositoryId The id of the optional repository for the store.
   * @return Floating API to finish the call.
   */
  public StoreParametersBuilder<S> forRepository(String repositoryId) {
    parameters.repositoryId = repositoryId;
    return this;
  }

  /**
   * Use this to create or get a store, that can write data into a repository store,
   * even if the repository itself is read-only.
   * One use-case example is the 'ExportService' within scm-webapp.
   * Only use this feature, if you are sure that your service needs to write data into a repository,
   * even if it is read-only.
   *
   * @return Floating API to finish the call.
   */
  public StoreParametersBuilder<S> withReadOnlyIgnore() {
    parameters.ignoreReadOnly = true;
    return this;
  }

  /**
   * Creates or gets the store with the given name and (if specified) the given repository. If no
   * repository is given, the store will be global.
   */
  public S build() {
    return factory.apply(parameters);
  }
}
