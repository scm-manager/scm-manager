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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AbstractStoreTest {

  private final AtomicBoolean readOnly = new AtomicBoolean();
  private final TestStore store = new TestStore(readOnly);

  @Test
  void shouldCacheMissingConfigurationWithoutCopyingNull() {
    assertThat(store.get()).isNull();
    assertThat(store.getOptional()).isEmpty();
    assertThat(store.readFromStoreCount).isEqualTo(1);
    assertThat(store.copyCount).isZero();
  }

  @Test
  void shouldStoreNullWithoutCopying() {
    store.set(null);

    assertThat(store.get()).isNull();
    assertThat(store.copyCount).isZero();
    assertThat(store.readFromStoreCount).isZero();
  }

  @Test
  void shouldReturnIndependentCopiesWithoutReadingAgain() {
    store.persisted = new ArrayList<>(List.of("original"));

    store.get().add("changed");

    assertThat(store.get()).containsExactly("original");
    assertThat(store.readFromStoreCount).isEqualTo(1);
  }

  @Test
  void shouldPersistAndCacheIndependentSnapshot() {
    List<String> configuration = new ArrayList<>(List.of("original"));

    store.set(configuration);
    configuration.add("changed");

    assertThat(store.get()).containsExactly("original");
    assertThat(store.persisted).containsExactly("original");
    assertThat(store.readFromStoreCount).isZero();
  }

  @Test
  void shouldNotWriteOrReplaceCacheWhenCopyFails() {
    store.set(List.of("original"));
    store.failCopy = true;

    assertThatThrownBy(() -> store.set(List.of("changed"))).isInstanceOf(StoreException.class);

    store.failCopy = false;
    assertThat(store.get()).containsExactly("original");
    assertThat(store.persisted).containsExactly("original");
  }

  @Test
  void shouldNotReplaceCacheWhenWriteFails() {
    store.set(List.of("original"));
    store.failWrite = true;

    assertThatThrownBy(() -> store.set(List.of("changed"))).isInstanceOf(StoreException.class);

    assertThat(store.get()).containsExactly("original");
    assertThat(store.persisted).containsExactly("original");
  }

  @Test
  void shouldRejectReadOnlyWritesBeforeCopying() {
    readOnly.set(true);

    assertThatThrownBy(() -> store.set(List.of("changed"))).isInstanceOf(StoreReadOnlyException.class);

    assertThat(store.persisted).isNull();
    assertThat(store.copyCount).isZero();
  }

  @Test
  void shouldDeleteCachedConfiguration() {
    store.set(List.of("original"));

    store.delete();

    assertThat(store.get()).isNull();
    assertThat(store.persisted).isNull();
  }

  @Test
  void shouldRejectReadOnlyDeletion() {
    store.set(List.of("original"));
    readOnly.set(true);

    assertThatThrownBy(store::delete).isInstanceOf(StoreReadOnlyException.class);

    assertThat(store.get()).containsExactly("original");
    assertThat(store.persisted).containsExactly("original");
  }

  private static class TestStore extends AbstractStore<List<String>> {

    private List<String> persisted;
    private int readFromStoreCount;
    private int copyCount;
    private boolean failCopy;
    private boolean failWrite;

    private TestStore(AtomicBoolean readOnly) {
      super(readOnly::get);
    }

    @Override
    protected List<String> readObject() {
      readFromStoreCount++;
      return persisted == null ? null : new ArrayList<>(persisted);
    }

    @Override
    protected List<String> copy(List<String> object) {
      copyCount++;
      if (failCopy) {
        throw new StoreException("copy failed");
      }
      return new ArrayList<>(object);
    }

    @Override
    protected void writeObject(List<String> object) {
      if (failWrite) {
        throw new StoreException("write failed");
      }
      persisted = object;
    }

    @Override
    protected void deleteObject() {
      persisted = null;
    }
  }
}
