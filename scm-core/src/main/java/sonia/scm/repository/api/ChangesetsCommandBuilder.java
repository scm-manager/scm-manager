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

package sonia.scm.repository.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.repository.Changeset;
import sonia.scm.repository.Repository;
import sonia.scm.repository.spi.ChangesetsCommand;
import sonia.scm.repository.spi.ChangesetsCommandRequest;

import java.util.Optional;

public class ChangesetsCommandBuilder {

  private static final Logger LOG = LoggerFactory.getLogger(ChangesetsCommandBuilder.class);

  private final Repository repository;
  private final ChangesetsCommand changesetsCommand;

  private final ChangesetsCommandRequest request = new ChangesetsCommandRequest();

  public ChangesetsCommandBuilder( Repository repository, ChangesetsCommand changesetsCommand) {
    this.repository = repository;
    this.changesetsCommand = changesetsCommand;
  }

  public Iterable<Changeset> getChangesets() {
      LOG.debug("Retrieve all changesets from {{}}", repository);
      return changesetsCommand.getChangesets(request);
  }

  public Optional<Changeset> getLatestChangeset() {
    LOG.debug("Retrieve latest changeset from {{}}", repository);
    return changesetsCommand.getLatestChangeset();
  }
}
