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
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
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

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

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

  public ChangesetPagingResult compute(LogCommandRequest request) {
    LOG.debug("fetch changesets for request: {}", request);

    GitChangesetConverter converter = null;
    RevWalk revWalk = null;

    try {
      if (!gitRepository.getAllRefs().isEmpty()) {
        int counter = 0;
        int start = request.getPagingStart();

        if (start < 0) {
          LOG.error("start parameter is negative, reset to 0");

          start = 0;
        }

        List<Changeset> changesetList = Lists.newArrayList();
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

        converter = converterFactory.create(gitRepository, revWalk);

        if (!Strings.isNullOrEmpty(request.getPath())) {
          revWalk.setTreeFilter(
            AndTreeFilter.create(
              PathFilter.create(request.getPath()), TreeFilter.ANY_DIFF));
        }

        if (branchId != null) {
          if (startId != null) {
            revWalk.markStart(revWalk.parseCommit(startId));
          } else {
            revWalk.markStart(revWalk.parseCommit(branchId));
          }

          if (ancestorId != null) {
            revWalk.markUninteresting(revWalk.parseCommit(ancestorId));
          }

          Iterator<RevCommit> iterator = revWalk.iterator();

          while (iterator.hasNext()) {
            RevCommit commit = iterator.next();

            if ((counter >= start)
              && ((limit < 0) || (counter < start + limit))) {
              changesetList.add(converter.createChangeset(commit));
            }

            counter++;

            if (commit.getId().equals(endId)) {
              break;
            }
          }
        } else if (ancestorId != null) {
          throw notFound(entity(GitLogCommand.REVISION, request.getBranch()).in(sonia.scm.repository.Repository.class, repositoryId));
        }

        if (branch != null) {
          return new ChangesetPagingResult(counter, changesetList, GitUtil.getBranch(branch.getName()));
        } else {
          return new ChangesetPagingResult(counter, changesetList);
        }
      } else {
        LOG.debug("the repository with id {} seems to be empty", this.repositoryId);

        return new ChangesetPagingResult(0, Collections.emptyList());
      }
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
}
