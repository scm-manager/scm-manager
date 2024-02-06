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

package sonia.scm.repository.api;

import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryPermissions;
import sonia.scm.repository.spi.FileLockCommand;
import sonia.scm.repository.spi.LockCommandRequest;
import sonia.scm.repository.spi.LockStatusCommandRequest;
import sonia.scm.repository.spi.UnlockCommandRequest;

import java.util.Collection;
import java.util.Optional;

/**
 * Can lock and unlock files and check lock states. Locked files can only be modified by the user holding the lock.
 *
 * @since 2.26.0
 */
public final class FileLockCommandBuilder {

  private final FileLockCommand fileLockCommand;
  private final Repository repository;

  public FileLockCommandBuilder(FileLockCommand fileLockCommand, Repository repository) {
    this.fileLockCommand = fileLockCommand;
    this.repository = repository;
  }

  /**
   * Creates builder to lock the given file.
   *
   * @param file The file to lock.
   * @return Builder for lock creation.
   */
  public InnerLockCommandBuilder lock(String file) {
    RepositoryPermissions.push(repository).check();
    return new InnerLockCommandBuilder(file);
  }

  /**
   * Creates builder to unlock the given file.
   *
   * @param file The file to unlock.
   * @return Builder to unlock a file.
   */
  public InnerUnlockCommandBuilder unlock(String file) {
    RepositoryPermissions.push(repository).check();
    return new InnerUnlockCommandBuilder(file);
  }

  /**
   * Retrieves the lock for a file, if it is locked.
   *
   * @param file The file to get the lock for.
   * @return {@link Optional} with the lock, if the file is locked,
   * or {@link Optional#empty()}, if the file is not locked
   */
  public Optional<FileLock> status(String file) {
    LockStatusCommandRequest lockStatusCommandRequest = new LockStatusCommandRequest();
    lockStatusCommandRequest.setFile(file);
    return fileLockCommand.status(lockStatusCommandRequest);
  }

  /**
   * Retrieves all locks for the repository.
   *
   * @return Collection of all locks for the repository.
   */
  public Collection<FileLock> getAll() {
    return fileLockCommand.getAll();
  }

  public class InnerLockCommandBuilder {
    private final String file;

    public InnerLockCommandBuilder(String file) {
      this.file = file;
    }

    /**
     * Creates the lock.
     *
     * @return The result of the lock creation.
     * @throws FileLockedException if the file is already locked.
     */
    public LockCommandResult execute() {
      LockCommandRequest lockCommandRequest = new LockCommandRequest();
      lockCommandRequest.setFile(file);
      return fileLockCommand.lock(lockCommandRequest);
    }
  }

  public class InnerUnlockCommandBuilder {
    private final String file;
    private boolean force;

    public InnerUnlockCommandBuilder(String file) {
      this.file = file;
    }

    /**
     * Set the command to force unlock. Shortcut for <code>force(true)</code>.
     *
     * @return This builder instance.
     * @see #force(boolean)
     */
    public InnerUnlockCommandBuilder force() {
      return force(true);
    }

    /**
     * Set whether to force unlock or not. A lock from a different user can only
     * be removed with force set to <code>true</code>.
     *
     * @param force Whether to force unlock or not.
     * @return This builder instance.
     */
    public InnerUnlockCommandBuilder force(boolean force) {
      this.force = force;
      return this;
    }

    /**
     * Remove the lock.
     *
     * @return The result of the lock removal.
     * @throws FileLockedException if the file is locked by another user and {@link #force(boolean)} has not been
     *                             set to <code>true</code>.
     */
    public UnlockCommandResult execute() {
      UnlockCommandRequest unlockCommandRequest = new UnlockCommandRequest();
      unlockCommandRequest.setFile(file);
      unlockCommandRequest.setForce(force);
      return fileLockCommand.unlock(unlockCommandRequest);
    }
  }
}
