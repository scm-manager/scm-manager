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

import com.github.legman.Subscribe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.EagerSingleton;
import sonia.scm.plugin.Extension;
import sonia.scm.repository.Changeset;
import sonia.scm.repository.InternalRepositoryException;
import sonia.scm.repository.Modifications;
import sonia.scm.repository.PreReceiveRepositoryHookEvent;
import sonia.scm.repository.Repository;
import sonia.scm.repository.api.RepositoryService;
import sonia.scm.repository.api.RepositoryServiceFactory;

import javax.inject.Inject;
import java.io.IOException;

@Extension
@EagerSingleton
public class FileLockPreCommitHook {

  private static final Logger LOG = LoggerFactory.getLogger(FileLockPreCommitHook.class);

  private final GitLockStoreFactory lockStoreFactory;
  private final RepositoryServiceFactory serviceFactory;

  @Inject
  public FileLockPreCommitHook(GitLockStoreFactory lockStoreFactory, RepositoryServiceFactory serviceFactory) {
    this.lockStoreFactory = lockStoreFactory;
    this.serviceFactory = serviceFactory;
  }

  @Subscribe(async = false)
  public void checkForLocks(PreReceiveRepositoryHookEvent event) {
    Repository repository = event.getRepository();
    LOG.trace("checking for locks during push in repository {}", repository);
    GitLockStoreFactory.GitLockStore gitLockStore = lockStoreFactory.create(repository);
    if (!gitLockStore.hasLocks()) {
      LOG.trace("no locks found in repository {}", repository);
      return;
    }
    try (RepositoryService service = serviceFactory.create(repository)) {
      checkPaths(event, gitLockStore, service);
    } catch (IOException e) {
      throw new InternalRepositoryException(repository, "could not check locks", e);
    }
  }

  private void checkPaths(PreReceiveRepositoryHookEvent event, GitLockStoreFactory.GitLockStore gitLockStore, RepositoryService service) throws IOException {
    new Checker(gitLockStore, service)
      .checkPaths(event.getContext().getChangesetProvider().getChangesets());
  }

  private static class Checker {

    private final GitLockStoreFactory.GitLockStore lockStore;
    private final RepositoryService service;

    private Checker(GitLockStoreFactory.GitLockStore lockStore, RepositoryService service) {
      this.lockStore = lockStore;
      this.service = service;
    }

    private void checkPaths(Iterable<Changeset> changesets) throws IOException {
      for (Changeset c : changesets) {
        checkPaths(c);
      }
    }

    private void checkPaths(Changeset changeset) throws IOException {
      LOG.trace("checking changeset {}", changeset.getId());
      Modifications modifications = service.getModificationsCommand()
        .revision(changeset.getId())
        .getModifications();

      if (modifications != null) {
        checkPaths(modifications);
      } else {
        LOG.trace("no modifications for the changeset {} found", changeset.getId());
      }
    }

    private void checkPaths(Modifications modifications) {
      check(modifications.getEffectedPaths());
    }

    private void check(Iterable<String> modifiedPaths) {
      for (String path : modifiedPaths) {
        lockStore.assertModifiable(path);
      }
    }
  }
}
