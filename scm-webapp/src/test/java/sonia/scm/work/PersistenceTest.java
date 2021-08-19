/*
 * MIT License
 *
 * Copyright (c) 2020-present Cloudogu GmbH and Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package sonia.scm.work;

import lombok.EqualsAndHashCode;
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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PersistenceTest {

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
        1L, Collections.singleton(new Resource("a")), new MyTask()
      );
      persistence.store(work);

      UnitOfWork loaded = persistence.loadAll().iterator().next();
      assertThat(loaded).isEqualTo(work);
    }

    @Test
    void shouldStoreInjectingChunkOfWork() {
      UnitOfWork work = new InjectingUnitOfWork(
        1L, Collections.singleton(new Resource("a")), MyTask.class
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
    void shouldNotFailForSingleItems() {
      store(1);
      persistence.store(new SimpleUnitOfWork(
        2L, Collections.emptySet(), new NotSerializable())
      );

      assertThat(persistence.loadAll()).hasSize(1);
    }

    @Test
    void shouldRemoveStored() {
      store(1);
      SimpleUnitOfWork chunkOfWork = new SimpleUnitOfWork(
        2L, Collections.emptySet(), new MyTask()
      );
      persistence.store(chunkOfWork);
      persistence.remove(chunkOfWork);

      assertThat(persistence.loadAll()).hasSize(1);
    }

    private void store(long... orderIds) {
      for (long order : orderIds) {
        persistence.store(new SimpleUnitOfWork(
          order, Collections.emptySet(), new MyTask()
        ));
      }
    }

  }

  @Test
  void shouldNotFailForNonChunkOfWorkItems() throws IOException {
    InMemoryBlobStore blobStore = new InMemoryBlobStore();

    Persistence persistence = new Persistence(PersistenceTest.class.getClassLoader(), blobStore);
    persistence.store(new SimpleUnitOfWork(
      1L, Collections.emptySet(), new MyTask())
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
