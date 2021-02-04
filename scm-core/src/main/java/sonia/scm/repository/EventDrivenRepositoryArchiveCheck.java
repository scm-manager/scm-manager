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

package sonia.scm.repository;

import com.github.legman.Subscribe;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

/**
 * Default implementation of {@link RepositoryArchivedCheck}. This tracks the archive status of repositories by using
 * {@link RepositoryModificationEvent}s. The initial set of archived repositories is read by
 * {@link EventDrivenRepositoryArchiveCheckInitializer} on startup.
 */
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
