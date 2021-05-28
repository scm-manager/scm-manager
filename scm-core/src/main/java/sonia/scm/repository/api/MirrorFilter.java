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

import sonia.scm.repository.Changeset;
import sonia.scm.repository.Tag;

import java.util.Collection;
import java.util.Optional;

import static java.util.Collections.emptyList;

public interface MirrorFilter {

  default Filter getFilter(FilterContext context) {
    return new Filter() {};
  }

  interface Filter {

    default boolean acceptBranch(BranchUpdate branch) {
      return true;
    }

    default boolean acceptTag(TagUpdate tag) {
      return true;
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

  interface BranchUpdate {
    String getBranchName();

    Changeset getChangeset();

    String getNewRevision();

    Optional<String> getOldRevision();

    boolean isForcedUpdate();
  }

  interface TagUpdate {
    String getTagName();

    Tag getTag();

    Optional<String> getOldRevision();
  }
}
