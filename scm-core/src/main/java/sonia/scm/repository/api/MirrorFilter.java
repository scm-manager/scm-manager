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

package sonia.scm.repository.api;

import com.google.common.annotations.Beta;
import sonia.scm.repository.Changeset;
import sonia.scm.repository.Tag;

import java.util.Collection;
import java.util.Optional;

import static java.util.Collections.emptyList;

@Beta
public interface MirrorFilter {

  default Filter getFilter(FilterContext context) {
    return new Filter() {};
  }

  interface Filter {

    default Result acceptBranch(BranchUpdate branch) {
      return Result.accept();
    }

    default Result acceptTag(TagUpdate tag) {
      return Result.accept();
    }
  }

  interface FilterContext {

    default Collection<BranchUpdate> getBranchUpdates() {
      return emptyList();
    }

    default Collection<TagUpdate> getTagUpdates() {
      return emptyList();
    }
  }

  class Result {
    private final boolean accepted;
    private final String rejectReason;

    private Result(boolean accepted, String rejectReason) {
      this.accepted = accepted;
      this.rejectReason = rejectReason;
    }

    public static Result reject(String rejectReason) {
      return new Result(false, rejectReason);
    }

    public static Result reject() {
      return new Result(false, null);
    }

    public static Result accept() {
      return new Result(true, null);
    }

    public boolean isAccepted() {
      return accepted;
    }

    public Optional<String> getRejectReason() {
      return Optional.ofNullable(rejectReason);
    }
  }

  enum UpdateType {
    CREATE, DELETE, UPDATE
  }

  interface BranchUpdate {
    String getBranchName();

    Optional<Changeset> getChangeset();

    Optional<String> getNewRevision();

    Optional<String> getOldRevision();

    Optional<UpdateType> getUpdateType();

    boolean isForcedUpdate();
  }

  interface TagUpdate {
    String getTagName();

    Optional<Tag> getTag();

    Optional<String> getNewRevision();

    Optional<String> getOldRevision();

    Optional<UpdateType> getUpdateType();
  }
}
