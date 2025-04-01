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
import sonia.scm.store.Exporter;
import sonia.scm.store.StoreEntryMetaData;
import sonia.scm.store.StoreType;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;
import java.util.function.Function;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static sonia.scm.store.file.ExportCopier.putFileContentIntoStream;

class ExportableConfigFileStore implements ExportableStore {

  private final Path file;

  static final Function<StoreType, Optional<Function<Path, ExportableStore>>> CONFIG_FACTORY =
    storeType -> storeType == StoreType.CONFIG ? of(ExportableConfigFileStore::new) : empty();

  ExportableConfigFileStore(Path file) {
    this.file = file;
  }

  @Override
  public StoreEntryMetaData getMetaData() {
    return new StoreEntryMetaData(StoreType.CONFIG, file.getFileName().toString());
  }

  @Override
  public void export(Exporter exporter) throws IOException {
    putFileContentIntoStream(exporter, file);
  }
}
