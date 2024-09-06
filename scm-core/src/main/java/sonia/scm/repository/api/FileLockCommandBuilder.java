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
