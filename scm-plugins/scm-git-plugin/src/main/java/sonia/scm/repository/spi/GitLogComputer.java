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

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import org.eclipse.jgit.errors.InvalidObjectIdException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.revwalk.filter.RevFilter;
import org.eclipse.jgit.treewalk.filter.AndTreeFilter;
import org.eclipse.jgit.treewalk.filter.PathFilter;
import org.eclipse.jgit.treewalk.filter.TreeFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.NotFoundException;
import sonia.scm.repository.Changeset;
import sonia.scm.repository.ChangesetPagingResult;
import sonia.scm.repository.GitChangesetConverter;
import sonia.scm.repository.GitChangesetConverterFactory;
import sonia.scm.repository.GitUtil;
import sonia.scm.repository.InternalRepositoryException;
import sonia.scm.util.IOUtil;

import java.time.Instant;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import static sonia.scm.ContextEntry.ContextBuilder.entity;
import static sonia.scm.NotFoundException.notFound;

public class GitLogComputer {

  private static final Logger LOG = LoggerFactory.getLogger(GitLogComputer.class);

  private final String repositoryId;
  private final Repository gitRepository;
  private final GitChangesetConverterFactory converterFactory;

  public GitLogComputer(String repositoryId, Repository repository, GitChangesetConverterFactory converterFactory) {
    this.repositoryId = repositoryId;
    this.gitRepository = repository;
    this.converterFactory = converterFactory;
  }

  private RevFilter createFilter(LogCommandRequest request) {

    return new RevFilter() {
      @Override
      public boolean include(RevWalk walker, RevCommit commit) {
        PersonIdent author = commit.getAuthorIdent();
        PersonIdent committer = commit.getCommitterIdent();
        Instant when = committer.getWhenAsInstant();

        return isMatchingBefore(when)
          && isMatchingAfter(when)
          && isMatchingPartOfMessage(commit)
          && isMatchingPerson(author, committer);
      }

      private boolean isMatchingPartOfMessage(RevCommit commit) {
        return Strings.isNullOrEmpty(request.getPartOfMessage()) ||
          commit.getFullMessage().toLowerCase(Locale.ENGLISH).contains(request.getPartOfMessage());
      }

      private boolean isMatchingPerson(PersonIdent author, PersonIdent committer) {
        return Strings.isNullOrEmpty(request.getPerson()) ||
          author.getName().toLowerCase(Locale.ENGLISH).contains(request.getPerson()) ||
          author.getEmailAddress().toLowerCase(Locale.ENGLISH).contains(request.getPerson()) ||
          committer.getName().toLowerCase(Locale.ENGLISH).contains(request.getPerson()) ||
          committer.getEmailAddress().toLowerCase(Locale.ENGLISH).contains(request.getPerson());
      }

      private boolean isMatchingBefore(Instant when) {
        return request.getBefore() == null || when.isBefore(request.getBefore());
      }

      private boolean isMatchingAfter(Instant when) {
        return request.getAfter() == null || when.isAfter(request.getAfter());
      }

      @Override
      public RevFilter clone() {
        return this;
      }
    };
  }

  public ChangesetPagingResult compute(LogCommandRequest request) {
    LOG.debug("fetch changesets for request: {}", request);

    GitChangesetConverter converter = null;
    RevWalk revWalk = null;

    try {
      if (gitRepository.getAllRefs().isEmpty()) {
        LOG.debug("the repository with id {} seems to be empty", this.repositoryId);
        return new ChangesetPagingResult(0, Collections.emptyList());
      }

      int startIndex = request.getPagingStart();
      int limit = request.getPagingLimit();
      ObjectId startId = null;

      if (!Strings.isNullOrEmpty(request.getStartChangeset())) {
        startId = gitRepository.resolve(request.getStartChangeset());
      }

      ObjectId endId = null;

      if (!Strings.isNullOrEmpty(request.getEndChangeset())) {
        endId = gitRepository.resolve(request.getEndChangeset());
      }

      Ref branch = GitUtil.getBranchIdOrCurrentHead(gitRepository, request.getBranch());
      ObjectId branchId;
      if (branch == null) {
        if (request.getBranch() != null) {
          try {
            branchId = ObjectId.fromString(request.getBranch());
          } catch (InvalidObjectIdException e) {
            throw notFound(entity(GitLogCommand.REVISION, request.getBranch()).in(sonia.scm.repository.Repository.class, repositoryId));
          }
        } else {
          branchId = null;
        }
      } else {
        branchId = branch.getObjectId();
      }

      ObjectId ancestorId = null;

      if (!Strings.isNullOrEmpty(request.getAncestorChangeset())) {
        ancestorId = gitRepository.resolve(request.getAncestorChangeset());
        if (ancestorId == null) {
          throw notFound(entity(GitLogCommand.REVISION, request.getAncestorChangeset()).in(sonia.scm.repository.Repository.class, repositoryId));
        }
      }

      revWalk = new RevWalk(gitRepository);
      revWalk.setRevFilter(createFilter(request));

      converter = converterFactory.create(gitRepository, revWalk);

      if (!Strings.isNullOrEmpty(request.getPath())) {
        revWalk.setTreeFilter(
          AndTreeFilter.create(
            PathFilter.create(request.getPath()), TreeFilter.ANY_DIFF));
      }

      if (branchId == null && ancestorId != null) {
        throw notFound(entity(GitLogCommand.REVISION, request.getBranch()).in(sonia.scm.repository.Repository.class, repositoryId));
      }

      if (startId != null) {
        revWalk.markStart(revWalk.parseCommit(startId));
      } else {
        revWalk.markStart(revWalk.parseCommit(branchId));
      }

      if (ancestorId != null) {
        revWalk.markUninteresting(revWalk.parseCommit(ancestorId));
      }

      FetchResult fetchedCommits = startIndex < 0 ?
        fetchLastPage(revWalk, limit, endId, converter) :
        fetchPositivePageIndex(revWalk.iterator(), startIndex, limit, endId, converter);

      return new ChangesetPagingResult(
        fetchedCommits.total(),
        fetchedCommits.changesets(),
        branch != null ? GitUtil.getBranch(branch.getName()) : null,
        fetchedCommits.hasMore
      );

    } catch (MissingObjectException e) {
      throw notFound(entity(GitLogCommand.REVISION, e.getObjectId().getName()).in(sonia.scm.repository.Repository.class, repositoryId));
    } catch (NotFoundException e) {
      throw e;
    } catch (Exception ex) {
      throw new InternalRepositoryException(entity(sonia.scm.repository.Repository.class, repositoryId).build(), "could not create change log", ex);
    } finally {
      IOUtil.close(converter);
      GitUtil.release(revWalk);
    }
  }

  private FetchResult fetchPositivePageIndex(Iterator<RevCommit> iterator, int startIndex, int limit, ObjectId endId, GitChangesetConverter converter) {
    List<Changeset> changesets = Lists.newArrayList();
    boolean atCommitsEnd = false;
    int commitIndex = 0;

    while (iterator.hasNext()) {
      RevCommit commit = iterator.next();

      if (isWithinPage(commitIndex, startIndex, limit)) {
        changesets.add(converter.createChangeset(commit));
      }

      commitIndex++;

      if (commit.getId().equals(endId)) {
        atCommitsEnd = true;
        break;
      }

      if (isAfterPage(commitIndex, startIndex, limit)) {
        break;
      }
    }

    if (!iterator.hasNext()) {
      atCommitsEnd = true;
    }

    return new FetchResult(changesets, atCommitsEnd ? commitIndex : null, !atCommitsEnd);
  }

  private boolean isWithinPage(int commitIndex, int startIndex, int limit) {
    return (commitIndex >= startIndex) && ((limit < 0) || (commitIndex < startIndex + limit));
  }

  private boolean isAfterPage(int commitIndex, int startIndex, int limit) {
    return limit > -1 && (commitIndex >= startIndex + limit);
  }

  private FetchResult fetchLastPage(RevWalk revWalk, int limit, ObjectId endId, GitChangesetConverter converter) {
    List<RevCommit> changesets = Lists.newArrayList();
    int commitIndex = 0;

    for (RevCommit revCommit : revWalk) {
      if (changesets.size() == limit) {
        changesets.clear();
      }
      changesets.add(revCommit);
      commitIndex++;

      if (revCommit.getId().equals(endId)) {
        break;
      }
    }

    return new FetchResult(
      convertToChangeset(changesets, converter),
      commitIndex,
      false
    );
  }

  private List<Changeset> convertToChangeset(List<RevCommit> commits, GitChangesetConverter converter) {
    return commits.stream().map(converter::createChangeset).toList();
  }

  record FetchResult(List<Changeset> changesets, Integer total, boolean hasMore) {
  }
}
