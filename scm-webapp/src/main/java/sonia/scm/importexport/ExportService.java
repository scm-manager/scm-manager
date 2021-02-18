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

package sonia.scm.importexport;

import sonia.scm.repository.Repository;
import sonia.scm.repository.api.ExportFailedException;
import sonia.scm.store.Blob;
import sonia.scm.store.BlobStore;
import sonia.scm.store.BlobStoreFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.UUID;

import static sonia.scm.ContextEntry.ContextBuilder.entity;

@Singleton
public class ExportService {

  static final String STORE_NAME = "repository-export";
  private final BlobStoreFactory blobStoreFactory;
  private final HashSet<String> pendingExports = new HashSet<>();

  @Inject
  public ExportService(BlobStoreFactory blobStoreFactory) {
    this.blobStoreFactory = blobStoreFactory;
  }

  public OutputStream store(Repository repository) {
    pendingExports.add(repository.getId());
    BlobStore store = createStore(repository);
    if (!store.getAll().isEmpty()) {
      store.clear();
    }

    //TODO Is random id really better than repository id?
    Blob blob = store.create(UUID.randomUUID().toString());
    try {
      return blob.getOutputStream();
    } catch (IOException e) {
      throw new ExportFailedException(
        entity(repository).build(),
        "Could not store repository export to blob file",
        e
      );
    }
  }

  public InputStream get(Repository repository) {
    try {
      return createStore(repository)
        .getAll()
        .get(0)
        .getInputStream();
    } catch (IOException e) {
      throw new ExportFailedException(
        entity(repository).build(),
        "Could not stored repository export from blob",
        e
      );
    }
  }

  public void clear(Repository repository) {
    createStore(repository).clear();
  }

  public void setExportFinished(Repository repository) {
    pendingExports.remove(repository.getId());
  }

  public boolean isExporting(Repository repository) {
    return pendingExports.stream().anyMatch(e -> e.equals(repository.getId()));
  }

  private BlobStore createStore(Repository repository) {
    return blobStoreFactory.withName(STORE_NAME).forRepository(repository).build();
  }
}
