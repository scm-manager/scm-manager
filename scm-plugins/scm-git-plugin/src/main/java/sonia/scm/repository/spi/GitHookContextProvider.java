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


import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.transport.ReceiveCommand;
import org.eclipse.jgit.transport.ReceivePack;
import sonia.scm.repository.api.GitHookBranchProvider;
import sonia.scm.repository.GitChangesetConverterFactory;
import sonia.scm.repository.api.GitHookModificationsProvider;
import sonia.scm.repository.api.GitHookMessageProvider;
import sonia.scm.repository.api.GitHookTagProvider;
import sonia.scm.repository.api.GitReceiveHookMergeDetectionProvider;
import sonia.scm.repository.api.HookBranchProvider;
import sonia.scm.repository.api.HookChangesetProvider;
import sonia.scm.repository.api.HookModificationsProvider;
import sonia.scm.repository.api.HookFeature;
import sonia.scm.repository.api.HookMessageProvider;
import sonia.scm.repository.api.HookTagProvider;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;


public class GitHookContextProvider extends HookContextProvider
{

  
  private static final Set<HookFeature> SUPPORTED_FEATURES = EnumSet.of(
    HookFeature.MESSAGE_PROVIDER,
    HookFeature.CHANGESET_PROVIDER,
    HookFeature.BRANCH_PROVIDER,
    HookFeature.TAG_PROVIDER,
    HookFeature.MODIFICATIONS_PROVIDER,
    HookFeature.MERGE_DETECTION_PROVIDER
  );


  private final GitChangesetConverterFactory converterFactory;

  public GitHookContextProvider(
    GitChangesetConverterFactory converterFactory, ReceivePack receivePack,
                                List<ReceiveCommand> receiveCommands,
    Repository repository,
    String repositoryId
  ) {
    this.receivePack = receivePack;
    this.receiveCommands = receiveCommands;
    this.repository = repository;
    this.repositoryId = repositoryId;
    this.changesetProvider = new GitHookChangesetProvider(converterFactory, receivePack,
      receiveCommands);
    this.converterFactory = converterFactory;
  }


  @Override
  public HookMessageProvider createMessageProvider()
  {
    return new GitHookMessageProvider(receivePack);
  }


  @Override
  public HookBranchProvider getBranchProvider()
  {
    return new GitHookBranchProvider(receiveCommands);
  }

  @Override
  public HookTagProvider getTagProvider() {
    return new GitHookTagProvider(receiveCommands, repository);
  }

  @Override
  public HookChangesetProvider getChangesetProvider()
  {
    return changesetProvider;
  }

  @Override
  public HookMergeDetectionProvider getMergeDetectionProvider() {
    return new GitReceiveHookMergeDetectionProvider(repository, repositoryId, receiveCommands, converterFactory);
  }

  @Override
  public HookModificationsProvider getModificationsProvider() {
    return new GitHookModificationsProvider(receiveCommands, repository);
  }

  @Override
  public Set<HookFeature> getSupportedFeatures()
  {
    return SUPPORTED_FEATURES;
  }

  public String getRepositoryId() {
    return repositoryId;
  }

  private final GitHookChangesetProvider changesetProvider;
  private final List<ReceiveCommand> receiveCommands;
  private final ReceivePack receivePack;
  private final Repository repository;
  private final String repositoryId;
}
