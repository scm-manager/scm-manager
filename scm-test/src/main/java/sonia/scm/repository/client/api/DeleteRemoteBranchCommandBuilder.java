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

package sonia.scm.repository.client.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.repository.client.spi.DeleteRemoteBranchCommand;

import java.io.IOException;

/**
 * @since 2.4.0
 */
public final class DeleteRemoteBranchCommandBuilder {

  private static final Logger LOG = LoggerFactory.getLogger(DeleteRemoteBranchCommandBuilder.class);

  private DeleteRemoteBranchCommand command;

  public DeleteRemoteBranchCommandBuilder(DeleteRemoteBranchCommand command) {
    this.command = command;
  }

  public DeleteRemoteBranchCommandBuilder delete(String name) throws IOException {
    LOG.debug("delete branch {}", name);

    command.delete(name);

    return this;
  }
}
