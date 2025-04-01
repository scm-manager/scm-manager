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

package sonia.scm.store.file;

import sonia.scm.store.ExportableStore;
import sonia.scm.store.StoreType;

import java.nio.file.Path;
import java.util.Optional;
import java.util.function.Function;

import static java.util.Optional.empty;
import static java.util.Optional.of;

class ExportableDataFileStore extends ExportableDirectoryBasedFileStore {

  static final Function<StoreType, Optional<Function<Path, ExportableStore>>> DATA_FACTORY =
    storeType -> storeType == StoreType.DATA ? of(ExportableDataFileStore::new) : empty();

  ExportableDataFileStore(Path directory) {
    super(directory);
  }

  @Override
  StoreType getStoreType() {
    return StoreType.DATA;
  }

  boolean shouldIncludeFile(Path file) {
    return file.getFileName().toString().endsWith(".xml");
  }
}
