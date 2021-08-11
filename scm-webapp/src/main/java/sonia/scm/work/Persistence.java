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

import com.google.common.annotations.VisibleForTesting;
import com.google.inject.Inject;
import org.apache.commons.io.input.ClassLoaderObjectInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.plugin.PluginLoader;
import sonia.scm.store.Blob;
import sonia.scm.store.BlobStore;
import sonia.scm.store.BlobStoreFactory;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

class Persistence {

  private static final Logger LOG = LoggerFactory.getLogger(Persistence.class);
  private static final String STORE_NAME = "central-work-queue";

  private final ClassLoader classLoader;
  private final BlobStore store;

  @Inject
  public Persistence(PluginLoader pluginLoader, BlobStoreFactory storeFactory) {
    this(pluginLoader.getUberClassLoader(), storeFactory.withName(STORE_NAME).build());
  }

  @VisibleForTesting
  Persistence(ClassLoader classLoader, BlobStore store) {
    this.classLoader = classLoader;
    this.store = store;
  }

  Collection<ChunkOfWork> loadAll() {
    List<ChunkOfWork> chunks = new ArrayList<>();
    for (Blob blob : store.getAll()) {
      load(blob).ifPresent(chunkOfWork -> {
        chunkOfWork.setStorageId(null);
        chunks.add(chunkOfWork);
      });
      store.remove(blob);
    }
    Collections.sort(chunks);
    return chunks;
  }

  private Optional<ChunkOfWork> load(Blob blob) {
    try (ObjectInputStream stream = new ClassLoaderObjectInputStream(classLoader, blob.getInputStream())) {
      Object o = stream.readObject();
      if (o instanceof ChunkOfWork) {
        return Optional.of((ChunkOfWork) o);
      } else {
        LOG.error("loaded object is not a instance of {}: {}", ChunkOfWork.class, o);
      }
    } catch (IOException | ClassNotFoundException ex) {
      LOG.error("failed to load task from store", ex);
    }
    return Optional.empty();
  }

  void store(ChunkOfWork chunkOfWork) {
    Blob blob = store.create();
    try (ObjectOutputStream outputStream = new ObjectOutputStream(blob.getOutputStream())) {
      outputStream.writeObject(chunkOfWork);
      blob.commit();

      chunkOfWork.setStorageId(blob.getId());
    } catch (IOException ex) {
      LOG.error("failed to persist task {}", chunkOfWork, ex);
    }
  }

  void remove(ChunkOfWork chunkOfWork) {
    chunkOfWork.getStorageId().ifPresent(store::remove);
  }

}
