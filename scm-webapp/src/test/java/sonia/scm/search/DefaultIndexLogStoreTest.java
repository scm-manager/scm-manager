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

package sonia.scm.search;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sonia.scm.store.InMemoryByteDataStoreFactory;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class DefaultIndexLogStoreTest {

  private IndexLogStore indexLogStore;

  @BeforeEach
  void setUpIndexLogStore() {
    InMemoryByteDataStoreFactory dataStoreFactory = new InMemoryByteDataStoreFactory();
    indexLogStore = new DefaultIndexLogStore(dataStoreFactory);
  }

  @Test
  void shouldReturnEmptyOptional() {
    Optional<IndexLog> indexLog = indexLogStore.forIndex("index").get(String.class);
    assertThat(indexLog).isEmpty();
  }

  @Test
  void shouldStoreLog() {
    indexLogStore.forIndex("index").log(String.class, 42);
    Optional<IndexLog> index = indexLogStore.forIndex("index").get(String.class);
    assertThat(index).hasValueSatisfying(log -> {
      assertThat(log.getVersion()).isEqualTo(42);
      assertThat(log.getDate()).isNotNull();
    });
  }

}
