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

package sonia.scm.repository;

import com.github.legman.Subscribe;
import com.google.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.EagerSingleton;
import sonia.scm.NotFoundException;
import sonia.scm.plugin.Extension;
import sonia.scm.web.security.AdministrationContext;
import sonia.scm.web.security.PrivilegedAction;

/**
 * @since 1.37
 */
@Extension
@EagerSingleton
public final class LastModifiedUpdateListener {

  private static final Logger LOG = LoggerFactory.getLogger(LastModifiedUpdateListener.class);

  private final AdministrationContext adminContext;

  private final RepositoryManager repositoryManager;

  @Inject
  public LastModifiedUpdateListener(AdministrationContext adminContext,
                                    RepositoryManager repositoryManager) {
    this.adminContext = adminContext;
    this.repositoryManager = repositoryManager;
  }

  @Subscribe
  public void onPostReceive(PostReceiveRepositoryHookEvent event) {
    final Repository repository = event.getRepository();

    if (repository != null) {
      //J-
      adminContext.runAsAdmin(
        new LastModifiedPrivilegedAction(repositoryManager, repository)
      );
      //J+
    } else {
      LOG.warn("received hook without repository");
    }
  }

  static class LastModifiedPrivilegedAction implements PrivilegedAction {

    private final Repository repository;

    private final RepositoryManager repositoryManager;

    public LastModifiedPrivilegedAction(RepositoryManager repositoryManager,
                                        Repository repository) {
      this.repositoryManager = repositoryManager;
      this.repository = repository;
    }

    @Override
    public void run() {
      Repository dbr = repositoryManager.get(repository.getId());

      if (dbr != null) {
        LOG.debug("update last modified date of repository {}", dbr.getId());
        dbr.setLastModified(System.currentTimeMillis());

        try {
          repositoryManager.modify(dbr);
        } catch (NotFoundException e) {
          LOG.error("could not modify repository", e);
        }
      } else {
        LOG.error("could not find repository with id {}", repository.getId());
      }
    }
  }
}
