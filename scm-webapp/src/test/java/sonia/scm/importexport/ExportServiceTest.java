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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryTestData;
import sonia.scm.store.Blob;
import sonia.scm.store.BlobStore;
import sonia.scm.store.BlobStoreFactory;
import sonia.scm.store.InMemoryBlobStore;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static sonia.scm.importexport.ExportService.STORE_NAME;

@ExtendWith(MockitoExtension.class)
public class ExportServiceTest {

  private static final Repository REPOSITORY = RepositoryTestData.createHeartOfGold();

  @Mock(answer = Answers.RETURNS_DEEP_STUBS)
  private BlobStoreFactory blobStoreFactory;

  private BlobStore blobStore;

  @InjectMocks
  private ExportService exportService;

  @BeforeEach
  void mockBlobStore() {
    blobStore = new InMemoryBlobStore();
    when(blobStoreFactory.withName(STORE_NAME).forRepository(REPOSITORY).build()).thenReturn(blobStore);
  }

  @Test
  void shouldClearStoreIfEntryAlreadyExists() throws IOException {
    //Old content blob
    blobStore.create();

    String newContent = "Scm-Manager-Export";
    OutputStream os = exportService.store(REPOSITORY);
    os.write(newContent.getBytes());
    os.flush();
    os.close();

    // Only new blob should exist
    List<Blob> blobs = blobStore.getAll();
    assertThat(blobs).hasSize(1);

    //Verify content
    byte[] bytes = new byte[1024];
    exportService.get(REPOSITORY).read(bytes);
    assertThat(new String(bytes)).isEqualTo(newContent);
  }

  @Test
  void shouldShowCorrectExportStatus() throws IOException {
    exportService.store(REPOSITORY);

    assertThat(exportService.isExporting(REPOSITORY)).isTrue();

    exportService.setExportFinished(REPOSITORY);

    assertThat(exportService.isExporting(REPOSITORY)).isFalse();
  }

  @Test
  void shouldClearExports() {
    blobStore.create();

    exportService.clear(REPOSITORY);

    assertThat(blobStore.getAll()).isEmpty();
  }
}
