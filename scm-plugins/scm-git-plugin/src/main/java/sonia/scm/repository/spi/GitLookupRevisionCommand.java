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
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevObject;
import org.eclipse.jgit.revwalk.RevWalk;
import sonia.scm.repository.Changeset;
import sonia.scm.repository.GitChangesetConverter;
import sonia.scm.repository.GitChangesetConverterFactory;
import sonia.scm.repository.api.LookupRevisionCommandResult;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static java.util.Collections.emptyList;
import static java.util.Optional.empty;
import static java.util.Optional.of;

public class GitLookupRevisionCommand extends AbstractGitCommand implements LookupRevisionCommand {

  private final GitChangesetConverterFactory converterFactory;

  @Inject
  GitLookupRevisionCommand(@Assisted GitContext context, GitChangesetConverterFactory converterFactory) {
    super(context);
    this.converterFactory = converterFactory;
  }

  @Override
  public LookupRevisionCommandResult getChangesets(LookupRevisionCommandRequest request) {
    Repository gitRepository = open();
    GitChangesetConverter changesetConverter = converterFactory.create(gitRepository);
    try (RevWalk revWalk = new RevWalk(gitRepository)) {
      try {
        return findUniqueCandidate(request, gitRepository, revWalk, changesetConverter);
      } catch (AmbiguousObjectException e) {
        return analyseAmbiguity(request, e, revWalk, changesetConverter);
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private LookupRevisionCommandResult findUniqueCandidate(
          LookupRevisionCommandRequest request,
          Repository gitRepository,
          RevWalk revWalk,
          GitChangesetConverter changesetConverter) throws IOException {
    ObjectId resolved = gitRepository.resolve(request.getRevisionPrefix());
    if (resolved == null) {
      return new LookupRevisionCommandResult(emptyList(), false);
    }
    return new LookupRevisionCommandResult(
      checkWhetherCommit(
        revWalk,
        changesetConverter,
        resolved
      )
      .map(List::of)
      .orElseGet(Collections::emptyList),
      false);
  }

  private LookupRevisionCommandResult analyseAmbiguity(
          LookupRevisionCommandRequest request,
          AmbiguousObjectException e,
          RevWalk revWalk,
          GitChangesetConverter changesetConverter) throws IOException {
    Collection<ObjectId> candidates = e.getCandidates();
    Collection<Changeset> changesets = new ArrayList<>(candidates.size());
    boolean limitReached = false;
    for (ObjectId candidate : candidates) {
      if (changesets.size() >= request.getLimit()) {
        limitReached = true;
        break;
      }
      checkWhetherCommit(revWalk, changesetConverter, candidate)
        .ifPresent(changesets::add);
    }
    return new LookupRevisionCommandResult(changesets, limitReached);
  }

  private Optional<Changeset> checkWhetherCommit(RevWalk revWalk, GitChangesetConverter changesetConverter, ObjectId candidate) throws IOException {
    RevObject peel = revWalk.peel(revWalk.parseAny(candidate));
    if (peel instanceof RevCommit commit) {
      Changeset changeset = changesetConverter.createChangeset(commit);
      return of(changeset);
    }
    return empty();
  }

  public interface Factory {
    GitLookupRevisionCommand create(GitContext context);
  }
}
