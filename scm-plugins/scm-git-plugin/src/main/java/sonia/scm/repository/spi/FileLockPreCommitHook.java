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

import com.github.legman.Subscribe;
import jakarta.inject.Inject;
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

import java.io.IOException;

@Extension
@EagerSingleton
public class FileLockPreCommitHook {

  private static final Logger LOG = LoggerFactory.getLogger(FileLockPreCommitHook.class);

  private final GitFileLockStoreFactory fileLockStoreFactory;
  private final RepositoryServiceFactory serviceFactory;

  @Inject
  public FileLockPreCommitHook(GitFileLockStoreFactory fileLockStoreFactory, RepositoryServiceFactory serviceFactory) {
    this.fileLockStoreFactory = fileLockStoreFactory;
    this.serviceFactory = serviceFactory;
  }

  @Subscribe(async = false)
  public void checkForLocks(PreReceiveRepositoryHookEvent event) {
    Repository repository = event.getRepository();
    LOG.trace("checking for locks during push in repository {}", repository);
    GitFileLockStoreFactory.GitFileLockStore gitFileLockStore = fileLockStoreFactory.create(repository);
    if (!gitFileLockStore.hasLocks()) {
      LOG.trace("no locks found in repository {}", repository);
      return;
    }
    try (RepositoryService service = serviceFactory.create(repository)) {
      checkPaths(event, gitFileLockStore, service);
    } catch (IOException e) {
      throw new InternalRepositoryException(repository, "could not check locks", e);
    }
  }

  private void checkPaths(PreReceiveRepositoryHookEvent event, GitFileLockStoreFactory.GitFileLockStore gitFileLockStore, RepositoryService service) throws IOException {
    new Checker(gitFileLockStore, service)
      .checkPaths(event.getContext().getChangesetProvider().getChangesets());
  }

  private static class Checker {

    private final GitFileLockStoreFactory.GitFileLockStore fileLockStore;
    private final RepositoryService service;

    private Checker(GitFileLockStoreFactory.GitFileLockStore fileLockStore, RepositoryService service) {
      this.fileLockStore = fileLockStore;
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
        fileLockStore.assertModifiable(path);
      }
    }
  }
}
