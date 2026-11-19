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

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

class AbstractStoreConcurrencyTest {

  @ParameterizedTest
  @CsvSource({"READ, READ", "READ, WRITE", "READ, DELETE", "WRITE, WRITE", "WRITE, DELETE", "DELETE, WRITE"})
  void shouldKeepCacheConsistentWithBackingStore(Operation first, Operation second) throws Exception {
    ConcurrentStoreOperations operations = new ConcurrentStoreOperations();
    TestStore store = new TestStore(operations);
    if (first != Operation.READ) {
      store.get();
    }

    operations.run(() -> first.apply(store, "first"), () -> second.apply(store, "second"));

    assertThat(store.get()).isEqualTo(store.persisted);
    assertThat(store.readCount.get()).as("configuration read once").isEqualTo(1);
  }

  private enum Operation {
    READ, WRITE, DELETE;

    void apply(TestStore store, String value) {
      switch (this) {
        case READ -> store.get();
        case WRITE -> store.set(value);
        case DELETE -> store.delete();
      }
    }
  }

  private static class TestStore extends AbstractStore<String> {

    private final ConcurrentStoreOperations operations;
    private final AtomicInteger readCount = new AtomicInteger();
    private volatile String persisted = "original";

    private TestStore(ConcurrentStoreOperations operations) {
      super(() -> false);
      this.operations = operations;
    }

    @Override
    protected String readObject() {
      readCount.incrementAndGet();
      String value = persisted;
      operations.pauseFirstOperation();
      return value;
    }

    @Override
    protected String copy(String object) {
      return object;
    }

    @Override
    protected void writeObject(String object) {
      persisted = object;
      operations.pauseFirstOperation();
    }

    @Override
    protected void deleteObject() {
      persisted = null;
      operations.pauseFirstOperation();
    }
  }
}
