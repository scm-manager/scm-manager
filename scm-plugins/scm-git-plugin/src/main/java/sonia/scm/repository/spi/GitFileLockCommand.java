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
