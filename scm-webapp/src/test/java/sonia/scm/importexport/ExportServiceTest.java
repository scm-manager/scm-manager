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

import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.ThreadContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.NotFoundException;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryManager;
import sonia.scm.repository.RepositoryTestData;
import sonia.scm.store.Blob;
import sonia.scm.store.BlobStore;
import sonia.scm.store.BlobStoreFactory;
import sonia.scm.store.DataStore;
import sonia.scm.store.DataStoreFactory;
import sonia.scm.store.InMemoryBlobStore;
import sonia.scm.store.InMemoryDataStore;
import sonia.scm.user.User;

import java.io.IOException;
import java.io.OutputStream;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static sonia.scm.importexport.ExportService.STORE_NAME;

@ExtendWith(MockitoExtension.class)
class ExportServiceTest {

  private static final Repository REPOSITORY = RepositoryTestData.createHeartOfGold();

  @Mock(answer = Answers.RETURNS_DEEP_STUBS)
  private BlobStoreFactory blobStoreFactory;

  @Mock(answer = Answers.RETURNS_DEEP_STUBS)
  private DataStoreFactory dataStoreFactory;

  @Mock
  private ExportFileExtensionResolver resolver;

  @Mock
  private ExportNotificationHandler notificationHandler;

  @Mock
  private RepositoryManager repositoryManager;

  private BlobStore blobStore;
  private DataStore<RepositoryExportInformation> dataStore;

  @Mock
  private Subject subject;

  @InjectMocks
  private ExportService exportService;

  @BeforeEach
  void initMocks() {
    ThreadContext.bind(subject);
    PrincipalCollection principalCollection = mock(PrincipalCollection.class);
    lenient().when(subject.getPrincipals()).thenReturn(principalCollection);
    lenient().when(principalCollection.oneByType(User.class)).thenReturn(
      new User("trillian", "Trillian", "trillian@hitchhiker.org")
    );

    blobStore = new InMemoryBlobStore();
    when(blobStoreFactory.withName(STORE_NAME).forRepository(REPOSITORY.getId()).withReadOnlyIgnore().build())
      .thenReturn(blobStore);

    dataStore = new InMemoryDataStore<>();
    when(dataStoreFactory.withType(RepositoryExportInformation.class).withName(STORE_NAME).build())
      .thenReturn(dataStore);
  }

  @AfterEach
  void tearDown() {
    ThreadContext.unbindSubject();
  }

  @Test
  void shouldClearStoreIfEntryAlreadyExists() throws IOException {
    //Old content blob
    blobStore.create(REPOSITORY.getId());

    String newContent = "Scm-Manager-Export";
    OutputStream os = exportService.store(REPOSITORY, true, true, true);
    os.write(newContent.getBytes());
    os.flush();
    os.close();

    // Only new blob should exist
    List<Blob> blobs = blobStore.getAll();
    assertThat(blobs).hasSize(1);

    //Verify content
    byte[] bytes = new byte[18];
    exportService.getData(REPOSITORY).read(bytes);
    assertThat(new String(bytes)).isEqualTo(newContent);
  }

  @Test
  void shouldShowCorrectExportStatus() {
    doNothing().when(subject).checkPermission("repository:export:" + REPOSITORY.getId());
    exportService.store(REPOSITORY, false, false, false);
    assertThat(exportService.isExporting(REPOSITORY)).isTrue();

    exportService.setExportFinished(REPOSITORY);
    assertThat(exportService.isExporting(REPOSITORY)).isFalse();
    verify(notificationHandler).handleSuccessfulExport(REPOSITORY);
  }

  @Test
  void shouldOnlyClearRepositoryExports() {
    doNothing().when(subject).checkPermission("repository:export:" + REPOSITORY.getId());
    Repository hvpt = RepositoryTestData.createHappyVerticalPeopleTransporter();
    dataStore.put(hvpt.getId(), new RepositoryExportInformation());

    blobStore.create(REPOSITORY.getId());
    dataStore.put(REPOSITORY.getId(), new RepositoryExportInformation());

    exportService.clear(REPOSITORY.getId());

    assertThat(dataStore.get(REPOSITORY.getId())).isNull();
    assertThat(dataStore.get(hvpt.getId())).isNotNull();
    assertThat(blobStore.getAll()).isEmpty();
  }

  @Test
  void shouldGetExportInformation() {
    doNothing().when(subject).checkPermission("repository:export:" + REPOSITORY.getId());
    exportService.store(REPOSITORY, true, true, false);
    RepositoryExportInformation exportInformation = exportService.getExportInformation(REPOSITORY);

    assertThat(exportInformation.getExporterName()).isEqualTo("trillian");
    assertThat(exportInformation.getCreated()).isNotNull();
  }

  @Test
  void shouldThrowNotFoundException() {
    assertThrows(NotFoundException.class, () -> exportService.getExportInformation(REPOSITORY));
    assertThrows(NotFoundException.class, () -> exportService.getFileExtension(REPOSITORY));
    assertThrows(NotFoundException.class, () -> exportService.getData(REPOSITORY));
  }

  @Test
  void shouldResolveFileExtension() {
    doNothing().when(subject).checkPermission("repository:export:" + REPOSITORY.getId());
    String extension = "tar.gz.enc";
    RepositoryExportInformation info = new RepositoryExportInformation();
    dataStore.put(REPOSITORY.getId(), info);

    when(resolver.resolve(REPOSITORY, false, false, false)).thenReturn(extension);

    String fileExtension = exportService.getFileExtension(REPOSITORY);

    assertThat(fileExtension).isEqualTo(extension);
  }

  @Test
  void shouldOnlyCleanupUnfinishedExports() {
    blobStore.create(REPOSITORY.getId());
    RepositoryExportInformation info = new RepositoryExportInformation();
    info.setStatus(ExportStatus.EXPORTING);
    dataStore.put(
      REPOSITORY.getId(),
      info
    );

    Repository finishedExport = RepositoryTestData.createHappyVerticalPeopleTransporter();
    BlobStore finishedExportBlobStore = new InMemoryBlobStore();
    Blob finishedExportBlob = finishedExportBlobStore.create(finishedExport.getId());
    RepositoryExportInformation finishedExportInfo = new RepositoryExportInformation();
    finishedExportInfo.setStatus(ExportStatus.FINISHED);
    dataStore.put(
      finishedExport.getId(),
      finishedExportInfo
    );
    when(blobStoreFactory.withName(STORE_NAME).forRepository(finishedExport.getId()).build())
      .thenReturn(finishedExportBlobStore);
    when(repositoryManager.get(REPOSITORY.getId())).thenReturn(REPOSITORY);

    exportService.cleanupUnfinishedExports();

    assertThat(blobStore.getAll()).isEmpty();
    assertThat(dataStore.get(REPOSITORY.getId()).getStatus()).isEqualTo(ExportStatus.INTERRUPTED);
    assertThat(finishedExportBlobStore.get(finishedExport.getId())).isEqualTo(finishedExportBlob);
    assertThat(dataStore.get(finishedExport.getId()).getStatus()).isEqualTo(ExportStatus.FINISHED);
  }

  @Test
  void shouldDeleteUnfinishedExportsOfDeletedRepository() {
    RepositoryExportInformation info = new RepositoryExportInformation();
    info.setStatus(ExportStatus.EXPORTING);
    dataStore.put(
      REPOSITORY.getId(),
      info
    );
    when(repositoryManager.get(REPOSITORY.getId())).thenReturn(null);

    exportService.cleanupUnfinishedExports();

    assertThat(dataStore.getOptional(REPOSITORY.getId())).isEmpty();
  }

  @Test
  void shouldOnlyCleanupOutdatedExports() {
    blobStore.create(REPOSITORY.getId());
    Instant now = Instant.now();
    RepositoryExportInformation newExportInfo = new RepositoryExportInformation();
    newExportInfo.setCreated(now);
    dataStore.put(REPOSITORY.getId(), newExportInfo);

    Repository oldExportRepo = RepositoryTestData.createHappyVerticalPeopleTransporter();
    BlobStore oldExportBlobStore = new InMemoryBlobStore();
    oldExportBlobStore.create(oldExportRepo.getId());
    RepositoryExportInformation oldExportInfo = new RepositoryExportInformation();
    Instant old = Instant.now().minus(11, ChronoUnit.DAYS);
    oldExportInfo.setCreated(old);
    dataStore.put(oldExportRepo.getId(), oldExportInfo);
    when(blobStoreFactory.withName(STORE_NAME).forRepository(oldExportRepo.getId()).withReadOnlyIgnore().build())
      .thenReturn(oldExportBlobStore);

    exportService.cleanupOutdatedExports();

    assertThat(blobStore.getAll()).hasSize(1);
    assertThat(oldExportBlobStore.getAll()).isEmpty();
    assertThat(dataStore.get(REPOSITORY.getId()).getCreated()).isEqualTo(now);
    assertThat(dataStore.get(oldExportRepo.getId())).isNull();
  }
}
