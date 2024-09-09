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

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.revwalk.RevCommit;
import sonia.scm.repository.InternalRepositoryException;
import sonia.scm.repository.Repository;

import java.io.IOException;
import java.util.concurrent.Callable;

import static sonia.scm.ContextEntry.ContextBuilder.entity;

class GitLazyChangesetResolver implements Callable<Iterable<RevCommit>> {
  private final Repository repository;
  private final Git git;

  public GitLazyChangesetResolver(Repository repository, Git git) {
    this.repository = repository;
    this.git = git;
  }

  @Override
  public Iterable<RevCommit> call() {
    try {
      return git.log().all().call();
    } catch (IOException | GitAPIException e) {
      throw new InternalRepositoryException(
        entity(repository).build(),
        "Could not resolve changesets for imported repository",
        e
      );
    }
  }
}
