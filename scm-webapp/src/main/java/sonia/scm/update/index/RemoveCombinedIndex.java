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

package sonia.scm.update.index;

import jakarta.annotation.Nonnull;
import jakarta.inject.Inject;
import sonia.scm.SCMContextProvider;
import sonia.scm.migration.UpdateStep;
import sonia.scm.plugin.Extension;
import sonia.scm.util.IOUtil;
import sonia.scm.version.Version;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static sonia.scm.store.StoreConstants.DATA_DIRECTORY_NAME;
import static sonia.scm.store.StoreConstants.VARIABLE_DATA_DIRECTORY_NAME;

@Extension
public class RemoveCombinedIndex implements UpdateStep {

  private final SCMContextProvider contextProvider;

  @Inject
  public RemoveCombinedIndex(SCMContextProvider contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public void doUpdate() throws IOException {
    Path index = contextProvider.resolve(Paths.get("index"));
    if (Files.exists(index)) {
      IOUtil.delete(index.toFile());
    }

    Path indexLog = contextProvider.resolve(indexLogPath());
    if (Files.exists(indexLog)) {
      IOUtil.delete(indexLog.toFile());
    }
  }

  @Nonnull
  private Path indexLogPath() {
    return Paths.get(VARIABLE_DATA_DIRECTORY_NAME).resolve(DATA_DIRECTORY_NAME).resolve("index-log");
  }

  @Override
  public Version getTargetVersion() {
    return Version.parse("2.0.0");
  }

  @Override
  public String getAffectedDataType() {
    return "sonia.scm.index";
  }
}
