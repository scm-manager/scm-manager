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

package sonia.scm.store;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sonia.scm.security.KeyGenerator;


import java.io.File;
import java.io.IOException;

import java.util.List;

/**
 * File based implementation of {@link BlobStore}.
 *
 */
public class FileBlobStore extends FileBasedStore<Blob> implements BlobStore {

 
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
