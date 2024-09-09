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
import org.eclipse.jgit.errors.AmbiguousObjectException;
import sonia.scm.NotUniqueRevisionException;
import sonia.scm.repository.Repository;
import sonia.scm.repository.api.DiffResult;

import java.io.IOException;

public class GitDiffResultCommand extends AbstractGitCommand implements DiffResultCommand {

  @Inject
  GitDiffResultCommand(@Assisted GitContext context) {
    super(context);
  }

  public DiffResult getDiffResult(DiffCommandRequest request) throws IOException {
    org.eclipse.jgit.lib.Repository repository = open();
    return new GitDiffResult(
      this.repository,
      repository,
      Differ.diff(repository, request),
      request.getIgnoreWhitespaceLevel(),
      0,
      null
    );
  }

  @Override
  public DiffResult getDiffResult(DiffResultCommandRequest request) throws IOException {
    org.eclipse.jgit.lib.Repository repository = open();
    int offset = request.getOffset() == null ? 0 : request.getOffset();
    try {
      return new GitDiffResult(
        this.repository,
        repository,
        Differ.diff(repository, request),
        request.getIgnoreWhitespaceLevel(),
        offset,
        request.getLimit()
      );
    } catch (AmbiguousObjectException ex) {
      throw new NotUniqueRevisionException(Repository.class, context.getRepository().getId());
    }
  }

  public interface Factory {
    DiffResultCommand create(GitContext context);
  }

}
