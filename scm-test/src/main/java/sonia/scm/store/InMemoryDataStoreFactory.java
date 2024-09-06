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
 * In memory configuration store factory for testing purposes.
 *
 * @deprecated use {@link InMemoryByteDataStoreFactory} instead.
 */
@Deprecated
@SuppressWarnings("java:S3740")
public class InMemoryDataStoreFactory implements DataStoreFactory {

  private InMemoryDataStore store;

  public InMemoryDataStoreFactory() {
  }

  public InMemoryDataStoreFactory(InMemoryDataStore store) {
    this.store = store;
  }

  @Override
  public <T> DataStore<T> getStore(TypedStoreParameters<T> storeParameters) {
    if (store != null) {
      return store;
    }
    return new InMemoryDataStore<>();
  }
}
