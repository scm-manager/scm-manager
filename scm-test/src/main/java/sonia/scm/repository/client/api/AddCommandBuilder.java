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
import sonia.scm.repository.client.spi.AddCommand;
import sonia.scm.util.Util;

import java.io.IOException;

/**
 * @since 1.18
 */
public final class AddCommandBuilder {

  private static final Logger logger = LoggerFactory.getLogger(AddCommandBuilder.class);

  private final AddCommand command;

  AddCommandBuilder(AddCommand command) {
    this.command = command;
  }

  public AddCommandBuilder add(String... paths) throws IOException {
    for (String p : paths) {
      add(p);
    }
    return this;
  }

  private void add(String path) throws IOException {
    if (Util.isNotEmpty(path)) {
      logger.debug("add path {}", path);

      command.add(path);
    }
  }
}
