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

import sonia.scm.repository.HgConfigResolver;
import sonia.scm.repository.HgRepositoryFactory;
import sonia.scm.repository.Repository;
import sonia.scm.repository.api.HgHookBranchProvider;
import sonia.scm.repository.api.HgHookMessageProvider;
import sonia.scm.repository.api.HgHookTagProvider;
import sonia.scm.repository.api.HookBranchProvider;
import sonia.scm.repository.api.HookChangesetProvider;
import sonia.scm.repository.api.HookFeature;
import sonia.scm.repository.api.HookMessageProvider;
import sonia.scm.repository.api.HookTagProvider;

import java.util.EnumSet;
import java.util.Set;

/**
 * Mercurial implementation of {@link HookContextProvider}.
 *
 */
public class HgHookContextProvider extends HookContextProvider {

  private static final Set<HookFeature> SUPPORTED_FEATURES = EnumSet.of(
    HookFeature.CHANGESET_PROVIDER,
    HookFeature.MESSAGE_PROVIDER,
    HookFeature.BRANCH_PROVIDER,
    HookFeature.TAG_PROVIDER
  );

  private final HgHookChangesetProvider hookChangesetProvider;
  private HgHookMessageProvider hgMessageProvider;
  private HgHookBranchProvider hookBranchProvider;
  private HgHookTagProvider hookTagProvider;

  public HgHookContextProvider(HgConfigResolver configResolver, HgRepositoryFactory factory, Repository repository, String startRev) {
    this.hookChangesetProvider = new HgHookChangesetProvider(configResolver, factory, repository, startRev);
  }

  @Override
  public HookBranchProvider getBranchProvider() {
    if (hookBranchProvider == null) {
      hookBranchProvider = new HgHookBranchProvider(hookChangesetProvider);
    }
    return hookBranchProvider;
  }

  @Override
  public HookTagProvider getTagProvider() {
    if (hookTagProvider == null) {
      hookTagProvider = new HgHookTagProvider(hookChangesetProvider);
    }
    return hookTagProvider;
  }

  @Override
  public HookChangesetProvider getChangesetProvider()
  {
    return hookChangesetProvider;
  }

  public HgHookMessageProvider getHgMessageProvider() {
    if (hgMessageProvider == null) {
      hgMessageProvider = new HgHookMessageProvider();
    }
    return hgMessageProvider;
  }

  @Override
  public Set<HookFeature> getSupportedFeatures()
  {
    return SUPPORTED_FEATURES;
  }

  @Override
  protected HookMessageProvider createMessageProvider()
  {
    return getHgMessageProvider();
  }
}
