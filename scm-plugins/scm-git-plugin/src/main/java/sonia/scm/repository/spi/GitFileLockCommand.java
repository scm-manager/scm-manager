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

import com.google.inject.assistedinject.Assisted;
import jakarta.inject.Inject;
import sonia.scm.repository.api.FileLock;
import sonia.scm.repository.api.LockCommandResult;
import sonia.scm.repository.api.UnlockCommandResult;
import sonia.scm.repository.spi.GitFileLockStoreFactory.GitFileLockStore;

import java.util.Collection;
import java.util.Optional;

public class GitFileLockCommand implements FileLockCommand {

  private final GitContext context;
  private final GitFileLockStoreFactory lockStoreFactory;

  @Inject
  public GitFileLockCommand(@Assisted GitContext context, GitFileLockStoreFactory lockStoreFactory) {
    this.context = context;
    this.lockStoreFactory = lockStoreFactory;
  }

  @Override
  public LockCommandResult lock(LockCommandRequest request) {
    GitFileLockStore lockStore = getLockStore();
    lockStore.put(request.getFile());
    return new LockCommandResult(true);
  }

  @Override
  public UnlockCommandResult unlock(UnlockCommandRequest request) {
    GitFileLockStore lockStore = getLockStore();
    lockStore.remove(request.getFile(), request.isForce());
    return new UnlockCommandResult(true);
  }

  @Override
  public Optional<FileLock> status(LockStatusCommandRequest request) {
    GitFileLockStore lockStore = getLockStore();
    return lockStore.getLock(request.getFile());
  }

  @Override
  public Collection<FileLock> getAll() {
    GitFileLockStore lockStore = getLockStore();
    return lockStore.getAll();
  }

  private GitFileLockStore getLockStore() {
    return lockStoreFactory.create(context.getRepository());
  }

  public interface Factory {
    FileLockCommand create(GitContext context);
  }

}
