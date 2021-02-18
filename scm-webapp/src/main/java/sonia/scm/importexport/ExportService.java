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

import com.google.common.annotations.VisibleForTesting;
import lombok.Value;
import org.apache.shiro.SecurityUtils;
import sonia.scm.NotFoundException;
import sonia.scm.repository.Repository;
import sonia.scm.repository.api.ExportFailedException;
import sonia.scm.store.Blob;
import sonia.scm.store.BlobStore;
import sonia.scm.store.BlobStoreFactory;
import sonia.scm.user.User;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.time.Clock;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;

import static sonia.scm.ContextEntry.ContextBuilder.entity;

@Singleton
public class ExportService {

  static final String STORE_NAME = "repository-export";
  private final BlobStoreFactory blobStoreFactory;
  private final Clock clock;
  private final HashSet<String> pendingExports = new HashSet<>();

  @Inject
  public ExportService(BlobStoreFactory blobStoreFactory) {
    this.blobStoreFactory = blobStoreFactory;
    this.clock = Clock.systemUTC();
  }

  @VisibleForTesting
  ExportService(BlobStoreFactory blobStoreFactory, Clock clock) {
    this.blobStoreFactory = blobStoreFactory;
    this.clock = clock;
  }

  public OutputStream store(Repository repository, String fileExtension) {
    pendingExports.add(repository.getId());
    BlobStore store = createStore(repository);
    if (!store.getAll().isEmpty()) {
      store.clear();
    }

    Blob blob = store.create(fileExtension);
    try {
      OutputStream os = blob.getOutputStream();
      storeExportInformation(repository, os);
      return os;
    } catch (IOException e) {
      throw new ExportFailedException(
        entity(repository).build(),
        "Could not store repository export to blob file",
        e
      );
    }
  }

  private void storeExportInformation(Repository repository, OutputStream os) {
    try {
      byte[] exportInformation = createExportInformation().getBytes();
      try (DataOutputStream dos = new DataOutputStream(os)) {
        dos.writeInt(exportInformation.length);
        dos.write(exportInformation);
      }
    } catch (IOException e) {
      throw new ExportFailedException(
        entity(repository).build(),
        "Could not write export information to export-blob",
        e
      );
    }
  }

  public String getFileExtension(Repository repository) {
    return getBlob(repository).getId();
  }

  public InputStream getData(Repository repository) {
    try {
      InputStream is = getBlob(repository).getInputStream();
      readExportInformation(is);
      return is;
    } catch (IOException e) {
      throw new ExportFailedException(
        entity(repository).build(),
        "Could not stored repository export from blob",
        e
      );
    }
  }

  public void checkExportExists(Repository repository) {
    getBlob(repository);
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


  private String createExportInformation() {
    User exporter = SecurityUtils.getSubject().getPrincipals().oneByType(User.class);
    Instant instant = clock.instant();

    return String.format("%s|%s|%s", exporter.getName(), exporter.getMail(), instant.toString());
  }

  public RepositoryExportInformation getExportInformation(Repository repository) {
    try {
      String information = readExportInformation(getBlob(repository).getInputStream());
      String[] splitInformation = information.split("\\|");
      return new RepositoryExportInformation(splitInformation[0], splitInformation[1], Instant.parse(splitInformation[2]));
    } catch (IOException e) {
      throw new ExportFailedException(entity(repository).build(), "Could not read export information from export-blob", e);
    }
  }

  private String readExportInformation(InputStream is) throws IOException {
    try (DataInputStream dis = new DataInputStream(is)) {
      int length = dis.readInt();
      byte[] exportInformation = new byte[length];
      dis.read(exportInformation);
      return new String(exportInformation);
    }
  }

  private Blob getBlob(Repository repository) {
    List<Blob> blobs = createStore(repository).getAll();
    if (blobs.isEmpty()) {
      throw new NotFoundException("repository-export", repository.getId());
    }
    return blobs.get(0);
  }

  private BlobStore createStore(Repository repository) {
    return blobStoreFactory.withName(STORE_NAME).forRepository(repository).build();
  }

  @Value
  static class RepositoryExportInformation {
    String username;
    String mail;
    Instant when;
  }
}
