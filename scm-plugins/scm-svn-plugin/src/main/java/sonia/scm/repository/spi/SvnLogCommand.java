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

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNLogEntry;
import org.tmatesoft.svn.core.io.SVNRepository;
import sonia.scm.repository.Changeset;
import sonia.scm.repository.ChangesetPagingResult;
import sonia.scm.repository.InternalRepositoryException;
import sonia.scm.repository.SvnUtil;
import sonia.scm.util.Util;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

import static sonia.scm.repository.SvnUtil.parseRevision;

public class SvnLogCommand extends AbstractSvnCommand implements LogCommand {

  private static final Logger LOG = LoggerFactory.getLogger(SvnLogCommand.class);

  SvnLogCommand(SvnContext context) {
    super(context);
  }

  @Override
  @SuppressWarnings("unchecked")
  public Changeset getChangeset(String revision, LogCommandRequest request) {
    Changeset changeset = null;

    if (LOG.isDebugEnabled()) {
      LOG.debug("fetch changeset {}", revision);
    }

    try {
      long revisionNumber;
      if ("head".equals(revision)) {
        revisionNumber = -1;
      } else {
        revisionNumber = parseRevision(revision, repository);
        Preconditions.checkArgument(revisionNumber > 0, "revision must be greater than zero: %d", revisionNumber);
      }
      SVNRepository repo = open();
      Collection<SVNLogEntry> entries = repo.log(null, null, revisionNumber,
        revisionNumber, true, true);

      if (Util.isNotEmpty(entries)) {
        changeset = SvnUtil.createChangeset(entries.iterator().next());
      }
    } catch (SVNException ex) {
      throw new InternalRepositoryException(repository, "could not open repository", ex);
    }

    return changeset;
  }

  @Override
  public ChangesetPagingResult getChangesets(LogCommandRequest request) {
    if (LOG.isDebugEnabled()) {
      LOG.debug("fetch changesets for {}", request);
    }

    ChangesetPagingResult changesets;
    int start = request.getPagingStart();
    int limit = request.getPagingLimit();
    long startRevision = parseRevision(request.getStartChangeset(), repository);
    long endRevision = parseRevision(request.getEndChangeset(), repository);

    try {
      SVNRepository repo = open();

      if (isFilteredRequest(startRevision, endRevision, request)) {
        changesets = getChangesetsWithFilters(repo, startRevision, endRevision, start, limit, request);
      } else {
        changesets = getChangesetsWithNoFilters(repo, start, limit);
      }
    } catch (SVNException ex) {
      throw new InternalRepositoryException(repository, "could not open repository", ex);
    }

    return changesets;
  }

  private boolean isFilteredRequest(long startRevision, long endRevision, LogCommandRequest request) {
    return (startRevision > 0) ||
      (endRevision > 0) ||
      !Strings.isNullOrEmpty(request.getPath()) ||
      !Strings.isNullOrEmpty(request.getPartOfMessage()) ||
      !Strings.isNullOrEmpty(request.getPerson()) ||
      request.getBefore() != null ||
      request.getAfter() != null;
  }

  private ChangesetPagingResult getChangesetsWithNoFilters(SVNRepository repo,
                                                           int start,
                                                           int limit) throws SVNException {
    List<Changeset> changesets = Lists.newArrayList();
    long latest = repo.getLatestRevision();

    if (start <= 0 && limit <= 0) {
      repo.log(null, latest, 1, true, true, new ChangesetCollector(changesets));
      return new ChangesetPagingResult((int) latest, changesets);
    }

    long startRev;
    long endRev = 1;

    if (start < 0) {
      long remainder = latest % limit;
      startRev = remainder == 0 ? endRev + limit - 1 : endRev + remainder - 1;
    } else {
      startRev = latest - start;
    }

    if (limit > 0) {
      endRev = Math.max(startRev - (limit - 1), 1);
    }


    if (startRev > 0) {
      LOG.debug("fetch changeset from {} to {}", startRev, endRev);
      repo.log(null, startRev, endRev, true, true, new ChangesetCollector(changesets));
    }

    return new ChangesetPagingResult((int) latest, changesets);
  }


  private ChangesetPagingResult getChangesetsWithFilters(SVNRepository repo,
                                                         long startRevision,
                                                         long endRevision,
                                                         int start,
                                                         int limit,
                                                         LogCommandRequest request) throws SVNException {
    long startRev;
    long endRev = Math.max(endRevision, 1);
    long maxRev = repo.getLatestRevision();

    if (startRevision >= 0) {
      startRev = startRevision;
    } else {
      startRev = maxRev;
    }

    LOG.debug("fetch changeset from {} to {} for path {}", startRev, endRev, request.getPath());

    List<Changeset> changesets = loadFilteredChangesets(repo, startRev, endRev, request);

    if (changesets.isEmpty() || start >= changesets.size()) {
      return new ChangesetPagingResult(changesets.size(), new ArrayList<>());
    }

    int total = changesets.size();
    int end = total;

    if (start <= 0 && limit <= 0) {
      return new ChangesetPagingResult(total, changesets);
    }

    if (start < 0) {
      int remainder = total % limit;
      start = remainder == 0 ? total - limit : total - remainder;
    } else {
      end = Math.min(total, start + limit);
    }

    LOG.trace("create sublist from {} to {} of total {} collected changesets", start, end, total);

    return new ChangesetPagingResult(total, new ArrayList<>(changesets.subList(start, end)));
  }

  private List<Changeset> loadFilteredChangesets(SVNRepository repo, long startRev, long endRev, LogCommandRequest request) throws SVNException {
    List<Changeset> changesets = Lists.newArrayList();
    String[] paths = Strings.isNullOrEmpty(request.getPath()) ? null : new String[]{request.getPath()};

    repo.log(paths, startRev, endRev, true, true, new ChangesetCollector(changesets));

    return changesets
      .stream()
      .filter(changeset -> applyDescriptionFilters(request, changeset))
      .filter(changeset -> applyPersonFilters(request, changeset))
      .filter(changeset -> applyWhenFilters(request, changeset))
      .toList();
  }

  private boolean applyDescriptionFilters(LogCommandRequest request, Changeset changeset) {
    String description = changeset.getDescription().toLowerCase(Locale.ENGLISH);
    return Strings.isNullOrEmpty(request.getPartOfMessage()) || description.contains(request.getPartOfMessage());
  }

  private boolean applyPersonFilters(LogCommandRequest request, Changeset changeset) {
    String authorName = changeset.getAuthor().getName().toLowerCase(Locale.ENGLISH);
    return Strings.isNullOrEmpty(request.getPerson()) || authorName.contains(request.getPerson());
  }

  private boolean applyWhenFilters(LogCommandRequest request, Changeset changeset) {
    Instant when = Instant.ofEpochMilli(changeset.getDate());
    return (request.getBefore() == null || when.isBefore(request.getBefore()))
      && (request.getAfter() == null || when.isAfter(request.getAfter()));
  }
}
