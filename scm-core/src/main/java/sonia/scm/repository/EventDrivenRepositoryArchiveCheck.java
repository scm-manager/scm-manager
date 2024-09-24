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
import sonia.scm.plugin.Extension;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

/**
 * Default implementation of {@link RepositoryArchivedCheck}. This tracks the archive status of repositories by using
 * {@link RepositoryModificationEvent}s. The initial set of archived repositories is read by
 * {@link EventDrivenRepositoryArchiveCheckInitializer} on startup.
 */
@Extension
public final class EventDrivenRepositoryArchiveCheck implements RepositoryArchivedCheck {

  private static final Collection<String> ARCHIVED_REPOSITORIES = Collections.synchronizedSet(new HashSet<>());

  static void setAsArchived(String repositoryId) {
    ARCHIVED_REPOSITORIES.add(repositoryId);
  }

  static void removeFromArchived(String repositoryId) {
    ARCHIVED_REPOSITORIES.remove(repositoryId);
  }

  static boolean isRepositoryArchived(String repositoryId) {
    return ARCHIVED_REPOSITORIES.contains(repositoryId);
  }

  @Override
  public boolean isArchived(String repositoryId) {
    return isRepositoryArchived(repositoryId);
  }

  @Subscribe(async = false)
  public void updateListener(RepositoryModificationEvent event) {
    Repository repository = event.getItem();
    if (repository.isArchived()) {
      EventDrivenRepositoryArchiveCheck.setAsArchived(repository.getId());
    } else {
      EventDrivenRepositoryArchiveCheck.removeFromArchived(repository.getId());
    }
  }
}
