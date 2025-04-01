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

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sonia.scm.security.KeyGenerator;
import sonia.scm.store.Blob;
import sonia.scm.store.BlobStore;
import sonia.scm.store.EntryAlreadyExistsStoreException;
import sonia.scm.store.StoreException;


import java.io.File;
import java.io.IOException;

import java.util.List;

/**
 * File based implementation of {@link BlobStore}.
 *
 */
class FileBlobStore extends FileBasedStore<Blob> implements BlobStore {

 
  private static final Logger LOG
    = LoggerFactory.getLogger(FileBlobStore.class);

  private static final String SUFFIX = ".blob";

  private final KeyGenerator keyGenerator;

  FileBlobStore(KeyGenerator keyGenerator, File directory, boolean readOnly) {
    super(directory, SUFFIX, readOnly);
    this.keyGenerator = keyGenerator;
  }

  @Override
  public Blob create() {
    return create(keyGenerator.createKey());
  }

  @Override
  public Blob create(String id) {
    Preconditions.checkArgument(!Strings.isNullOrEmpty(id),
      "id argument is required");
    LOG.debug("create new blob with id {}", id);

    assertNotReadOnly();

    File file = getFile(id);

    try {
      if (file.exists()) {
        throw new EntryAlreadyExistsStoreException(
          "blob with id ".concat(id).concat(" allready exists"));
      }
      else if (!file.createNewFile()) {
        throw new StoreException("could not create blob for id ".concat(id));
      }
    }
    catch (IOException ex) {
      throw new StoreException("could not create blob for id ".concat(id), ex);
    }

    return new FileBlob(id, file);
  }

  @Override
  public void remove(Blob blob) {
    assertNotReadOnly();
    Preconditions.checkNotNull(blob, "blob argument is required");
    remove(blob.getId());
  }

  @Override
  public List<Blob> getAll() {
    LOG.trace("get all items from data store");

    Builder<Blob> builder = ImmutableList.builder();

    for (File file : directory.listFiles()) {
      builder.add(read(file));
    }

    return builder.build();
  }

  @Override
  protected FileBlob read(File file) {
    FileBlob blob = null;

    if (file.exists()) {
      String id = getId(file);

      blob = new FileBlob(id, file);
    }

    return blob;
  }

}
