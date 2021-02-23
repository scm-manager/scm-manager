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
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static sonia.scm.importexport.ExportService.STORE_NAME;

@ExtendWith(MockitoExtension.class)
public class ExportServiceTest {

  private static final Repository REPOSITORY = RepositoryTestData.createHeartOfGold();

  @Mock(answer = Answers.RETURNS_DEEP_STUBS)
  private BlobStoreFactory blobStoreFactory;

  @Mock(answer = Answers.RETURNS_DEEP_STUBS)
  private DataStoreFactory dataStoreFactory;

  @Mock
  private ExportFileExtensionResolver resolver;

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
    when(blobStoreFactory.withName(STORE_NAME).forRepository(REPOSITORY.getId()).build())
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
    exportService.store(REPOSITORY, false, false, false);
    assertThat(exportService.isExporting(REPOSITORY)).isTrue();

    exportService.setExportFinished(REPOSITORY);
    assertThat(exportService.isExporting(REPOSITORY)).isFalse();
  }

  @Test
  void shouldOnlyClearRepositoryExports() {
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
    exportService.store(REPOSITORY, true, true, false);
    RepositoryExportInformation exportInformation = exportService.getExportInformation(REPOSITORY);

    assertThat(exportInformation.getExporter().getName()).isEqualTo("trillian");
    assertThat(exportInformation.getExporter().getMail()).isEqualTo("trillian@hitchhiker.org");
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
    String extension = "tar.gz.enc";
    RepositoryExportInformation info = new RepositoryExportInformation();
    dataStore.put(REPOSITORY.getId(), info);

    when(resolver.resolve(REPOSITORY, info)).thenReturn(extension);

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

    exportService.cleanupUnfinishedExports();

    assertThat(blobStore.getAll()).isEmpty();
    assertThat(dataStore.get(REPOSITORY.getId()).getStatus()).isEqualTo(ExportStatus.INTERRUPTED);
    assertThat(finishedExportBlobStore.get(finishedExport.getId())).isEqualTo(finishedExportBlob);
    assertThat(dataStore.get(finishedExport.getId()).getStatus()).isEqualTo(ExportStatus.FINISHED);
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
    when(blobStoreFactory.withName(STORE_NAME).forRepository(oldExportRepo.getId()).build())
      .thenReturn(oldExportBlobStore);

    exportService.cleanupOutdatedExports();

    assertThat(blobStore.getAll()).hasSize(1);
    assertThat(oldExportBlobStore.getAll()).isEmpty();
    assertThat(dataStore.get(REPOSITORY.getId()).getCreated()).isEqualTo(now);
    assertThat(dataStore.get(oldExportRepo.getId())).isNull();
  }
}
