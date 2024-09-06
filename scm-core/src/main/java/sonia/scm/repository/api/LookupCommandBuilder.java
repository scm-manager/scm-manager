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

import sonia.scm.repository.spi.LookupCommand;
import sonia.scm.repository.spi.LookupCommandRequest;

import java.util.Optional;

/**
 * The lookup command executes a lookup for additional repository information.
 *
 * @since 2.10.0
 */
public class LookupCommandBuilder {

  private final LookupCommand lookupCommand;

  public LookupCommandBuilder(LookupCommand lookupCommand) {
    this.lookupCommand = lookupCommand;
  }

  public <T> Optional<T> lookup(Class<T> type, String... args) {
    LookupCommandRequest<T> request = new LookupCommandRequest<>();
    request.setType(type);
    request.setArgs(args);
    return lookupCommand.lookup(request);
  }
}
