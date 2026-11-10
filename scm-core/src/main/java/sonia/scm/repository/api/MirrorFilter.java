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

  /**
   * Evaluated for changed and unchanged branches and tags on every successful fetch.
   * For unchanged refs, the update type is empty, both revisions contain the current revision,
   * and the current changeset or tag is available. Accepting or rejecting such a ref is a no-op.
   */
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
    private enum Outcome { ACCEPT, REJECT, REMOVE }

    private final Outcome outcome;
    private final String reason;

    private Result(Outcome outcome, String reason) {
      this.outcome = outcome;
      this.reason = reason;
    }

    /**
     * Reject the incoming change while preserving the existing destination ref.
     * The reason describes only the cause; the command adds the action to the log entry.
     */
    public static Result reject(String rejectReason) {
      return new Result(Outcome.REJECT, rejectReason);
    }

    public static Result reject() {
      return new Result(Outcome.REJECT, null);
    }

    public static Result accept() {
      return new Result(Outcome.ACCEPT, null);
    }

    /** Exclude the ref entirely, deleting it from the destination if it exists. */
    public static Result remove() {
      return remove(null);
    }

    /**
     * Exclude the ref entirely, whether or not it exists in the destination.
     * The reason describes only the cause; the command adds the action to the log entry.
     */
    public static Result remove(String removeReason) {
      return new Result(Outcome.REMOVE, removeReason);
    }

    public boolean isRemovalRequested() {
      return outcome == Outcome.REMOVE;
    }

    public boolean isAccepted() {
      return outcome == Outcome.ACCEPT;
    }

    public Optional<String> getRejectReason() {
      return outcome == Outcome.REJECT ? Optional.ofNullable(reason) : Optional.empty();
    }

    public Optional<String> getRemoveReason() {
      return outcome == Outcome.REMOVE ? Optional.ofNullable(reason) : Optional.empty();
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
