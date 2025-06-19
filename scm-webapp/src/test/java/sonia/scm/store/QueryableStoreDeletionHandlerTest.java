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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.group.Group;
import sonia.scm.user.User;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith({QueryableStoreExtension.class, MockitoExtension.class})
@QueryableStoreExtension.QueryableTypes({QueryableTypeWithGroupParent.class, QueryableTypeWithMultipleParents.class})
class QueryableStoreDeletionHandlerTest {

  @Mock
  private StoreMetaDataProvider metaDataProvider;

  @Nested
  class WithSingleParent {

    private QueryableStoreDeletionHandler queryableStoreDeletionHandler;

    @BeforeEach
    void setUpHandler(QueryableStoreFactory storeFactory) {
      when(metaDataProvider.getTypesWithParent(Group.class))
        .thenReturn(Set.of(QueryableTypeWithGroupParent.class));

      queryableStoreDeletionHandler = new QueryableStoreDeletionHandler(Set.of(), metaDataProvider, storeFactory);
    }

    @Test
    void shouldDeleteStoresWithParent(QueryableTypeWithGroupParentStoreFactory groupParentStoreFactory) {
      try (QueryableMutableStore<QueryableTypeWithGroupParent> groupParentStore = groupParentStoreFactory.getMutable("earth")) {
        groupParentStore.put(new QueryableTypeWithGroupParent());

        queryableStoreDeletionHandler.notifyDeleted(Group.class, "earth");

        assertThat(groupParentStore.getAll())
          .isEmpty();
      }
    }

    @Test
    void shouldKeepStoresWithOtherParent(QueryableTypeWithGroupParentStoreFactory groupParentStoreFactory) {
      try (QueryableMutableStore<QueryableTypeWithGroupParent> groupParentStore = groupParentStoreFactory.getMutable("hog")) {
        groupParentStore.put(new QueryableTypeWithGroupParent());

        queryableStoreDeletionHandler.notifyDeleted(Group.class, "earth");

        assertThat(groupParentStore.getAll())
          .hasSize(1);
      }
    }
  }

  @Nested
  class WithMultipleParents {

    private QueryableStoreDeletionHandler queryableStoreDeletionHandler;

    @BeforeEach
    void setUpHandler(QueryableStoreFactory storeFactory) {
      queryableStoreDeletionHandler = new QueryableStoreDeletionHandler(Set.of(), metaDataProvider, storeFactory);
    }

    @Test
    void shouldDeleteStoresWhenSingleParentDeleted(QueryableTypeWithMultipleParentsStoreFactory storeFactory) {
      when(metaDataProvider.getTypesWithParent(Group.class))
        .thenReturn(Set.of(QueryableTypeWithMultipleParents.class));

      try (QueryableMutableStore<QueryableTypeWithMultipleParents> store = storeFactory.getMutable("earth", "dent", "house")) {
        store.put(new QueryableTypeWithMultipleParents());

        queryableStoreDeletionHandler.notifyDeleted(Group.class, "earth");

        assertThat(store.getAll())
          .isEmpty();
      }
    }

    @Test
    void shouldDeleteStoresWhenParentChantDeleted(QueryableTypeWithMultipleParentsStoreFactory storeFactory) {
      when(metaDataProvider.getTypesWithParent(Group.class, User.class))
        .thenReturn(Set.of(QueryableTypeWithMultipleParents.class));

      try (QueryableMutableStore<QueryableTypeWithMultipleParents> store = storeFactory.getMutable("earth", "dent", "house")) {
        store.put(new QueryableTypeWithMultipleParents());

        queryableStoreDeletionHandler.notifyDeleted(
          new StoreDeletionNotifier.ClassWithId(Group.class, "earth"),
          new StoreDeletionNotifier.ClassWithId(User.class, "dent")
        );

        assertThat(store.getAll())
          .isEmpty();
      }
    }
  }
}
