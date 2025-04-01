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

import jakarta.inject.Inject;
import sonia.scm.repository.RepositoryLocationResolver;
import sonia.scm.update.RepositoryUpdateIterator;

import java.nio.file.Path;
import java.util.function.Consumer;

public class FileRepositoryUpdateIterator implements RepositoryUpdateIterator {

  private final RepositoryLocationResolver locationResolver;

  @Inject
  public FileRepositoryUpdateIterator(RepositoryLocationResolver locationResolver) {
    this.locationResolver = locationResolver;
  }

  @Override
  public void forEachRepository(Consumer<String> repositoryIdConsumer) {
    locationResolver
      .forClass(Path.class)
      .forAllLocations((repositoryId, path) -> repositoryIdConsumer.accept(repositoryId));
  }
}
