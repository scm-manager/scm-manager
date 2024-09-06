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

package sonia.scm.store;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class FileBasedStoreEntryImporterFactoryTest {

  @Test
  void shouldCreateStoreEntryImporterForDataStore(@TempDir Path temp) {
    FileBasedStoreEntryImporterFactory factory = new FileBasedStoreEntryImporterFactory(temp);

    FileBasedStoreEntryImporter dataImporter = (FileBasedStoreEntryImporter) factory.importStore(new StoreEntryMetaData(StoreType.DATA, "hitchhiker"));
    assertThat(dataImporter.getDirectory()).isEqualTo(temp.resolve("store").resolve("data").resolve("hitchhiker"));
  }

  @Test
  void shouldCreateStoreEntryImporterForConfigStore(@TempDir Path temp) {
    FileBasedStoreEntryImporterFactory factory = new FileBasedStoreEntryImporterFactory(temp);

    FileBasedStoreEntryImporter configImporter = (FileBasedStoreEntryImporter) factory.importStore(new StoreEntryMetaData(StoreType.CONFIG, ""));
    assertThat(configImporter.getDirectory()).isEqualTo(temp.resolve("store").resolve("config"));
  }
}
