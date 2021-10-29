/*
 * MIT License
 *
 * Copyright (c) 2020-present Cloudogu GmbH and Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
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
public interface LockCommand {

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
