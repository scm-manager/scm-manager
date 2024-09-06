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

import sonia.scm.repository.api.FileLock;
import sonia.scm.repository.api.LockCommandResult;
import sonia.scm.repository.api.UnlockCommandResult;

import java.util.Collection;
import java.util.Optional;

/**
 * Interface for lock implementations.
 *
 * @since 2.26.0
 */
public interface FileLockCommand {

  /**
   * Locks a given file.
   *
   * @param request The details of the lock creation.
   * @return The result of the lock creation.
   * @throws sonia.scm.repository.api.FileLockedException if the file is already locked.
   */
  LockCommandResult lock(LockCommandRequest request);

  /**
   * Unlocks a given file.
   *
   * @param request The details of the lock removal.
   * @return The result of the lock removal.
   * @throws sonia.scm.repository.api.FileLockedException if the file is locked and the lock cannot be removed.
   */
  UnlockCommandResult unlock(UnlockCommandRequest request);

  /**
   * Returns the lock of a file, if it exists.
   *
   * @param request Details of the status request.
   * @return {@link Optional} with the lock, if the file is locked,
   * or {@link Optional#empty()}, if the file is not locked
   */
  Optional<FileLock> status(LockStatusCommandRequest request);

  /**
   * Returns all locks for the repository.
   */
  Collection<FileLock> getAll();
}
