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

package sonia.scm.update.repository;

import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.SCMContextProvider;
import sonia.scm.util.IOUtil;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;

public class DeleteMigrationStrategy extends BaseMigrationStrategy {

  private static final Logger LOG = LoggerFactory.getLogger(DeleteMigrationStrategy.class);

  @Inject
  DeleteMigrationStrategy(SCMContextProvider contextProvider) {
    super(contextProvider);
  }

  @Override
  public Optional<Path> migrate(String id, String name, String type) {
    Path sourceDataPath = getSourceDataPath(name, type);
    try {
      IOUtil.delete(sourceDataPath.toFile(), true);
    } catch (IOException e) {
      LOG.warn("could not delete old repository path for repository {} with type {} and id {}", name, type, id);
    }
    return Optional.empty();
  }
}
