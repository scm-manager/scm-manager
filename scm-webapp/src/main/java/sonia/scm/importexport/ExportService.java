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

package sonia.scm.importexport;

import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.SecurityUtils;
import sonia.scm.NotFoundException;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryManager;
import sonia.scm.repository.RepositoryPermissions;
import sonia.scm.repository.api.ExportFailedException;
import sonia.scm.store.Blob;
import sonia.scm.store.BlobStore;
import sonia.scm.store.BlobStoreFactory;
import sonia.scm.store.DataStore;
import sonia.scm.store.DataStoreFactory;
import sonia.scm.user.User;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static sonia.scm.ContextEntry.ContextBuilder.entity;

@Slf4j
public class ExportService {

  static final String STORE_NAME = "repository-export";
  private final BlobStoreFactory blobStoreFactory;
  private final DataStoreFactory dataStoreFactory;
  private final ExportFileExtensionResolver fileExtensionResolver;
  private final ExportNotificationHandler notificationHandler;
  private final RepositoryManager repositoryManager;

  @Inject
  public ExportService(BlobStoreFactory blobStoreFactory,
                       DataStoreFactory dataStoreFactory,
                       ExportFileExtensionResolver fileExtensionResolver,
                       ExportNotificationHandler notificationHandler,
                       RepositoryManager repositoryManager) {
    this.blobStoreFactory = blobStoreFactory;
    this.dataStoreFactory = dataStoreFactory;
    this.fileExtensionResolver = fileExtensionResolver;
    this.notificationHandler = notificationHandler;
    this.repositoryManager = repositoryManager;
  }

  public OutputStream store(Repository repository, boolean withMetadata, boolean compressed, boolean encrypted) {
    log.debug("Start storing export for repository {}",  repository);
    RepositoryPermissions.export(repository).check();
    storeExportInformation(repository.getId(), withMetadata, compressed, encrypted);
    try {
      return storeNewBlob(repository.getId()).getOutputStream();
    } catch (IOException e) {
      notificationHandler.handleFailedExport(repository);
      throw new ExportFailedException(
        entity(repository).build(),
        "Could not store repository export to blob file",
        e
      );
    }
  }

  public RepositoryExportInformation getExportInformation(Repository repository) {
    RepositoryPermissions.export(repository).check();
    RepositoryExportInformation info = createDataStore().get(repository.getId());
    if (info == null) {
      throw new NotFoundException(RepositoryExportInformation.class, repository.getId());
    }
    return info;
  }

  public InputStream getData(Repository repository) throws IOException {
    RepositoryPermissions.export(repository).check();
    Blob blob = getBlob(repository.getId());
    if (blob == null) {
      throw new NotFoundException(Blob.class, repository.getId());
    }
    return blob.getInputStream();
  }

  public void checkExportIsAvailable(Repository repository) {
    RepositoryPermissions.export(repository).check();
    if (createDataStore().get(repository.getId()) == null) {
      throw new NotFoundException(RepositoryExportInformation.class, repository.getId());
    }
  }

  public String getFileExtension(Repository repository) {
    RepositoryPermissions.export(repository).check();
    RepositoryExportInformation exportInfo = getExportInformation(repository);
    return fileExtensionResolver.resolve(repository, exportInfo.isWithMetadata(), exportInfo.isCompressed(), exportInfo.isEncrypted());
  }

  public void clear(String repositoryId) {
    log.debug("Clearing export for repository {}",  repositoryId);
    RepositoryPermissions.export(repositoryId).check();
    createDataStore().remove(repositoryId);
    createBlobStore(repositoryId).clear();
  }

  public void setExportFinished(Repository repository) {
    log.debug("Setting export as finished for repository {}",  repository);
    RepositoryPermissions.export(repository).check();
    DataStore<RepositoryExportInformation> dataStore = createDataStore();
    RepositoryExportInformation info = dataStore.get(repository.getId());
    info.setStatus(ExportStatus.FINISHED);
    dataStore.put(repository.getId(), info);
    notificationHandler.handleSuccessfulExport(repository);
  }

  public boolean isExporting(Repository repository) {
    RepositoryPermissions.export(repository).check();
    RepositoryExportInformation info = createDataStore().get(repository.getId());
    boolean isExporting = info != null && info.getStatus() == ExportStatus.EXPORTING;

    if (isExporting) {
      log.debug("Repository {} is still exporting", repository);
    }

    return isExporting;
  }

  public void cleanupUnfinishedExports() {
    DataStore<RepositoryExportInformation> dataStore = createDataStore();
    List<Map.Entry<String, RepositoryExportInformation>> unfinishedExports = dataStore.getAll().entrySet().stream()
      .filter(e -> e.getValue().getStatus() == ExportStatus.EXPORTING)
      .toList();

    for (Map.Entry<String, RepositoryExportInformation> export : unfinishedExports) {
      log.debug("Cleaning up export for repository {}",  export.getKey());
      if (isRepositoryExisting(export.getKey())) {
        createBlobStore(export.getKey()).clear();
        RepositoryExportInformation info = dataStore.get(export.getKey());
        info.setStatus(ExportStatus.INTERRUPTED);
        dataStore.put(export.getKey(), info);
        log.debug("Export for repository {} has been cleaned up",  export.getKey());
      } else {
        dataStore.remove(export.getKey());
        log.debug("Repository {} has already been deleted. Deleting dangling export.",  export.getKey());
      }
    }
  }

  void cleanupOutdatedExports() {
    log.debug("Cleaning up outdated exports");
    DataStore<RepositoryExportInformation> dataStore = createDataStore();
    List<String> outdatedExportIds = collectOutdatedExportIds(dataStore);

    for (String id : outdatedExportIds) {
      createBlobStore(id).clear();
      log.debug("Cleaned up blob of outdated export for repository {}",  id);
    }

    outdatedExportIds.forEach(dataStore::remove);
    log.debug("Cleaned up outdated exports");
  }

  private List<String> collectOutdatedExportIds(DataStore<RepositoryExportInformation> dataStore) {
    List<String> outdatedExportIds = new ArrayList<>();
    Instant expireDate = Instant.now().minus(10, ChronoUnit.DAYS);

    dataStore
      .getAll()
      .entrySet()
      .stream()
      .filter(e -> e.getValue().getCreated().isBefore(expireDate))
      .forEach(e -> outdatedExportIds.add(e.getKey()));
    return outdatedExportIds;
  }

  private Blob storeNewBlob(String repositoryId) {
    BlobStore store = createBlobStore(repositoryId);
    if (!store.getAll().isEmpty()) {
      store.clear();
    }

    return store.create(repositoryId);
  }

  private void storeExportInformation(String repositoryId, boolean withMetadata, boolean compressed, boolean encrypted) {
    DataStore<RepositoryExportInformation> dataStore = createDataStore();
    if (dataStore.get(repositoryId) != null) {
      dataStore.remove(repositoryId);
    }

    String exporter = SecurityUtils.getSubject().getPrincipals().oneByType(User.class).getName();
    RepositoryExportInformation info = new RepositoryExportInformation(exporter, Instant.now(), withMetadata, compressed, encrypted, ExportStatus.EXPORTING);
    dataStore.put(repositoryId, info);
  }

  private Blob getBlob(String repositoryId) {
    return createBlobStore(repositoryId).get(repositoryId);
  }

  private DataStore<RepositoryExportInformation> createDataStore() {
    return dataStoreFactory.withType(RepositoryExportInformation.class).withName(STORE_NAME).build();
  }

  private BlobStore createBlobStore(String repositoryId) {
    return blobStoreFactory.withName(STORE_NAME).forRepository(repositoryId).withReadOnlyIgnore().build();
  }

  private boolean isRepositoryExisting(String repositoryId) {
    return this.repositoryManager.get(repositoryId) != null;
  }
}
