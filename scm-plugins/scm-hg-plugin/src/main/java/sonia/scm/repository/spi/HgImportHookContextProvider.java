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
