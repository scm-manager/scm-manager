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

package sonia.scm.store.file;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.CopyOnWrite;
import sonia.scm.security.KeyGenerator;
import sonia.scm.store.DataStore;
import sonia.scm.store.IdHandlerForStoresForGeneratedId;
import sonia.scm.store.StoreException;
import sonia.scm.xml.XmlStreams;

import java.io.File;
import java.util.Map;
import java.util.Objects;

import static sonia.scm.CopyOnWrite.compute;

/**
 * Jaxb implementation of {@link DataStore}.
 *
 * @param <T> type of stored data.
 */
class JAXBDataStore<T> extends FileBasedStore<T> implements DataStore<T> {

 
  private static final Logger LOG = LoggerFactory.getLogger(JAXBDataStore.class);

  private final TypedStoreContext<T> context;
  private final DataFileCache.DataFileCacheInstance cache;

  private final IdHandlerForStoresForGeneratedId<T> idHandlerForStores;

  JAXBDataStore(KeyGenerator keyGenerator, TypedStoreContext<T> context, File directory, boolean readOnly, DataFileCache.DataFileCacheInstance cache) {
    super(directory, StoreConstants.FILE_EXTENSION, readOnly);
    this.cache = cache;
    this.directory = directory;
    this.context = context;
    this.idHandlerForStores = new IdHandlerForStoresForGeneratedId<>(context.getType(), keyGenerator, this::doPut);
  }

  @Override
  public String put(T item) {
    return idHandlerForStores.put(item);
  }

  @Override
  public void put(String id, T item) {
    idHandlerForStores.put(id, item);
  }

  private void doPut(String id, T item) {
    LOG.debug("put item {} to store", id);

    assertNotReadOnly();

    File file = getFile(id);

    try {
      Marshaller marshaller = context.createMarshaller();

      marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
      CopyOnWrite.withTemporaryFile(
        temp -> marshaller.marshal(item, XmlStreams.createWriter(temp.toFile())),
        file.toPath(),
        () -> cache.put(file, item)
      );
    } catch (JAXBException ex) {
      throw new StoreException("could not write object with id ".concat(id),
        ex);
    }
  }

  @Override
  public Map<String, T> getAll() {
    LOG.trace("get all items from data store");

    Builder<String, T> builder = ImmutableMap.builder();

    for (File file : Objects.requireNonNull(directory.listFiles())) {
      if (file.isFile() && file.getName().endsWith(StoreConstants.FILE_EXTENSION)) {
        builder.put(getId(file), read(file));
      }
    }

    return builder.build();
  }

  @Override
  protected void remove(File file) {
    cache.remove(file);
    super.remove(file);
  }

  @Override
  protected T read(File file) {
    return cache.get(file, () ->
      compute(() -> {
        if (file.exists()) {
          LOG.trace("try to read {}", file);
          return context.unmarshal(file);
        }
        return null;
      }).withLockedFileForRead(file)
    );
  }
}
