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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;

class FileBasedStoreEntryImporterTest {

  @Test
  void shouldCreateFileFromInputStream(@TempDir Path temp) {
    FileBasedStoreEntryImporter importer = new FileBasedStoreEntryImporter(temp);
    String fileName = "testStore.xml";

    importer.importEntry(fileName, new ByteArrayInputStream("testdata".getBytes()));

    assertThat(Files.exists(temp.resolve(fileName))).isTrue();
    assertThat(temp.resolve(fileName)).hasContent("testdata");
  }
}
