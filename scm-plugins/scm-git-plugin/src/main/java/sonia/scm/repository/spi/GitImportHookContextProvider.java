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
import org.eclipse.jgit.revwalk.RevCommit;
import sonia.scm.repository.GitChangesetConverter;
import sonia.scm.repository.Tag;
import sonia.scm.repository.api.HookBranchProvider;
import sonia.scm.repository.api.HookChangesetProvider;
import sonia.scm.repository.api.HookFeature;
import sonia.scm.repository.api.HookTagProvider;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.function.Supplier;

class GitImportHookContextProvider extends HookContextProvider {
  private final GitChangesetConverter converter;
  private final List<Tag> newTags;
  private final Supplier<Iterable<RevCommit>> changesetResolver;
  private final List<String> newBranches;

  GitImportHookContextProvider(GitChangesetConverter converter,
                               List<String> newBranches,
                               List<Tag> newTags,
                               Supplier<Iterable<RevCommit>> changesetResolver) {
    this.converter = converter;
    this.newTags = newTags;
    this.changesetResolver = changesetResolver;
    this.newBranches = newBranches;
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
      GitConvertingChangesetIterable changesets = new GitConvertingChangesetIterable(changesetResolver.get(), converter);
      return r -> new HookChangesetResponse(changesets);
  }
}
