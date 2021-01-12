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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryLocationResolver;
import sonia.scm.repository.RepositoryTestData;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FileStoreExporterTest {

  private static final Repository REPOSITORY = RepositoryTestData.create42Puzzle();

  @Mock(answer = Answers.RETURNS_DEEP_STUBS)
  private RepositoryLocationResolver resolver;

  @InjectMocks
  private FileStoreExporter fileStoreExporter;

  @Test
  void shouldReturnEmptyList(@TempDir Path temp) {
    when(resolver.supportsLocationType(Path.class)).thenReturn(true);
    when(resolver.forClass(Path.class).getLocation(REPOSITORY.getId())).thenReturn(temp);

    List<ExportableStore> exportableStores = fileStoreExporter.findExportableStores(REPOSITORY);
    assertThat(exportableStores).isEmpty();
  }

  @Test
  void shouldReturnListOfExportableStores(@TempDir Path temp) throws IOException {
    Path storePath = temp.resolve("store");
    createFile(storePath, "config", null, "first.xml");
    createFile(storePath, "data", "ci", "second.xml");
    createFile(storePath, "data", "jenkins", "third.xml");
    when(resolver.supportsLocationType(Path.class)).thenReturn(true);
    when(resolver.forClass(Path.class).getLocation(REPOSITORY.getId())).thenReturn(temp);

    List<ExportableStore> exportableStores = fileStoreExporter.findExportableStores(REPOSITORY);

    assertThat(exportableStores).hasSize(3);
    assertThat(exportableStores.get(0).getType()).isEqualTo("config");
    assertThat(exportableStores.get(1).getType()).isEqualTo("data");
    assertThat(exportableStores.get(2).getType()).isEqualTo("data");
  }

  private File createFile(Path storePath, String type, String name, String fileName) throws IOException {
    Path path = name != null ? storePath.resolve(type).resolve(name) : storePath.resolve(type);
    new File(path.toUri()).mkdirs();
    File file = new File(path.toFile(), fileName);
    if (!file.exists()) {
      file.createNewFile();
    }
    FileWriter source = new FileWriter(file);
    source.write("something");
    source.close();
    return file;
  }
}
