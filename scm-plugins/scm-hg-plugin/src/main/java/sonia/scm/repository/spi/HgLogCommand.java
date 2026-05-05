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
import com.google.inject.assistedinject.Assisted;
import jakarta.inject.Inject;
import sonia.scm.repository.Changeset;
import sonia.scm.repository.ChangesetPagingResult;
import sonia.scm.repository.spi.javahg.HgLogChangesetCommand;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HgLogCommand extends AbstractCommand implements LogCommand {

  @Inject
  HgLogCommand(@Assisted HgCommandContext context) {
    super(context);
  }

  @Override
  public Changeset getChangeset(String id, LogCommandRequest request) {
    org.javahg.Repository repository = open();
    HgLogChangesetCommand cmd = on(repository);

    return cmd.rev(id).single();
  }

  @Override
  public ChangesetPagingResult getChangesets(LogCommandRequest request) {
    org.javahg.Repository repository = open();

    if (!Strings.isNullOrEmpty(request.getPath())
      || !Strings.isNullOrEmpty(request.getBranch())
      || !Strings.isNullOrEmpty(request.getAncestorChangeset())
      || !Strings.isNullOrEmpty(request.getPartOfMessage())
      || request.getBefore() != null
      || request.getAfter() != null
      || !Strings.isNullOrEmpty(request.getPerson())) {
      return collectSafely(repository, request);
    }

    int start = -1;
    int end = 0;

    String startChangeset = request.getStartChangeset();
    String endChangeset = request.getEndChangeset();

    if (!Strings.isNullOrEmpty(startChangeset)) {
      start = on(repository).rev(startChangeset).singleRevision();
    } else if (!Strings.isNullOrEmpty(endChangeset)) {
      end = on(repository).rev(endChangeset).singleRevision();
    }

    if (start < 0) {
      start = on(repository).rev("tip").singleRevision();
    }

    // SingleRevision might return -1 if the repository is empty
    if (start < 0) {
      return new ChangesetPagingResult(0, new ArrayList<>());
    }

    int total = start - end + 1;
    int limit = request.getPagingLimit();

    if (total <= limit || limit <= 0) {
      return new ChangesetPagingResult(total, fetchChangesetsBasedFromStartToEnd(repository, start, end));
    }

    if (request.getPagingStart() >= 0) {
      start -= request.getPagingStart();
      end = Math.max(end, start - request.getPagingLimit() + 1);
    } else {
      int remainder = total % request.getPagingLimit();
      start = remainder == 0 ? end + request.getPagingLimit() - 1 : end + remainder - 1;
    }

    if (start < end) {
      return new ChangesetPagingResult(total, new ArrayList<>());
    }

    return new ChangesetPagingResult(total, fetchChangesetsBasedFromStartToEnd(repository, start, end));
  }

  private List<Changeset> fetchChangesetsBasedFromStartToEnd(org.javahg.Repository repository, int start, int end) {
    return on(repository).rev(start + ":"
      + end).execute();
  }

  private ChangesetPagingResult collectSafely(
    org.javahg.Repository repository, LogCommandRequest request) {
    HgLogChangesetCommand cmd = on(repository);
    String startChangeset = request.getStartChangeset();
    String endChangeset = request.getEndChangeset();
    String ancestorChangeset = request.getAncestorChangeset();

    String revset = null;

    if (!Strings.isNullOrEmpty(startChangeset) && !Strings.isNullOrEmpty(endChangeset)) {
      revset = startChangeset.concat(":").concat(endChangeset);
    } else if (!Strings.isNullOrEmpty(startChangeset) && !Strings.isNullOrEmpty(ancestorChangeset)) {
      int start = on(repository).rev(startChangeset).singleRevision();
      int ancestor = on(repository).rev(ancestorChangeset).singleRevision();
      revset = String.format("only(%s,%s)", start, ancestor);
    } else if (!Strings.isNullOrEmpty(endChangeset)) {
      revset = "tip:".concat(endChangeset);
    } else if (!Strings.isNullOrEmpty(startChangeset)) {
      revset = startChangeset.concat(":0");
    }

    String partOfMessage = request.getPartOfMessage();
    if (!Strings.isNullOrEmpty(partOfMessage)) {
      String searchRevset = String.format("desc('%s')", escapeSingleQuote(partOfMessage));
      revset = applyFilter(revset, searchRevset);
    }

    String person = request.getPerson();
    if (!Strings.isNullOrEmpty(person)) {
      String searchRevset = String.format("author('%s')", escapeSingleQuote(person));
      revset = applyFilter(revset, searchRevset);
    }

    Instant before = request.getBefore();
    if (before != null) {
      String searchRevset = String.format("date('<%s')", before);
      revset = applyFilter(revset, searchRevset);
    }

    Instant after = request.getAfter();
    if (after != null) {
      String searchRevset = String.format("date('>%s')", after);
      revset = applyFilter(revset, searchRevset);
    }

    if (revset != null) {
      revset = String.format("sort((%s), -date)", revset);
      cmd.rev(revset);
    }

    if (!Strings.isNullOrEmpty(request.getBranch())) {
      cmd.branch(request.getBranch());
    }

    int start = request.getPagingStart();
    int limit = request.getPagingLimit();

    List<Changeset> changesets;

    if ((start <= 0) && (limit <= 0)) {
      if (!Strings.isNullOrEmpty(request.getPath())) {
        changesets = cmd.execute(request.getPath());
      } else {
        changesets = cmd.execute();
      }

      return new ChangesetPagingResult(changesets.size(), changesets);
    }

    List<Integer> revisionList;
    if (!Strings.isNullOrEmpty(request.getPath())) {
      revisionList = cmd.loadRevisions(request.getPath());
    } else {
      revisionList = cmd.loadRevisions();
    }

    if (revisionList.isEmpty() || start >= revisionList.size()) {
      return new ChangesetPagingResult(revisionList.size(), Collections.emptyList());
    }

    int total = revisionList.size();
    if (start < 0) {
      int remainder = total % limit;
      start = remainder == 0 ? revisionList.size() - limit : revisionList.size() - remainder;
      limit = revisionList.size();
    } else {
      limit = limit < 0 ? revisionList.size() : Math.min(start + limit, revisionList.size());
    }

    List<Integer> sublist = revisionList.subList(start, limit);

    String[] revs = new String[sublist.size()];

    for (int i = 0; i < sublist.size(); i++) {
      revs[i] = sublist.get(i).toString();
    }

    changesets = on(repository).rev(revs).execute();

    return new ChangesetPagingResult(total, changesets);
  }


  private String escapeSingleQuote(String filter) {
    if (filter.contains("'") || filter.contains("\\")) {
      return filter.replace("\\", "\\\\").replace("'", "\\'");
    }

    return filter;
  }

  private String applyFilter(String revset, String filter) {
    if (revset != null) {
      return String.format("(%s) and %s", revset, filter);
    } else {
      return filter;
    }
  }

  private HgLogChangesetCommand on(org.javahg.Repository repository) {
    return HgLogChangesetCommand.on(repository, getContext().getConfig());
  }

  public interface Factory {
    HgLogCommand create(HgCommandContext context);
  }

}
