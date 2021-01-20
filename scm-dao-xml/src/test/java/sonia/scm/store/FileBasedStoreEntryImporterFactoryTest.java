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
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class FileBasedStoreEntryImporterFactoryTest {

  @Test
  void shouldCreateStoreEntryImporterForDataStore(@TempDir Path temp) {
    FileBasedStoreEntryImporterFactory factory = new FileBasedStoreEntryImporterFactory(temp.toFile());

    FileBasedStoreEntryImporter dataImporter = (FileBasedStoreEntryImporter) factory.importStore("data", "hitchhiker");
    assertThat(dataImporter.getType()).isEqualTo("data");
    assertThat(dataImporter.getName()).isEqualTo("hitchhiker");
    assertThat(dataImporter.getDirectory().getPath()).isEqualTo(temp.resolve("data").resolve("hitchhiker").toString());
  }

  @Test
  void shouldCreateStoreEntryImporterForConfigStore(@TempDir Path temp) {
    FileBasedStoreEntryImporterFactory factory = new FileBasedStoreEntryImporterFactory(temp.toFile());

    FileBasedStoreEntryImporter configImporter = (FileBasedStoreEntryImporter) factory.importStore("config", "");
    assertThat(configImporter.getType()).isEqualTo("config");
    assertThat(configImporter.getName()).isEqualTo("");
    assertThat(configImporter.getDirectory().getPath()).isEqualTo(temp.resolve("config").toString());
  }
}
