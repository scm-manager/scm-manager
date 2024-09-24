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

  Collection<UnitOfWork> loadAll() {
    List<UnitOfWork> chunks = new ArrayList<>();
    for (Blob blob : store.getAll()) {
      load(blob).ifPresent(chunkOfWork -> {
        chunkOfWork.assignStorageId(null);
        chunks.add(chunkOfWork);
      });
      store.remove(blob);
    }
    Collections.sort(chunks);
    return chunks;
  }

  private Optional<UnitOfWork> load(Blob blob) {
    try (ObjectInputStream stream = new ClassLoaderObjectInputStream(classLoader, blob.getInputStream())) {
      Object o = stream.readObject();
      if (o instanceof UnitOfWork) {
        return Optional.of((UnitOfWork) o);
      } else {
        LOG.error("loaded object is not a instance of {}: {}", UnitOfWork.class, o);
      }
    } catch (IOException | ClassNotFoundException ex) {
      LOG.error("failed to load task from store", ex);
    }
    return Optional.empty();
  }

  void store(UnitOfWork unitOfWork) {
    Blob blob = store.create();
    try (ObjectOutputStream outputStream = new ObjectOutputStream(blob.getOutputStream())) {
      outputStream.writeObject(unitOfWork);
      blob.commit();

      unitOfWork.assignStorageId(blob.getId());
    } catch (IOException ex) {
      throw new NonPersistableTaskException("Failed to persist task", ex);
    }
  }

  void remove(UnitOfWork unitOfWork) {
    unitOfWork.getStorageId().ifPresent(store::remove);
  }

}
