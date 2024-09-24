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

package sonia.scm.repository;

import jakarta.inject.Inject;
import sonia.scm.store.DataStore;
import sonia.scm.store.DataStoreFactory;

import java.util.Collection;
import java.util.Optional;

public class NamespaceDao {

  private final DataStore<Namespace> store;

  @Inject
  NamespaceDao(DataStoreFactory storeFactory) {
    this.store = storeFactory.withType(Namespace.class).withName("namespaces").build();
  }

  public Optional<Namespace> get(String namespace) {
    return store.getOptional(namespace);
  }

  public void add(Namespace namespace) {
    store.put(namespace.getNamespace(), namespace);
  }

  public void delete(String namespace) {
    store.remove(namespace);
  }

  public Collection<Namespace> allWithPermissions() {
    return store.getAll().values();
  }
}
