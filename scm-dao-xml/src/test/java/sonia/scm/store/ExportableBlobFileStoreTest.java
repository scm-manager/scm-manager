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

import java.nio.file.Path;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;

class ExportableBlobFileStoreTest {

  @Test
  void shouldIgnoreStoreIfExcludedStore() {
    Path dir = Paths.get("test/path/repository-export");
    ExportableBlobFileStore exportableBlobFileStore = new ExportableBlobFileStore(dir);

    Path file = Paths.get(dir.toString(), "some.blob");
    boolean result = exportableBlobFileStore.shouldIncludeFile(file);

    assertThat(result).isFalse();
  }

  @Test
  void shouldIgnoreStoreIfNotBlob() {
    Path dir = Paths.get("test/path/any-store");
    ExportableBlobFileStore exportableBlobFileStore = new ExportableBlobFileStore(dir);

    Path file = Paths.get(dir.toString(), "some.unblob");
    boolean result = exportableBlobFileStore.shouldIncludeFile(file);

    assertThat(result).isFalse();
  }

  @Test
  void shouldIncludeStore() {
    Path dir = Paths.get("test/path/any-blob-store");
    ExportableBlobFileStore exportableBlobFileStore = new ExportableBlobFileStore(dir);

    Path file = Paths.get(dir.toString(), "some.blob");
    boolean result = exportableBlobFileStore.shouldIncludeFile(file);

    assertThat(result).isTrue();
  }

}
