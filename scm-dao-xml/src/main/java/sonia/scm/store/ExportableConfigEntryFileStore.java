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

import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;
import java.util.function.Function;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static sonia.scm.store.ExportCopier.putFileContentIntoStream;

class ExportableConfigEntryFileStore implements ExportableStore {

  private final Path file;

  static final Function<StoreType, Optional<Function<Path, ExportableStore>>> CONFIG_ENTRY_FACTORY =
    storeType -> storeType == StoreType.CONFIG_ENTRY ? of(ExportableConfigEntryFileStore::new) : empty();

  ExportableConfigEntryFileStore(Path file) {
    this.file = file;
  }

  @Override
  public StoreEntryMetaData getMetaData() {
    return new StoreEntryMetaData(StoreType.CONFIG_ENTRY, file.getFileName().toString());
  }

  @Override
  public void export(Exporter exporter) throws IOException {
    putFileContentIntoStream(exporter, file);
  }
}
