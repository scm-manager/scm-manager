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
