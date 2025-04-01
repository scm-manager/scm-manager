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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(QueryableStoreExtension.class)
@QueryableStoreExtension.QueryableTypes(Spaceship.class)
class QueryableStoreExtensionTest {

  @Test
  void shouldProvideQueryableStoreFactory(QueryableStoreFactory storeFactory) {
    QueryableMutableStore<Spaceship> store = storeFactory.getMutable(Spaceship.class);
    store.put(new Spaceship("Heart Of Gold"));
    assertEquals(1, store.getAll().size());
  }

  @Test
  void shouldProvideTypeRelatedStoreFactory(SpaceshipStoreFactory storeFactory) {
    QueryableMutableStore<Spaceship> store = storeFactory.getMutable();
    store.put(new Spaceship("Heart Of Gold"));
    assertEquals(1, store.getAll().size());
  }
}

@QueryableType
class Spaceship {
  String name;

  Spaceship() {
  }

  Spaceship(String name) {
    this.name = name;
  }
}
