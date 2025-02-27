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

package sonia.scm.repository.spi;

import sonia.scm.repository.api.RevertCommandResult;

/**
 * Removes the changes from a particular changeset as a revert. This, in turn, will result a new changeset.
 *
 * @since 3.8
 */
public interface RevertCommand {

  /**
   * Executes a revert.
   * @param request parameter set for this command.
   * @see RevertCommand
   * @return result set of the executed command (see {@link RevertCommandResult}).
   */
  RevertCommandResult revert(RevertCommandRequest request);
}
