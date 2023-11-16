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

import com.google.common.collect.ImmutableSet;
import sonia.scm.repository.Changeset;
import sonia.scm.repository.Tag;
import sonia.scm.repository.api.HookBranchProvider;
import sonia.scm.repository.api.HookChangesetProvider;
import sonia.scm.repository.api.HookFeature;
import sonia.scm.repository.api.HookTagProvider;

import java.util.Collections;
import java.util.List;
import java.util.Set;

class HgImportHookContextProvider extends HookContextProvider {
  private final List<String> newBranches;
  private final List<Tag> newTags;
  private final HgLazyChangesetResolver changesetResolver;

  HgImportHookContextProvider(List<String> newBranches, List<Tag> newTags, HgLazyChangesetResolver changesetResolver) {
    this.newBranches = newBranches;
    this.newTags = newTags;
    this.changesetResolver = changesetResolver;
  }

  @Override
  public Set<HookFeature> getSupportedFeatures() {
    return ImmutableSet.of(HookFeature.CHANGESET_PROVIDER, HookFeature.BRANCH_PROVIDER, HookFeature.TAG_PROVIDER);
  }

  @Override
  public HookTagProvider getTagProvider() {
    return new HookTagProvider() {
      @Override
      public List<Tag> getCreatedTags() {
        return newTags;
      }

      @Override
      public List<Tag> getDeletedTags() {
        return Collections.emptyList();
      }
    };
  }

  @Override
  public HookBranchProvider getBranchProvider() {
    return new HookBranchProvider() {
      @Override
      public List<String> getCreatedOrModified() {
        return newBranches;
      }

      @Override
      public List<String> getDeletedOrClosed() {
        return Collections.emptyList();
      }
    };
  }

  @Override
  public HookChangesetProvider getChangesetProvider() {
    Iterable<Changeset> changesets = changesetResolver.call();
    return r -> new HookChangesetResponse(changesets);
  }
}
