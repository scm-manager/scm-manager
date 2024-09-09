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
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import sonia.scm.repository.Changeset;
import sonia.scm.repository.GitChangesetConverter;
import sonia.scm.repository.GitChangesetConverterFactory;
import sonia.scm.repository.GitUtil;
import sonia.scm.repository.InternalRepositoryException;

import java.io.IOException;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
public class GitChangesetsCommand extends AbstractGitCommand implements ChangesetsCommand {

  private final GitChangesetConverterFactory converterFactory;

  @Inject
  GitChangesetsCommand(@Assisted GitContext context, GitChangesetConverterFactory converterFactory) {
    super(context);
    this.converterFactory = converterFactory;
  }

  @Override
  public Iterable<Changeset> getChangesets(ChangesetsCommandRequest request) {
    try {
      log.debug("computing changesets for repository {}", repository);
      Repository gitRepository = open();

      try (RevWalk revWalk = new RevWalk(gitRepository)) {
        revWalk.markStart(GitUtil.getAllCommits(gitRepository, revWalk).collect(Collectors.toList()));
        log.trace("got git iterator for all changesets for repository {}", repository);
        Iterator<RevCommit> iterator = revWalk.iterator();
        return () -> new ChangesetIterator(iterator, revWalk, gitRepository);
      } finally {
        log.trace("returned iterator for all changesets for repository {}", gitRepository);
      }
    } catch (IOException e) {
      throw new InternalRepositoryException(context.getRepository(), "failed to get latest commit", e);
    }
  }

  @Override
  public Optional<Changeset> getLatestChangeset() {
    try {
      Repository repository = open();

      try (RevWalk revWalk = new RevWalk(repository)) {
        return GitUtil.getAllCommits(repository, revWalk)
          .max(new ByCommitDateComparator())
          .map(commit -> converterFactory.create(repository, revWalk).createChangeset(commit));
      }
    } catch (IOException e) {
      throw new InternalRepositoryException(context.getRepository(), "failed to get latest commit", e);
    }
  }

  private class ChangesetIterator implements Iterator<Changeset> {

    private final Iterator<RevCommit> iterator;
    private final GitChangesetConverter changesetConverter;
    private final RevWalk revWalk;

    ChangesetIterator(Iterator<RevCommit> iterator, RevWalk revWalk, Repository gitRepository) {
      this.iterator = iterator;
      this.changesetConverter = converterFactory.create(gitRepository, revWalk);
      this.revWalk = revWalk;
    }

    @Override
    public boolean hasNext() {
      return iterator.hasNext();
    }

    @Override
    public Changeset next() {
      try {
        log.trace("mapping changeset for repository {}", repository);
        return changesetConverter.createChangeset(revWalk.parseCommit(iterator.next()));
      } catch (IOException e) {
        throw new InternalRepositoryException(context.getRepository(), "failed to create changeset for single git revision", e);
      }
    }
  }

  private static class ByCommitDateComparator implements Comparator<RevCommit> {
    @Override
    public int compare(RevCommit rev1, RevCommit rev2) {
      long commitTime1 = rev1.getCommitTime();
      long commitTime2 = rev2.getCommitTime();
      return Long.compare(commitTime1, commitTime2);
    }
  }

  public interface Factory {
    ChangesetsCommand create(GitContext context);
  }

}
