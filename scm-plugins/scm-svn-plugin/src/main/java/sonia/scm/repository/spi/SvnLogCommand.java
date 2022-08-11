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

package sonia.scm.repository.spi;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tmatesoft.svn.core.ISVNLogEntryHandler;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNLogEntry;
import org.tmatesoft.svn.core.io.SVNRepository;
import sonia.scm.repository.Changeset;
import sonia.scm.repository.ChangesetPagingResult;
import sonia.scm.repository.InternalRepositoryException;
import sonia.scm.repository.SvnUtil;
import sonia.scm.util.Util;

import java.util.Collection;
import java.util.List;

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
      long revisioNumber = parseRevision(revision, repository);
      Preconditions.checkArgument(revisioNumber > 0, "revision must be greater than zero: %d", revisioNumber);
      SVNRepository repo = open();
      Collection<SVNLogEntry> entries = repo.log(null, null, revisioNumber,
        revisioNumber, true, true);

      if (Util.isNotEmpty(entries)) {
        changeset = SvnUtil.createChangeset(entries.iterator().next());
      }
    } catch (SVNException ex) {
      throw new InternalRepositoryException(repository, "could not open repository", ex);
    }

    return changeset;
  }

  @Override
  @SuppressWarnings("unchecked")
  public ChangesetPagingResult getChangesets(LogCommandRequest request) {
    if (LOG.isDebugEnabled()) {
      LOG.debug("fetch changesets for {}", request);
    }

    ChangesetPagingResult changesets;
    int start = request.getPagingStart();
    int limit = request.getPagingLimit();
    long startRevision = parseRevision(request.getStartChangeset(), repository);
    long endRevision = parseRevision(request.getEndChangeset(), repository);
    String[] pathArray = null;

    if (!Strings.isNullOrEmpty(request.getPath())) {
      pathArray = new String[]{request.getPath()};
    }

    try {
      SVNRepository repo = open();

      if ((startRevision > 0) || (pathArray != null)) {
        changesets = getChangesets(repo, startRevision, endRevision, start,
          limit, pathArray);
      } else {
        changesets = getChangesets(repo, start, limit);
      }
    } catch (SVNException ex) {
      throw new InternalRepositoryException(repository, "could not open repository", ex);
    }

    return changesets;
  }

  private ChangesetPagingResult getChangesets(SVNRepository repo, int start,
                                              int limit)
    throws SVNException {
    long latest = repo.getLatestRevision();
    long startRev = latest - start;
    long endRev = Math.max(startRev - (limit - 1), 1);

    final List<Changeset> changesets = Lists.newArrayList();

    if (startRev > 0) {
      LOG.debug("fetch changeset from {} to {}", startRev, endRev);
      repo.log(null, startRev, endRev, true, true,
        new ChangesetCollector(changesets));
    }

    return new ChangesetPagingResult((int) latest, changesets);
  }

  @SuppressWarnings("unchecked")
  private ChangesetPagingResult getChangesets(SVNRepository repo,
                                              long startRevision, long endRevision, int start, int limit, String[] path)
    throws SVNException {
    long startRev;
    long endRev = Math.max(endRevision, 0);
    long maxRev = repo.getLatestRevision();

    if (startRevision >= 0) {
      startRev = startRevision;
    } else {
      startRev = maxRev;
    }

    List<SVNLogEntry> changesetList = Lists.newArrayList();

    LOG.debug("fetch changeset from {} to {} for path {}", startRev, endRev,
      path);

    Collection<SVNLogEntry> entries = repo.log(path, null, startRev, endRev,
      true, true);

    for (SVNLogEntry entry : entries) {
      if (entry.getRevision() <= maxRev) {
        changesetList.add(entry);
      }
    }

    int total = changesetList.size();
    int max = limit + start;
    int end = total;

    if ((max > 0) && (end > max)) {
      end = max;
    }

    if (start < 0) {
      start = 0;
    }

    LOG.trace(
      "create sublist from {} to {} of total {} collected changesets", start,
      end, total);

    changesetList = changesetList.subList(start, end);

    return new ChangesetPagingResult(total,
      SvnUtil.createChangesets(changesetList));
  }
}
