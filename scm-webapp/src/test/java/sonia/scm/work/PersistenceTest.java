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

package sonia.scm.work;

import lombok.EqualsAndHashCode;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.SimplePrincipalCollection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.plugin.PluginLoader;
import sonia.scm.store.Blob;
import sonia.scm.store.InMemoryBlobStore;
import sonia.scm.store.InMemoryBlobStoreFactory;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PersistenceTest {

  private final PrincipalCollection principal = new SimplePrincipalCollection("trillian", "test");

  @Nested
  class Default {

    @Mock
    private PluginLoader pluginLoader;

    private Persistence persistence;

    @BeforeEach
    void setUp() {
      when(pluginLoader.getUberClassLoader()).thenReturn(PersistenceTest.class.getClassLoader());
      persistence = new Persistence(pluginLoader, new InMemoryBlobStoreFactory());
    }

    @Test
    void shouldStoreSimpleChunkOfWork() {
      UnitOfWork work = new SimpleUnitOfWork(
        1L, principal, Collections.singleton(new Resource("a")), new MyTask()
      );
      persistence.store(work);

      UnitOfWork loaded = persistence.loadAll().iterator().next();
      assertThat(loaded).isEqualTo(work);
    }

    @Test
    void shouldStoreInjectingChunkOfWork() {
      UnitOfWork work = new InjectingUnitOfWork(
        1L, principal, Collections.singleton(new Resource("a")), MyTask.class
      );
      persistence.store(work);

      UnitOfWork loaded = persistence.loadAll().iterator().next();
      assertThat(loaded).isEqualTo(work);
    }

    @Test
    void shouldLoadInOrder() {
      store(5, 3, 1, 4, 2);

      long[] orderIds = persistence.loadAll()
        .stream()
        .mapToLong(UnitOfWork::getOrder)
        .toArray();

      assertThat(orderIds).containsExactly(1, 2, 3, 4, 5);
    }

    @Test
    void shouldRemoveAfterLoad() {
      store(1, 2);

      assertThat(persistence.loadAll()).hasSize(2);
      assertThat(persistence.loadAll()).isEmpty();
    }

    @Test
    void shouldFailIfNotSerializable() {
      store(1);

      SimpleUnitOfWork unitOfWork = new SimpleUnitOfWork(
        2L, principal, Collections.emptySet(), new NotSerializable()
      );

      assertThrows(NonPersistableTaskException.class, () -> persistence.store(unitOfWork));
    }

    @Test
    void shouldRemoveStored() {
      store(1);
      SimpleUnitOfWork chunkOfWork = new SimpleUnitOfWork(
        2L, principal, Collections.emptySet(), new MyTask()
      );
      persistence.store(chunkOfWork);
      persistence.remove(chunkOfWork);

      assertThat(persistence.loadAll()).hasSize(1);
    }

    private void store(long... orderIds) {
      for (long order : orderIds) {
        persistence.store(new SimpleUnitOfWork(
          order, principal, Collections.emptySet(), new MyTask()
        ));
      }
    }

  }

  @Test
  void shouldNotFailForNonChunkOfWorkItems() throws IOException {
    InMemoryBlobStore blobStore = new InMemoryBlobStore();

    Persistence persistence = new Persistence(PersistenceTest.class.getClassLoader(), blobStore);
    persistence.store(new SimpleUnitOfWork(
      1L, principal, Collections.emptySet(), new MyTask())
    );

    Blob blob = blobStore.create();
    try (ObjectOutputStream stream = new ObjectOutputStream(blob.getOutputStream())) {
      stream.writeObject(new MyTask());
      blob.commit();
    }

    assertThat(persistence.loadAll()).hasSize(1);
  }

  @EqualsAndHashCode
  public static class MyTask implements Task {

    @Override
    public void run() {

    }
  }

  // non static inner classes are not serializable
  private class NotSerializable implements Task {
    @Override
    public void run() {

    }
  }

}
