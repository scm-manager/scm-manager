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

import sonia.scm.SCMContextProvider;
import sonia.scm.migration.UpdateException;
import sonia.scm.repository.RepositoryLocationResolver;
import sonia.scm.store.StoreParameters;
import sonia.scm.store.StoreType;
import sonia.scm.update.StoreUpdateStepUtilFactory;
import sonia.scm.util.IOUtil;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileStoreUpdateStepUtil implements StoreUpdateStepUtilFactory.StoreUpdateStepUtil {

  private final RepositoryLocationResolver locationResolver;
  private final SCMContextProvider contextProvider;

  private final StoreParameters parameters;
  private final StoreType type;

  public FileStoreUpdateStepUtil(RepositoryLocationResolver locationResolver, SCMContextProvider contextProvider, StoreParameters parameters, StoreType type) {
    this.locationResolver = locationResolver;
    this.contextProvider = contextProvider;
    this.parameters = parameters;
    this.type = type;
  }

  @Override
  public void renameStore(String newName) {
    Path oldStorePath = resolveBasePath().resolve(parameters.getName());
    if (Files.exists(oldStorePath)) {
      Path newStorePath = resolveBasePath().resolve(newName);
      try {
        Files.move(oldStorePath, newStorePath);
      } catch (IOException e) {
        throw new UpdateException(String.format("Could not move store path %s to %s", oldStorePath, newStorePath), e);
      }
    }
  }

  @Override
  public void deleteStore() {
    Path oldStorePath = resolveBasePath().resolve(parameters.getName());
    IOUtil.deleteSilently(oldStorePath.toFile());
  }

  private Path resolveBasePath() {
    Path basePath;
    if (parameters.getRepositoryId() != null) {
      basePath = locationResolver.forClass(Path.class).getLocation(parameters.getRepositoryId());
    } else {
      basePath = contextProvider.getBaseDirectory().toPath();
    }
    Path storeBasePath;
    if (parameters.getRepositoryId() == null) {
      storeBasePath = basePath.resolve(Store.forStoreType(type).getGlobalStoreDirectory());
    } else {
      storeBasePath = basePath.resolve(Store.forStoreType(type).getRepositoryStoreDirectory());
    }
    return storeBasePath;
  }
}
